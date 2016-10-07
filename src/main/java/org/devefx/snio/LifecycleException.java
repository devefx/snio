package org.devefx.snio;

public final class LifecycleException extends Exception {
	private static final long serialVersionUID = 9195109867348186889L;
	protected String message;
    protected Throwable throwable;

    public LifecycleException() {
        this((String)null, (Throwable)null);
    }

    public LifecycleException(String message) {
        this(message, (Throwable)null);
    }

    public LifecycleException(Throwable throwable) {
        this((String)null, throwable);
    }

    public LifecycleException(String message, Throwable throwable) {
        this.message = null;
        this.throwable = null;
        this.message = message;
        this.throwable = throwable;
    }

    public String getMessage() {
        return this.message;
    }

    public Throwable getThrowable() {
        return this.throwable;
    }

    public String toString() {
        StringBuffer sb = new StringBuffer("LifecycleException:  ");
        if(this.message != null) {
            sb.append(this.message);
            if(this.throwable != null) {
                sb.append(":  ");
            }
        }

        if(this.throwable != null) {
            sb.append(this.throwable.toString());
        }

        return sb.toString();
    }
}