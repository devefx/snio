package org.devefx.snio.net;

import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

import org.devefx.snio.Context;
import org.devefx.snio.Manager;
import org.devefx.snio.codec.ByteArrayDecoder;
import org.devefx.snio.codec.MessageToMessageDecoder;

public class NetContext implements Context {
	private static MessageToMessageDecoder defaultDecoder;
    private Map<String, Sender> senderMap = new HashMap<>(100);
	private MessageToMessageDecoder decoder;
	private Manager manager;

	public MessageToMessageDecoder getDecoder() {
		if (decoder == null) {
			return getDefaultDecoder();
		}
		return decoder;
	}
	public void setDecoder(MessageToMessageDecoder decoder) {
		this.decoder = decoder;
	}
	
	public Manager getManager() {
		return manager;
	}
	
	public void setManager(Manager manager) {
		this.manager = manager;
	}

	public Sender findSender(String sessionId) {
		return senderMap.get(sessionId);
	}
	
	public Iterator<Sender> senders() {
		return senderMap.values().iterator();
	}

	protected void addSender(String sessionId, Sender sender) {
		senderMap.put(sessionId, sender);
	}
	
	protected static MessageToMessageDecoder getDefaultDecoder() {
		if (defaultDecoder == null) {
			defaultDecoder = new ByteArrayDecoder();
		}
		return defaultDecoder;
	}
}
