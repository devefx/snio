package org.devefx.snio.session;

import org.devefx.snio.*;
import org.devefx.snio.util.Enumerator;
import org.devefx.snio.util.StringManager;

import java.beans.PropertyChangeSupport;
import java.util.ArrayList;
import java.util.Enumeration;
import java.util.Iterator;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicInteger;

public class StandardSession implements Session {
    protected static final boolean ACTIVITY_CHECK;
    protected static final String[] EMPTY_ARRAY;
    protected Map<String, Object> attributes = new ConcurrentHashMap<>();
    protected transient String authType = null;
    protected long creationTime = 0L;
    protected transient volatile boolean expiring = false;

    protected String id = null;
    protected volatile long lastAccessedTime;
    protected transient ArrayList<SessionListener> listeners;
    protected transient Manager manager;
    protected transient Sender sender;
    protected int maxInactiveInterval;
    protected boolean isNew;
    protected volatile boolean isValid;
    protected static StringManager sm;

    protected transient PropertyChangeSupport support;
    protected volatile long thisAccessedTime;
    protected transient AtomicInteger accessCount;

    public StandardSession(Manager manager) {
        this.lastAccessedTime = this.creationTime;
        this.listeners = new ArrayList<>();
        this.maxInactiveInterval = -1;
        this.isNew = false;
        this.isValid = false;
        this.support = new PropertyChangeSupport(this);
        this.thisAccessedTime = this.creationTime;
        this.accessCount = null;
        this.manager = manager;
        if(ACTIVITY_CHECK) {
            this.accessCount = new AtomicInteger();
        }
    }

    public String getAuthType() {
        return authType;
    }

    public void setAuthType(String authType) {
        String oldAuthType = this.authType;
        this.authType = authType;
        this.support.firePropertyChange("authType", oldAuthType, this.authType);
    }

    @Override
    public long getCreationTime() {
        return creationTime;
    }

    @Override
    public void setCreationTime(long time) {
        this.creationTime = time;
        this.lastAccessedTime = time;
        this.thisAccessedTime = time;
    }

    @Override
    public String getId() {
        return id;
    }

    @Override
    public String getIdInternal() {
        return id;
    }

    @Override
    public void setId(String id) {
        if(this.id != null && this.manager != null) {
            this.manager.remove(this);
        }

        this.id = id;
        if(this.manager != null) {
            this.manager.add(this);
        }

        this.tellNew();
    }

    public void tellNew() {
        this.fireSessionEvent(SESSION_CREATED_EVENT, null);
    }

    @Override
    public String getInfo() {
        return "StandardSession/1.1";
    }

    @Override
    public long getLastAccessedTime() {
        if(!this.isValidInternal()) {
            throw new IllegalStateException(sm.getString("standardSession.getLastAccessedTime.ise"));
        } else {
            return this.lastAccessedTime;
        }
    }

    @Override
    public long getLastAccessedTimeInternal() {
        return lastAccessedTime;
    }

    @Override
    public Manager getManager() {
        return manager;
    }

    @Override
    public void setManager(Manager manager) {
        this.manager = manager;
    }

    @Override
    public Sender getSender() {
        return sender;
    }

    @Override
    public void setSender(Sender sender) {
        this.sender = sender;
    }

    @Override
    public int getMaxInactiveInterval() {
        return maxInactiveInterval;
    }

    @Override
    public void setMaxInactiveInterval(int interval) {
        this.maxInactiveInterval = interval;
        if(this.isValid && interval == 0) {
            this.expire();
        }
    }

    @Override
    public boolean isNew() {
        return isNew;
    }

    @Override
    public void setNew(boolean isNew) {
        this.isNew = isNew;
    }

    @Override
    public void setValid(boolean valid) {
        this.isValid = valid;
    }

    @Override
    public boolean isValid() {
        if(this.expiring) {
            return true;
        } else if(!this.isValid) {
            return false;
        } else if(ACTIVITY_CHECK && this.accessCount.get() > 0) {
            return true;
        } else {
            if(this.maxInactiveInterval >= 0) {
                long timeNow = System.currentTimeMillis();
                int timeIdle = (int)((timeNow - this.thisAccessedTime) / 1000L);
                if(timeIdle >= this.maxInactiveInterval) {
                    this.expire(true);
                }
            }
            return this.isValid;
        }
    }

    protected boolean isValidInternal() {
        return this.isValid || this.expiring;
    }

    @Override
    public void invalidate() {
        if(!this.isValidInternal()) {
            throw new IllegalStateException(sm.getString("standardSession.invalidate.ise"));
        } else {
            this.expire();
        }
    }

    @Override
    public void access() {
        this.lastAccessedTime = this.thisAccessedTime;
        this.thisAccessedTime = System.currentTimeMillis();
        if(ACTIVITY_CHECK) {
            this.accessCount.incrementAndGet();
        }
    }

