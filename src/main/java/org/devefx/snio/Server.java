package org.devefx.snio;

public interface Server {

    String getInfo();

    String getName();

    void setName(String name);

    int getPort();

    void setPort(int port);

    void addService(Service service);

    Service findService(Object type);

    Service[] findServices();

    void removeService(Service service);

    Engine getEngine();

    void setEngine(Engine engine);

    void initialize() throws LifecycleException;
}
