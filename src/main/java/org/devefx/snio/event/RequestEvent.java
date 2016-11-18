package org.devefx.snio.event;

import org.devefx.snio.Request;

import java.util.EventObject;

public final class RequestEvent extends EventObject {

    private Request request;
    private Object type;

    public RequestEvent(Request request, Object type) {
        super(request);
        this.request = request;
        this.type = type;
    }

    public Request getRequest() {
        return request;
    }

    public Object getType() {
        return type;
    }
}
