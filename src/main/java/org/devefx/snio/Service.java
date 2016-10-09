package org.devefx.snio;

public interface Service {

    Object getType();

    void service(Request request);

}
