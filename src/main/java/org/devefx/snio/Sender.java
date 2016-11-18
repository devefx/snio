package org.devefx.snio;

import java.net.SocketAddress;

public interface Sender {

    boolean isActive();

    boolean isWritable();

    SocketAddress remoteAddress();

    void writeAndFlush(byte[] bytes);

    void close();

}
