package org.devefx.snio.config;

import org.devefx.snio.Server;
import org.devefx.snio.Service;
import org.springframework.beans.factory.FactoryBean;

import java.util.Iterator;
import java.util.List;

public class ServerFactoryBean implements FactoryBean<Server> {

    private Server server;

    public ServerFactoryBean(Server server) {
        this.server = server;
    }

    public void setServices(List<Service> services) {
        Iterator<Service> it = services.iterator();
        while (it.hasNext()) {
            server.addService(it.next());
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
