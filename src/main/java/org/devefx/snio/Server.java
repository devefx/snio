package org.devefx.snio;

public interface Server {

    String getInfo();

    int getPort();

    void setPort(int port);

    void addService(Service service);

    Service findService(Object type);

    Service[] findServices();

    void removeService(Service service);

    Container getContainer();

    void setContainer(Container container);

    void initialize() throws LifecycleException;
}
