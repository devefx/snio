package org.devefx.snio.net.tcp;

import io.netty.buffer.Unpooled;
import io.netty.channel.Channel;
import org.devefx.snio.Sender;

import java.net.SocketAddress;

public class TCPSender implements Sender {

    private final Channel channel;

    public TCPSender(Channel channel) {
        this.channel = channel;
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
        return channel.remoteAddress();
    }

    @Override
    public void writeAndFlush(byte[] bytes) {
        if (bytes != null) {
            channel.writeAndFlush(Unpooled.copiedBuffer(bytes));
        }
    }

    @Override
    public void close() {
        channel.close();
    }
}
