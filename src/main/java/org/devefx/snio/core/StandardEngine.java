package org.devefx.snio.core;

import org.devefx.snio.Engine;
import org.devefx.snio.Lifecycle;
import org.devefx.snio.LifecycleException;
import org.devefx.snio.Server;
import org.devefx.snio.session.StandardManager;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import java.util.ArrayList;

public class StandardEngine extends ContainerBase implements Engine {

    private static Logger log = LoggerFactory.getLogger(StandardEngine.class);
    private ArrayList<Server> servers = new ArrayList<>();
    private String host = "localhost";
    private boolean stopAwait = false;

    @Override
    public String getHost() {
        return host;
    }

    @Override
    public void setHost(String host) {
        String oldHost = this.host;
        this.host = host;
        support.firePropertyChange("host", oldHost, host);
    }

    @Override
    public String getInfo() {
        return "StandardEngine/1.1";
    }

    public void init() throws LifecycleException {
        if (!initialized) {
            if (manager == null) {
                setManager(new StandardManager());
            }
            for (Server server : servers) {
                server.initialize();
            }
            initialized = true;
        }
    }

    @Override
    public void start() throws LifecycleException {
        if (!started) {
            init();
            for (Server server : servers) {
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
            for (Server server : servers) {
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
                // ignore
            }
        }
    }

    @Override
    public void addServer(Server server) {
        server.setContainer(this);
        synchronized (servers) {
            servers.add(server);
            if (initialized) {
                try {
                    server.initialize();
                } catch (LifecycleException e) {
                    log.error(sm.getString("standardEngine.addServer.initialize", server), e);
                    return;
                }
            }
            if (started && server instanceof Lifecycle) {
                try {
                    ((Lifecycle) server).start();
                } catch (LifecycleException e) {
                    log.error(sm.getString("standardEngine.addServer.start", server), e);
                    return;
                }
            }
            support.firePropertyChange("servers", null, server);
        }
    }

    @Override
    public Server[] findServers() {
        synchronized (servers) {
            return servers.toArray(new Server[servers.size()]);
        }
    }

    @Override
    public void removeServer(Server server) {
        synchronized (servers) {
            servers.remove(server);
        }
    }
}
