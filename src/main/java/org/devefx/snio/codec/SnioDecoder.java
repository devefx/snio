package org.devefx.snio.codec;

import io.netty.buffer.ByteBuf;
import io.netty.channel.Channel;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.socket.DatagramPacket;
import io.netty.handler.codec.MessageToMessageDecoder;
import org.devefx.snio.Manager;
import org.devefx.snio.Sender;
import org.devefx.snio.Service;
import org.devefx.snio.Session;
import org.devefx.snio.event.RequestEvent;
import org.devefx.snio.net.StandardRequest;
import org.devefx.snio.net.tcp.TCPSender;
import org.devefx.snio.net.udp.UDPSender;

import java.net.InetSocketAddress;
import java.net.SocketAddress;
import java.util.List;

public class SnioDecoder extends MessageToMessageDecoder<Object> {

    public static final String PROTOCOL_TCP = "TCP";
    public static final String PROTOCOL_UDP = "UDP";

    private Manager manager;

    public SnioDecoder(Manager manager) {
        this.manager = manager;
    }

    @Override
    public boolean acceptInboundMessage(Object msg) throws Exception {
        if (msg instanceof ByteBuf || msg instanceof DatagramPacket) {
            return true;
        }
        return false;
    }

    @Override
    protected void decode(ChannelHandlerContext ctx, Object msg, List<Object> out) throws Exception {

        StandardRequest request = new StandardRequest();

        ByteBuf buf;
        Sender sender;
        if (msg instanceof DatagramPacket) {
            DatagramPacket packet = (DatagramPacket) msg;
            buf = packet.content();
            sender = new UDPSender(ctx.channel(), packet.sender());
            request.setProtocol(PROTOCOL_UDP);
            request.setRemoteAddr(packet.sender());
            request.setLocalAddr(packet.recipient());
        } else {
            Channel channel = ctx.channel();
            buf = (ByteBuf) msg;
            sender = new TCPSender(ctx.channel());
            request.setProtocol(PROTOCOL_TCP);
            request.setRemoteAddr((InetSocketAddress) channel.remoteAddress());
            request.setLocalAddr((InetSocketAddress) channel.localAddress());
        }

        byte[] bytes;
        if (buf.isDirect()) {
            bytes = new byte[buf.readableBytes()];
            buf.readBytes(bytes);
        } else {
            bytes = buf.array();
        }

        String sessionId = makeSessionIdForAddress(sender.remoteAddress());
        Session session = manager.findSession(sessionId);
        if (session == null) {
            session = manager.createSession(sessionId);
            session.setMaxInactiveInterval(manager.getMaxInactiveInterval());
            manager.add(session);
        }
        session.setSender(sender);

        request.setSession(session);
        request.setObject(bytes);
        request.setContentLength(bytes.length);

        out.add(new RequestEvent(request, Service.DEFAULT_TYPE));
    }

    protected String makeSessionIdForAddress(SocketAddress address) {
        return address.toString();
    }
}
