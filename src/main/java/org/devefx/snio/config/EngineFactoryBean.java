package org.devefx.snio.config;

import org.devefx.snio.Engine;
import org.devefx.snio.LifecycleException;
import org.devefx.snio.Manager;
import org.devefx.snio.Server;
import org.devefx.snio.core.StandardEngine;
import org.springframework.beans.factory.FactoryBean;
import org.springframework.context.Lifecycle;

import java.util.List;

public class EngineFactoryBean implements FactoryBean<Engine>, Lifecycle {

    private StandardEngine engine = new StandardEngine();

    public void setHost(String host) {
        engine.setHost(host);
    }

    public void setServers(List<Server> servers) {
        if (servers != null) {
            for (Server server : servers) {
                engine.addServer(server);
            }
        }
    }

    public void setManager(Manager manager) {
        engine.setManager(manager);
    }

    @Override
    public Engine getObject() throws Exception {
        return engine;
    }

    @Override
    public Class<?> getObjectType() {
        return StandardEngine.class;
    }

    @Override
    public boolean isSingleton() {
        return false;
    }

    @Override
    public void start() {
        try {
            engine.start();
            engine.await();
        } catch (LifecycleException e) {
            e.printStackTrace();
        }
    }

    @Override
    public void stop() {
        try {
            engine.stop();
        } catch (LifecycleException e) {
            e.printStackTrace();
        }
    }

    @Override
    public boolean isRunning() {
        return engine.isRunning();
    }
}
