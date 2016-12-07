package org.devefx.snio.net;

import org.devefx.snio.Request;
import org.devefx.snio.Session;

import java.net.InetSocketAddress;

public class StandardRequest implements Request {

    private String protocol;
    private int contentLength;
    private InetSocketAddress remoteAddr;
    private InetSocketAddress localAddr;
    private Session session;
    private Object object;
    private Object requestType;

    public void setProtocol(String protocol) {
        this.protocol = protocol;
    }

    public void setContentLength(int contentLength) {
        this.contentLength = contentLength;
    }

    public void setRemoteAddr(InetSocketAddress remoteAddr) {
        this.remoteAddr = remoteAddr;
    }

    public void setLocalAddr(InetSocketAddress localAddr) {
        this.localAddr = localAddr;
    }

    public void setSession(Session session) {
        this.session = session;
    }

    public void setObject(Object object) {
        this.object = object;
    }

    public void setRequestType(Object requestType) {
        this.requestType = requestType;
    }

    @Override
    public String getProtocol() {
        return protocol;
    }

    @Override
    public int getContentLength() {
        return contentLength;
    }

    @Override
    public InetSocketAddress getRemoteAddr() {
        return remoteAddr;
    }

    @Override
    public String getRemoteHost() {
        return remoteAddr.getHostName();
    }

    @Override
    public int getRemotePort() {
        return remoteAddr.getPort();
    }

    @Override
    public InetSocketAddress getLocalAddr() {
        return localAddr;
    }

    @Override
    public String getLocalHost() {
        return localAddr.getHostName();
    }

    @Override
    public int getLocalPort() {
        return localAddr.getPort();
    }

    @Override
    public String getSessionId() {
        return session.getId();
    }

    @Override
    public Session getSession() {
        return session;
    }

    @Override
    public Object getRequestType() {
        return requestType;
    }

    @Override
    public <T> T readerObject(Class<T> requiredClass) {
        if (!requiredClass.isInstance(object)) {
            throw new ClassCastException(object.getClass() + " cannot be cast to " + requiredClass);
        }
        return (T) object;
    }
}
