package org.devefx.snio;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.HashMap;
import java.util.Map;
import java.util.Queue;
import java.util.concurrent.ConcurrentLinkedDeque;

public class MessageDispatcher implements Runnable {
    private static final Logger log = LoggerFactory.getLogger(MessageDispatcher.class);
    private Queue<MessageEvent> eventQueue = new ConcurrentLinkedDeque<>();
    private Map<Object, Service> serviceMap = new HashMap<>(20);

    public MessageDispatcher(int capacity) {
        serviceMap = new HashMap<>(capacity);
    }

    public void addService(Service service) {
        serviceMap.put(service.getType(), service);
    }

    public void push(MessageEvent event) {
        synchronized (eventQueue) {
            eventQueue.add(event);
            eventQueue.notifyAll();
        }
    }

    public boolean isDone() {
        return eventQueue.isEmpty();
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
                MessageEvent event = null;
                while ((event = eventQueue.poll()) != null) {
                    Request request = event.getRequest();
                    request.getSession().access();
                    fireRequestEvent(request, event.getType());
                    request.getSession().endAccess();
                }
            }
        }
    }

    public void start() {
        Thread thread = new Thread(this, "MessageDispatcher");
        thread.setDaemon(true);
        thread.start();
    }

    private void fireRequestEvent(Request request, Object type) {
        synchronized (serviceMap) {
            Service service = serviceMap.get(type);
            if (service != null) {
            	if (log.isInfoEnabled()) {
                    log.info("[id: " + type + "] EXECUTE");
                }
                service.service(request);
            } else {
                if (log.isInfoEnabled()) {
                    log.info("[id: " + type + "] UNREGISTERED");
                }
            }
        }
    }
}
