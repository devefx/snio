package org.devefx.snio;

import java.util.EventObject;

public final class SessionEvent extends EventObject {
	private static final long serialVersionUID = 1L;
	private Object data;
	private Session session;
	private String type;
	
	public SessionEvent(Session session, String type, Object data) {
		super(session);
		this.session = session;
		this.type = type;
		this.data = data;
	}
	
	public Object getData() {
		return data;
	}
	
	public Session getSession() {
		return session;
	}
	
	public String getType() {
		return type;
	}
	
	@Override
	public String toString() {
		return "SessionEvent[\'" + getSession() + "\',\'" + getType() + "\']";
	}
}
