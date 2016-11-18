package org.devefx.snio;

public interface Service {

    Object DEFAULT_TYPE = "__SERVICE_DEFAULT__";

    Object getType();

    void service(Request request, Manager manager);

}