    @Override
    public void endAccess() {
        this.isNew = false;
        if(ACTIVITY_CHECK) {
            this.accessCount.decrementAndGet();
        }
    }

    @Override
    public void addSessionListener(SessionListener listener) {
        listeners.add(listener);
    }

    @Override
    public void removeSessionListener(SessionListener listener) {
        listeners.remove(listener);
    }

    @Override
    public Object getAttribute(String name) {
        if(!this.isValidInternal()) {
            throw new IllegalStateException(sm.getString("standardSession.getAttribute.ise"));
        } else {
            return name == null ? null : this.attributes.get(name);
        }
    }

    @Override
    public Enumeration<String> getAttributeNames() {
        if(!this.isValidInternal()) {
            throw new IllegalStateException(sm.getString("standardSession.getAttributeNames.ise"));
        } else {
            return new Enumerator<String>(this.attributes.keySet(), true);
        }
    }

    @Override
    public void setAttribute(String name, Object value) {
        this.setAttribute(name, value, true);
    }

    public void setAttribute(String name, Object value, boolean notify) {
        if(name == null) {
            throw new IllegalArgumentException(sm.getString("standardSession.setAttribute.namenull"));
        } else if(value == null) {
            this.removeAttribute(name);
        } else if(!this.isValidInternal()) {
            throw new IllegalStateException(sm.getString("standardSession.setAttribute.ise"));
        } else {
            Object unbound = attributes.put(name, value);
            if (notify) {
                if (unbound != null) {
                    fireSessionEvent("sessionAttributeReplaced", null);
                } else {
                    fireSessionEvent("sessionAttributeAdded", null);
                }
            }
        }
    }

    @Override
    public void removeAttribute(String name) {
        this.removeAttribute(name, true);
    }

    public void removeAttribute(String name, boolean notify) {
        if(!this.isValidInternal()) {
            throw new IllegalStateException(sm.getString("standardSession.removeAttribute.ise"));
        } else {
            this.removeAttributeInternal(name, notify);
        }
    }

    public void removeAttributeInternal(String name, boolean notify) {
        if(name != null) {
            Object value = this.attributes.remove(name);
            if (notify && value != null) {
                fireSessionEvent("sessionAttributeRemoved", null);
            }
        }
    }

    @Override
    public void expire() {
        expire(true);
    }

    public void expire(boolean notify) {
        if(!this.expiring && this.isValid && manager != null) {
            synchronized (this) {
                this.expiring = true;

                if(ACTIVITY_CHECK) {
                    this.accessCount.set(0);
                }

                this.setValid(false);
                long time = System.currentTimeMillis();
                int timeAlive = (int)((time - this.creationTime) / 1000L);
                int expired;
                synchronized(this.manager) {
                    if(timeAlive > this.manager.getSessionMaxAliveTime()) {
                        this.manager.setSessionMaxAliveTime(timeAlive);
                    }
                    expired = this.manager.getExpiredSessions();
                    ++expired;
                    this.manager.setExpiredSessions(expired);
                    int average = this.manager.getSessionAverageAliveTime();
                    average = (average * (expired - 1) + timeAlive) / expired;
                    this.manager.setSessionAverageAliveTime(average);
                }

                this.manager.remove(this);
                if(notify) {
                    this.fireSessionEvent(SESSION_DESTROYED_EVENT, null);
                }

                this.expiring = false;
                String[] keys = this.keys();

                for(int i = 0; i < keys.length; ++i) {
                    this.removeAttributeInternal(keys[i], notify);
                }

                sender.close();
            }
        }
    }

    @Override
    public void recycle() {
        this.attributes.clear();
        this.setAuthType(null);
        this.creationTime = 0L;
        this.expiring = false;
        this.id = null;
        this.lastAccessedTime = 0L;
        this.maxInactiveInterval = -1;
        this.isNew = false;
        this.isValid = false;
        this.manager = null;
    }

    public void fireSessionEvent(String type, Object data) {
        if (!listeners.isEmpty()) {
            SessionEvent event = new SessionEvent(this, type, data);
            Iterator<SessionListener> it = listeners.iterator();
            while (it.hasNext()) {
                it.next().sessionEvent(event);
            }
        }
    }

    protected String[] keys() {
        return this.attributes.keySet().toArray(EMPTY_ARRAY);
    }

    public String toString() {
        StringBuffer sb = new StringBuffer();
        sb.append("StandardSession[");
        sb.append(this.id);
        sb.append("]");
        return sb.toString();
    }

    static {
        ACTIVITY_CHECK = Boolean.valueOf(System.getProperty("org.devefx.snio.session.StandardSession.ACTIVITY_CHECK", "false")).booleanValue();
        EMPTY_ARRAY = new String[0];
        sm = StringManager.getManager("org.devefx.snio.session");
    }
}
