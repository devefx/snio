package org.devefx.snio;

public interface Engine extends Container    {

    String getHost();

    void setHost(String host);

    void addServer(Server server);

    Server[] findServers();

    void removeServer(Server server);

}
