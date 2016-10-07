package org.devefx.snio;

public final class Message {

    private Object type;
    private Object content;

    public Message(Object type, Object content) {
        this.type = type;
        this.content = content;
    }

    public Object getType() {
        return type;
    }

    public Object getContent() {
        return content;
    }
}
