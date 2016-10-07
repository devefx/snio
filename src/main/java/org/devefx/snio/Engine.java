package org.devefx.snio;

public interface Engine {

    String getDefaultHost();

    void setDefaultHost(String host);

    void addServer(Server server);

    Server findServer(String name);

    Server[] findServers();

    void removeServer(Server server);

}
