package org.devefx.snio;

import java.net.InetSocketAddress;

public interface Request {

    int getContentLength();

    String getProtocol();

    InetSocketAddress getRemoteAddr();

    String getRemoteHost();

    int getRemotePort();

    InetSocketAddress getLocalAddr();

    String getLocalHost();

    int getLocalPort();

    String getSessionId();

    Session getSession();

    <T> T readerObject(Class<T> requiredClass);

}
