package org.devefx.snio;

import java.util.EventObject;

public final class MessageEvent extends EventObject {
	private static final long serialVersionUID = 4851428124370964466L;
	
	private Request request;
    private Object type;

    public MessageEvent(Request request, Object type) {
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
