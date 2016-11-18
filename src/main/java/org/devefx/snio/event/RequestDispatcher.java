package org.devefx.snio.event;

import org.devefx.snio.*;
import org.devefx.snio.util.LifecycleSupport;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Queue;
import java.util.concurrent.ConcurrentLinkedDeque;

public class RequestDispatcher implements Lifecycle, Runnable {

    private static final Logger log = LoggerFactory.getLogger(RequestDispatcher.class);
    protected LifecycleSupport lifecycle = new LifecycleSupport(this);
    private Queue<RequestEvent> eventQueue = new ConcurrentLinkedDeque<>();
    private Thread thread;
    private Server server;

    public RequestDispatcher(Server server) {
        this.server = server;
    }

    public Server getServer() {
        return server;
    }

    public void push(RequestEvent event) {
        synchronized (eventQueue) {
            eventQueue.add(event);
            eventQueue.notifyAll();
        }
    }

    public boolean isDone() {
        return eventQueue.isEmpty();
    }

    @Override
    public void start() throws LifecycleException {
        thread = new Thread(this, "RequestDispatcher");
        thread.setDaemon(true);
        thread.start();
    }

    @Override
    public void stop() throws LifecycleException {
        try {
            thread.interrupt();
            thread.join();
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
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

    @Override
    public void run() {
        while (true) {
            synchronized (eventQueue) {
                if (isDone()) {
                    try {
                        eventQueue.wait();
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    }
                }
                RequestEvent event;
                while ((event = eventQueue.poll()) != null) {
                    Request request = event.getRequest();
                    request.getSession().access();
                    fireRequestEvent(request, event.getType());
                    request.getSession().endAccess();
                }
            }
        }
    }

    protected void fireRequestEvent(Request request, Object type) {
        Service service = server.findService(type);
        if (service != null) {
            if (log.isInfoEnabled()) {
                log.info("[id: " + type + "] EXECUTE");
            }
            try {
                service.service(request, server.getContainer().getManager());
            } catch (Exception e) {
                e.printStackTrace();
                log.error("service an exception occurred:" + service, e);
            }
        } else {
            if (log.isInfoEnabled()) {
                log.info("[id: " + type + "] UNREGISTERED");
            }
        }
    }

}
