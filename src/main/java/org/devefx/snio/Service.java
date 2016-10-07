package org.devefx.snio;

public interface Service {

    <T> T getType();

    void service(Request request);

}
