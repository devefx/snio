package org.devefx.snio;

public interface Lifecycle {
    String INIT_EVENT = "init";
    String START_EVENT = "start";
    String BEFORE_START_EVENT = "before_start";
    String AFTER_START_EVENT = "after_start";
    String STOP_EVENT = "stop";
    String BEFORE_STOP_EVENT = "before_stop";
    String AFTER_STOP_EVENT = "after_stop";
    String DESTROY_EVENT = "destroy";
    String PERIODIC_EVENT = "periodic";
    String SESSION_EXPIRED_EVENT = "session_expired";

    void addLifecycleListener(LifecycleListener listener);

    LifecycleListener[] findLifecycleListeners();

    void removeLifecycleListener(LifecycleListener listener);

    void start() throws LifecycleException;

    void stop() throws LifecycleException;
}
