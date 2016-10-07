package org.devefx.snio.core;

import org.devefx.snio.Engine;
import org.devefx.snio.Lifecycle;
import org.devefx.snio.LifecycleException;
import org.devefx.snio.Server;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

public class StandardEngine extends ContainerBase implements Engine {
    private static Logger log = LoggerFactory.getLogger(StandardEngine.class);
    private static final Server[] EMPTY_ARRAY = new Server[0];
    private String defaultHost = "0.0.0.0";
    private Map<String, Server> serverMap = new ConcurrentHashMap<>();
    private boolean stopAwait = false;

    @Override
    public String getInfo() {
        return "StandardEngine/1.0";
    }

    @Override
    public String getDefaultHost() {
        return defaultHost;
    }

    @Override
    public void setDefaultHost(String host) {
        String oldDefaultHost = this.defaultHost;
        this.defaultHost = host;
        support.firePropertyChange("defaultHost", oldDefaultHost, host);
    }

    @Override
    public void addServer(Server server) {
        server.setEngine(this);
        serverMap.put(server.getName(), server);
        if (initialized) {
            try {
                server.initialize();
            } catch (LifecycleException e) {
                log.error(sm.getString("standardEngine.addServer.initialize"), e);
            }
        }
        if (started && server instanceof Lifecycle) {
            try {
                ((Lifecycle) server).start();
            } catch (LifecycleException e) {
                log.error(sm.getString("standardEngine.addServer.start"), e);
            }
        }
        support.firePropertyChange("server", null, server);
    }

    @Override
    public Server findServer(String name) {
        if (name == null) {
            return null;
        }
        return serverMap.get(name);
    }

    @Override
    public Server[] findServers() {
        return serverMap.values().toArray(EMPTY_ARRAY);
    }

    @Override
    public void removeServer(Server server) {
        serverMap.remove(server);
    }

    public void init() throws LifecycleException {
        if (!initialized) {
            initialized = true;
            for (Server server : findServers()) {
                server.initialize();
            }
        }
    }

    @Override
    public void start() throws LifecycleException {
        if (!started) {
            if (!initialized) {
                init();
            }
            for (Server server: findServers()) {
                if (server instanceof Lifecycle) {
                    ((Lifecycle) server).start();
                }
            }

            super.start();
        }
    }

    @Override
    public void stop() throws LifecycleException {
        if (started) {
            for (Server server: findServers()) {
                if (server instanceof Lifecycle) {
                    ((Lifecycle) server).stop();
                }
            }
            super.stop();
        }
    }

    public void stopAwait() {
        this.stopAwait = true;
    }

    public void await() {
        while (!stopAwait) {
            try {
                Thread.sleep(10000L);
            } catch (InterruptedException e) {

            }
        }
    }

    @Override
    public String toString() {
        StringBuffer sb = new StringBuffer("StandardEngine[");
        sb.append(this.getName());
        sb.append("]");
        return sb.toString();
    }
}
