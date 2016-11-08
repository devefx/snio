package org.devefx.snio.core;

import org.devefx.snio.*;
import org.devefx.snio.util.LifecycleSupport;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

public abstract class ServerBase implements Lifecycle, Server {
    private static final Service[] EMPTY_ARRAY = new Service[0];
    private String name;
    protected LifecycleSupport lifecycle = new LifecycleSupport(this);
    private int port = 11894;
    private Map<Object, Service> serviceMap = new ConcurrentHashMap<>();
    protected boolean started = false;
    protected boolean initialized = false;
    private Engine engine;

    @Override
    public String getName() {
        return name;
    }

    @Override
    public void setName(String name) {
        this.name = name;
    }

    @Override
    public int getPort() {
        return port;
    }

    @Override
    public void setPort(int port) {
        this.port = port;
    }

    @Override
    public void addService(Service service) {
    	if (service.getType() == null) {
			throw new RuntimeException(service + ": type is null");
		}
        serviceMap.put(service.getType(), service);
    }

    @Override
    public Service findService(Object type) {
        if (type == null) {
            return null;
        }
        return serviceMap.get(type);
    }

    @Override
    public Service[] findServices() {
        return serviceMap.values().toArray(EMPTY_ARRAY);
    }

    @Override
    public void removeService(Service service) {
        serviceMap.remove(service);
    }

    @Override
    public Engine getEngine() {
        return engine;
    }

    @Override
    public void setEngine(Engine engine) {
        this.engine = engine;
    }

    @Override
    public void addLifecycleListener(LifecycleListener listener) {
        lifecycle.addLifecycleListener(listener);
    }

    @Override
    public LifecycleListener[] findLifecycleListeners() {
        return lifecycle.findLifecycleListeners();
    }

    @Override
    public void removeLifecycleListener(LifecycleListener listener) {
        lifecycle.removeLifecycleListener(listener);
    }
}
