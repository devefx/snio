package org.devefx.snio.net;

import org.devefx.snio.Context;
import org.devefx.snio.Request;
import org.devefx.snio.Session;
import org.devefx.snio.util.StringManager;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.net.InetSocketAddress;
import java.security.Principal;

public class NetRequest implements Request {
    private static final Logger log = LoggerFactory.getLogger(NetRequest.class);
    private static final StringManager sm = StringManager.getManager("org.devefx.snio.net");
    private int contentLength;
    private Principal principal;
    private String protocol;
    private Session session;
    private InetSocketAddress localAddress;
    private InetSocketAddress remoteAddress;
    private Object content;

    @Override
    public int getContentLength() {
        return contentLength;
    }

    protected void setContentLength(int contentLength) {
        this.contentLength = contentLength;
    }

    protected void setContent(Object content) {
        this.content = content;
    }

    @Override
    public String getProtocol() {
        return protocol;
    }

    protected void setProtocol(String protocol) {
        this.protocol = protocol;
    }

    @Override
    public InetSocketAddress getRemoteAddr() {
        return remoteAddress;
    }

    protected void setRemoteAddress(InetSocketAddress remoteAddress) {
        this.remoteAddress = remoteAddress;
    }

    @Override
    public String getRemoteHost() {
        return remoteAddress.getHostName();
    }

    @Override
    public int getRemotePort() {
        return remoteAddress.getPort();
    }

    @Override
    public InetSocketAddress getLocalAddr() {
        return localAddress;
    }

    protected void setLocalAddress(InetSocketAddress localAddress) {
        this.localAddress = localAddress;
    }

    @Override
    public String getLocalHost() {
        return localAddress.getHostName();
    }

    @Override
    public int getLocalPort() {
        return localAddress.getPort();
    }

    @Override
    public String getRemoteUser() {
        return null;
    }

    @Override
    public Principal getPrincipal() {
        return principal;
    }

    @Override
    public String getSessionId() {
        return session.getId();
    }

    @Override
    public Session getSession() {
        return session;
    }

    protected void setSession(Session session) {
        this.session = session;
    }

    @Override
    public Context getContext() {
        return null;
    }

    @Override
    public <T> T readerObject(Class<T> requiredClass) {
        if (!requiredClass.isAssignableFrom(content.getClass())) {
            log.error(sm.getString("netRequest.readerObject", content.getClass().getName(), requiredClass.getName()));
            return null;
        }
        return (T) content;
    }
}
