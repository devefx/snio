package org.devefx.snio.net.udp;

import io.netty.buffer.Unpooled;
import io.netty.channel.Channel;
import io.netty.channel.socket.DatagramPacket;
import org.devefx.snio.Sender;

import java.net.InetSocketAddress;
import java.net.SocketAddress;

public class UDPSender implements Sender {

    private final Channel channel;
    private final InetSocketAddress address;

    public UDPSender(Channel channel, InetSocketAddress address) {
        this.channel = channel;
        this.address = address;
    }

    @Override
    public boolean isActive() {
        return channel.isActive();
    }

    @Override
    public boolean isWritable() {
        return channel.isWritable();
    }

    @Override
    public SocketAddress remoteAddress() {
        return address;
    }

    @Override
    public void writeAndFlush(byte[] bytes) {
        if (bytes != null) {
            DatagramPacket packet = new DatagramPacket(Unpooled.copiedBuffer(bytes), address);
            channel.writeAndFlush(packet);
        }
    }

    @Override
    public void close() {
        channel.close();
    }


}
