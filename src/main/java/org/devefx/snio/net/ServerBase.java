package org.devefx.snio.net;

import org.devefx.snio.*;
import org.devefx.snio.core.Dispatcher;
import org.devefx.snio.util.LifecycleSupport;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.beans.PropertyChangeSupport;
import java.net.InetSocketAddress;
import java.net.SocketAddress;
import java.util.ArrayList;

public abstract class ServerBase implements Server, Lifecycle, Runnable {

    private static final Logger log = LoggerFactory.getLogger(ServerBase.class);
    protected LifecycleSupport lifecycle = new LifecycleSupport(this);
    private int port = 11894;
    private ArrayList<Service> services = new ArrayList<>();
    private Thread thread;
    protected boolean started = false;
    protected boolean initialized = false;
    protected PropertyChangeSupport support = new PropertyChangeSupport(this);
    protected Container container;
    protected ServerInitializer serverInitializer;
    protected Dispatcher dispatcher = new Dispatcher(this);

    @Override
    public Container getContainer() {
        return container;
    }

    @Override
    public void setContainer(Container container) {
        this.container = container;
    }

    @Override
    public void setPort(int port) {
        this.port = port;
    }

    @Override
    public int getPort() {
        return port;
    }

    public void setServerInitializer(ServerInitializer serverInitializer) {
        this.serverInitializer = serverInitializer;
    }

    @Override
    public void start() throws LifecycleException {
        if (!started) {
            initialize();
            lifecycle.fireLifecycleEvent(BEFORE_START_EVENT, this);
            dispatcher.start();
            thread = new Thread(this, getInfo());
            thread.setDaemon(true);
            thread.start();
            lifecycle.fireLifecycleEvent(START_EVENT, this);
            started = true;
            lifecycle.fireLifecycleEvent(AFTER_START_EVENT, this);
        }
    }

    @Override
    public void stop() throws LifecycleException {
        if (started) {
            try {
                lifecycle.fireLifecycleEvent(BEFORE_STOP_EVENT, this);
                dispatcher.stop();
                thread.interrupt();
                thread.join();
                lifecycle.fireLifecycleEvent(STOP_EVENT, this);
                started = false;
                lifecycle.fireLifecycleEvent(AFTER_STOP_EVENT, this);
            } catch (InterruptedException e) {
                log.error("Stop the server an error occurred", e);
            }
        }
    }

    @Override
    public void addService(Service service) {
        synchronized (services) {
            services.add(service);
            support.firePropertyChange("service",  null, service);
        }
    }

    @Override
    public Service findService(Object type) {
        if (type == null) {
            return null;
        }
        synchronized (services) {
            for (Service service : services) {
                if (type.equals(service.getType())) {
                    return service;
                }
            }
        }
        return null;
    }

    @Override
    public Service[] findServices() {
        synchronized (services) {
            return services.toArray(new Service[services.size()]);
        }
    }

    @Override
    public void removeService(Service service) {
        synchronized (services) {
            services.remove(service);
        }
    }

    @Override
    public void removeLifecycleListener(LifecycleListener listener) {
        lifecycle.removeLifecycleListener(listener);
    }

    @Override
    public void addLifecycleListener(LifecycleListener listener) {
        lifecycle.addLifecycleListener(listener);
    }

    @Override
    public LifecycleListener[] findLifecycleListeners() {
        return lifecycle.findLifecycleListeners();
    }

    protected SocketAddress bindAddress() {
        if (container instanceof Engine) {
            return new InetSocketAddress(((Engine) container).getHost(), getPort());
        }
        return new InetSocketAddress(getPort());
    }

}
