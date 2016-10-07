package org.devefx.snio.net;

import io.netty.buffer.ByteBuf;

import java.net.SocketAddress;

public interface Sender {

    boolean isActive();

    boolean isWritable();

    SocketAddress remoteAddress();

    void writeAndFlush(ByteBuf buf);

    void writeAndFlush(byte[] bytes);

    void close();
}
