package org.devefx.snio.config;

import org.devefx.snio.Server;
import org.devefx.snio.Service;
import org.devefx.snio.net.ServerBase;
import org.devefx.snio.net.ServerInitializer;
import org.devefx.snio.net.tcp.TCPServer;
import org.springframework.beans.factory.FactoryBean;

import java.util.List;

public class ServerFactoryBean implements FactoryBean<Server> {

    private ServerBase server;

    public ServerFactoryBean(ServerBase server) {
        this.server = server;
    }

    public void setServerInitializer(ServerInitializer serverInitializer) {
        server.setServerInitializer(serverInitializer);
    }

    public void setServices(List<Service> services) {
        if (services != null) {
            for (Service service : services) {
                server.addService(service);
            }
        }
    }

    public void setPort(int port) {
        server.setPort(port);
    }

    public void setLengthFieldLength(int lengthFieldLength) {
        if (server instanceof TCPServer) {
            ((TCPServer) server).setLengthFieldLength(lengthFieldLength);
        } else {
            throw new RuntimeException("setLengthFieldLength: Can only use TCPServer.");
        }
    }

    public void setLengthFieldOffset(int lengthFieldOffset) {
        if (server instanceof TCPServer) {
            ((TCPServer) server).setLengthFieldOffset(lengthFieldOffset);
        } else {
            throw new RuntimeException("setLengthFieldOffset: Can only use TCPServer.");
        }
    }

    @Override
    public Server getObject() throws Exception {
        return server;
    }

    @Override
    public Class<?> getObjectType() {
        return server.getClass();
    }

    @Override
    public boolean isSingleton() {
        return false;
    }
}
