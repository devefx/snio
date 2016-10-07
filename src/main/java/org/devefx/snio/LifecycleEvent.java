package org.devefx.snio;

import java.util.EventObject;

public final class LifecycleEvent extends EventObject {
	private static final long serialVersionUID = 1L;
	private Object data;
    private Lifecycle lifecycle;
    private String type;
    
    public LifecycleEvent(Lifecycle lifecycle, String type) {
        this(lifecycle, type, (Object)null);
    }

    public LifecycleEvent(Lifecycle lifecycle, String type, Object data) {
    	super(lifecycle);
        this.data = null;
        this.lifecycle = null;
        this.type = null;
        this.lifecycle = lifecycle;
        this.type = type;
        this.data = data;
    }
    
    public Object getData() {
		return data;
	}
    
    public Lifecycle getLifecycle() {
		return lifecycle;
	}
    
    public String getType() {
		return type;
	}
}
