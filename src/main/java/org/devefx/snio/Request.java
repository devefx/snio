package org.devefx.snio;

import java.net.InetSocketAddress;
import java.security.Principal;

public interface Request {

    int getContentLength();

    String getProtocol();

    InetSocketAddress getRemoteAddr();

    String getRemoteHost();

    int getRemotePort();

    InetSocketAddress getLocalAddr();

    String getLocalHost();

    int getLocalPort();

    String getRemoteUser();

    Principal getPrincipal();

    String getSessionId();

    Session getSession();

    Context getContext();

    <T> T readerObject(Class<T> requiredClass);
}
