package org.devefx.snio.net;

import io.netty.buffer.ByteBuf;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelInboundHandlerAdapter;
import io.netty.channel.socket.DatagramPacket;
import org.devefx.snio.*;
import org.devefx.snio.codec.MessageToMessageDecoder;
import org.devefx.snio.core.StandardEngine;
import org.devefx.snio.net.connector.*;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.net.SocketAddress;
import java.util.HashMap;
import java.util.Map;

public class NetChannelHandler extends ChannelInboundHandlerAdapter {
    private static final String PROTOCOL_UDP = "UDP";
    private static final String PROTOCOL_TCP = "TCP";
    
    private Map<SocketAddress, String> addrMapperSession = new HashMap<>(100);
    private NetContext context = new NetContext();
    private MessageDispatcher dispatcher;
    
    public NetChannelHandler(Server server, MessageToMessageDecoder decoder) {
        Service[] services = server.findServices();
        dispatcher = new MessageDispatcher(services.length);
        for (int i = 0; i < services.length; i++) {
            dispatcher.addService(services[i]);
        }
        dispatcher.start();
        
        final StandardEngine engine = (StandardEngine) server.getEngine();
        engine.addLifecycleListener(new LifecycleListener() {
            @Override
            public void lifecycleEvent(LifecycleEvent event) {
                if (Lifecycle.AFTER_START_EVENT.equals(event.getType())) {
                	Manager manager = engine.getManager();
                	manager.addLifecycleListener(new SessionLifecycleListener());
                    context.setManager(manager);
                }
            }
        });
        context.setDecoder(decoder);
    }

    @Override
    public void channelRead(ChannelHandlerContext ctx, Object msg) throws Exception {
        if (msg instanceof DatagramPacket) {
            readInUdp(ctx, (DatagramPacket) msg);
        } else {
            readInTcp(ctx, (ByteBuf) msg);
        }
    }

    @Override
    public void channelReadComplete(ChannelHandlerContext ctx) throws Exception {
        ctx.flush();
    }

    @Override
    public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause) throws Exception {
        if (cause instanceof IOException) {
            SocketAddress addr = ctx.channel().remoteAddress();
            if (addr != null) {
                Session session = getOrCreateSession(addr, -1);
                session.expire();
            }
        }
        cause.printStackTrace();
    }

    private void readInUdp(ChannelHandlerContext ctx, DatagramPacket packet) throws Exception {
        // decoder
        Message message = context.getDecoder().decode(packet.content());

        // request
        Session session = getOrCreateSession(packet.sender(), 60);
        if (context.findSender(session.getId()) == null) {
        	context.addSender(session.getId(), new UdpSender(ctx.channel(), packet.sender()));
		}
        
        NetRequest request = new NetRequest();
        request.setProtocol(PROTOCOL_UDP);
        request.setLocalAddress(packet.recipient());
        request.setRemoteAddress(packet.sender());
        request.setContentLength(packet.content().capacity());
        request.setContent(message.getContent());
        request.setSession(session);
        request.setContext(context);

        // dispatcher
        fireRequestEvent(request, message.getType());
    }

    private void readInTcp(ChannelHandlerContext ctx, ByteBuf msg) throws Exception {
        // decoder
        Message message = context.getDecoder().decode(msg);

        // request
        Session session = getOrCreateSession(ctx.channel().remoteAddress(), -1);
        if (context.findSender(session.getId()) == null) {
        	context.addSender(session.getId(), new TcpSender(ctx.channel()));
		}
        
        NetRequest request = new NetRequest();
        request.setProtocol(PROTOCOL_TCP);
        request.setLocalAddress((InetSocketAddress) ctx.channel().localAddress());
        request.setRemoteAddress((InetSocketAddress) ctx.channel().remoteAddress());
        request.setContentLength(msg.capacity());
        request.setContent(message.getContent());
        request.setSession(session);
        request.setContext(context);
        
        // dispatcher
        fireRequestEvent(request, message.getType());
    }

	public Session getOrCreateSession(SocketAddress addr, int interval) throws IOException {
		String sessionId = addrMapperSession.get(addr);
		Session session = null;
		if (sessionId == null || (session = context.getManager().findSession(sessionId)) == null) {
            session = context.getManager().createSession(null);
            session.setMaxInactiveInterval(interval);
            context.getManager().add(session);
            addrMapperSession.put(addr, session.getId());
        }
		return session;
	}

    public void fireRequestEvent(Request request, Object type) {
        dispatcher.push(new MessageEvent(request, type));
    }
    
    class SessionLifecycleListener implements LifecycleListener {
		@Override
		public void lifecycleEvent(LifecycleEvent event) {
			if (Lifecycle.SESSION_EXPIRED_EVENT.equals(event.getType())) {
				String sessionId = (String) event.getData();
				for (Map.Entry<SocketAddress, String> entry : addrMapperSession.entrySet()) {
					if (entry.getValue().equals(sessionId)) {
						addrMapperSession.remove(entry.getKey());
						break;
					}
				}
				context.removeSender(sessionId);
			}
		}
    }
}
