package org.devefx.snio.config;

import org.devefx.snio.Engine;
import org.devefx.snio.Manager;
import org.devefx.snio.Server;
import org.devefx.snio.core.StandardEngine;
import org.springframework.beans.factory.FactoryBean;
import org.springframework.beans.factory.InitializingBean;

import java.util.Iterator;
import java.util.List;

public class EngineFactoryBean implements InitializingBean, FactoryBean<Engine> {

    private StandardEngine engine = new StandardEngine();
    private boolean autoStart = true;

    public void setHost(String host) {
        engine.setDefaultHost(host);
    }

    public void setAutoStart(boolean autoStart) {
        this.autoStart = autoStart;
    }

    public void setManager(Manager manager) {
        engine.setManager(manager);
    }

    public void setServer(List<Server> serverList) {
        Iterator<Server> it = serverList.iterator();
        while (it.hasNext()) {
            engine.addServer(it.next());
        }
    }

    @Override
    public void afterPropertiesSet() throws Exception {
        if (autoStart) {
            engine.start();
            engine.await();
        }
    }

    @Override
    public Engine getObject() throws Exception {
        return engine;
    }

    @Override
    public Class<?> getObjectType() {
        return engine.getClass();
    }

    @Override
    public boolean isSingleton() {
        return false;
    }
}
