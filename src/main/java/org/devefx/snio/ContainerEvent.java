package org.devefx.snio;

import java.util.EventObject;

public class ContainerEvent extends EventObject {
	private static final long serialVersionUID = 1L;
	private Container container;
    private Object data;
    private String type;
    
    public ContainerEvent(Container container, String type, Object data) {
        super(container);
        this.container = container;
        this.type = type;
        this.data = data;
    }
    
    public Object getData() {
		return data;
	}
    
    public Container getContainer() {
		return container;
	}
    
    public String getType() {
		return type;
	}
    
    public String toString() {
        return "ContainerEvent[\'" + getContainer() + "\',\'" + getType() + "\',\'" + getData() + "\']";
    }
}
