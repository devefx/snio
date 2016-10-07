package org.devefx.snio.session;

import org.devefx.snio.Manager;
import org.devefx.snio.Session;
import org.devefx.snio.SessionEvent;
import org.devefx.snio.SessionListener;
import org.devefx.snio.net.Sender;
import org.devefx.snio.util.Enumerator;
import org.devefx.snio.util.StringManager;

import java.beans.PropertyChangeSupport;
import java.security.Principal;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicInteger;

public class StandardSession implements Session {
    protected static final boolean ACTIVITY_CHECK;
    protected static final String[] EMPTY_ARRAY;
    protected Map<String, Object> attributes = new ConcurrentHashMap<>();
    protected long creationTime = 0L;
    protected transient volatile boolean expiring = false;

    protected String id = null;
    protected volatile long lastAccessedTime;
    protected transient ArrayList<SessionListener> listeners;
    protected transient Manager manager;
    protected int maxInactiveInterval;
    protected boolean isNew;
    protected volatile boolean isValid;
    protected transient Principal principal;
    protected static StringManager sm;
    protected transient PropertyChangeSupport support;
    protected volatile long thisAccessedTime;
    protected transient AtomicInteger accessCount;
    protected transient Sender sender;

    public StandardSession(Manager manager) {
        this.lastAccessedTime = this.creationTime;
        this.listeners = new ArrayList<>();
        this.manager = null;
        this.maxInactiveInterval = -1;
        this.isNew = false;
        this.isValid = false;
        this.principal = null;
        this.support = new PropertyChangeSupport(this);
        this.thisAccessedTime = this.creationTime;
        this.accessCount = null;
        this.manager = manager;
        if (ACTIVITY_CHECK) {
            this.accessCount = new AtomicInteger();
        }
    }

    @Override
    public long getCreationTime() {
        return creationTime;
    }

    @Override
    public void setCreationTime(long time) {
        creationTime = time;
        lastAccessedTime = time;
        thisAccessedTime = time;
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
        if(this.id != null && manager != null) {
            manager.remove(this);
        }
        this.id = id;
        if (manager != null) {
            manager.add(this);
        }
        fireSessionEvent("createSession", null);
    }

    @Override
    public String getInfo() {
        return "StandardSession/1.0";
    }

    @Override
    public long getLastAccessedTime() {
        if (!isValidInternal()) {
            throw new IllegalStateException(sm.getString("standardSession.getLastAccessedTime.ise"));
        }
        return lastAccessedTime;
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
    public int getMaxInactiveInterval() {
        return maxInactiveInterval;
    }

    @Override
    public void setMaxInactiveInterval(int interval) {
        this.maxInactiveInterval = interval;
        if (isValid && interval == 0) {
            expire();
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
    public Principal getPrincipal() {
        return principal;
    }

    @Override
    public void setPrincipal(Principal principal) {
        Principal oldPrincipal = this.principal;
        this.principal = principal;
        this.support.firePropertyChange("principal", oldPrincipal, this.principal);
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
        } else {
            if(this.maxInactiveInterval >= 0) {
                long timeNow = System.currentTimeMillis();
                int timeIdle = (int)((timeNow - this.thisAccessedTime) / 1000L);
                if(timeIdle >= this.maxInactiveInterval) {
                    expire();
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
        if (!isValidInternal()) {
            throw new IllegalStateException(sm.getString("standardSession.invalidate.ise"));
        }
        expire();
    }

    @Override
    public void access() {
        lastAccessedTime = thisAccessedTime;
        thisAccessedTime = System.currentTimeMillis();
        if(ACTIVITY_CHECK) {
            accessCount.incrementAndGet();
        }
    }

    @Override
    public void endAccess() {
        isNew = false;
        if (ACTIVITY_CHECK) {
            accessCount.decrementAndGet();
        }
    }

    @Override
    public void setSender(Sender sender) {
        this.sender = sender;
    }

    @Override
    public Sender getSender() {
        return sender;
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
        if (!isValidInternal()) {
            throw new IllegalStateException(sm.getString("standardSession.getAttributeNames.ise"));
        }
        return name == null ? null : attributes.get(name);
    }

    @Override
    public Enumeration<String> getAttributeNames() {
        if (!isValidInternal()) {
            throw new IllegalStateException(sm.getString("standardSession.getAttributeNames.ise"));
        }
        return new Enumerator<String>(attributes.keySet(), false);
    }

    @Override
    public void setAttribute(String name, Object value) {
        setAttribute(name, value, true);
    }

    public void setAttribute(String name, Object value, boolean notify) {
        if(name == null) {
            throw new IllegalArgumentException(sm.getString("standardSession.setAttribute.namenull"));
        } else if(value == null) {
            removeAttribute(name);
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
        removeAttribute(name, true);
    }

    public void removeAttribute(String name, boolean notify) {
        if(!isValidInternal()) {
            throw new IllegalStateException(sm.getString("standardSession.removeAttribute.ise"));
        } else {
            if(name != null) {
                Object value = this.attributes.remove(name);
                if (notify && value != null) {
                    fireSessionEvent("sessionAttributeRemoved", null);
                }
            }
        }
    }

    @Override
    public void expire() {
        expire(true);
    }

    public void expire(boolean notify) {
        synchronized (this) {
            if (!expiring && isValid && manager != null) {
                expiring = true;

                if (ACTIVITY_CHECK) {
                    accessCount.set(0);
                }

                setValid(false);
                long t1 = System.currentTimeMillis();
                int timeAlive = (int) ((t1 - creationTime) / 1000L);
                int expired = 0;
                synchronized (manager) {
                    if (timeAlive > manager.getSessionMaxAliveTime()) {
                        manager.setSessionMaxAliveTime(timeAlive);
                    }
                    expired = manager.getExpiredSessions();
                    expired++;
                    manager.setExpiredSessions(expired);
                    int average = manager.getSessionAverageAliveTime();
                    average = (average * (expired - 1) + timeAlive) / expired;
                    manager.setSessionAverageAliveTime(average);
                }

                manager.remove(this);
                if (notify) {
                    fireSessionEvent("destroySession", null);
                }

                expiring = false;
                for (String key : attributes.keySet()) {
                    removeAttribute(key, notify);
                }
            }

        }
    }

    @Override
    public void recycle() {
        this.attributes.clear();
        this.creationTime = 0L;
        this.expiring = false;
        this.id = null;
        this.lastAccessedTime = 0L;
        this.maxInactiveInterval = -1;
        this.setPrincipal(null);
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

    static {
        ACTIVITY_CHECK = Boolean.valueOf(System.getProperty("org.devefx.snio.session.StandardSession.ACTIVITY_CHECK", "false")).booleanValue();
        EMPTY_ARRAY = new String[0];
        sm = StringManager.getManager("org.devefx.snio.session");
    }
}
