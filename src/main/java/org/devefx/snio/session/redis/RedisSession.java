package org.devefx.snio.session.redis;

import org.devefx.snio.Manager;
import org.devefx.snio.session.StandardSession;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import redis.clients.jedis.Jedis;

import java.io.IOException;
import java.util.HashMap;

public class RedisSession extends StandardSession {

    private final Logger log = LoggerFactory.getLogger(RedisSession.class);

    protected static boolean manualDirtyTrackingSupportEnabled = false;

    public static void setManualDirtyTrackingSupportEnabled(boolean enabled) {
        manualDirtyTrackingSupportEnabled = enabled;
    }

    protected static String manualDirtyTrackingAttributeKey = "__changed__";

    public static void setManualDirtyTrackingAttributeKey(String key) {
        manualDirtyTrackingAttributeKey = key;
    }

    protected HashMap<String, Object> changedAttributes;
    protected boolean dirty;

    public RedisSession(Manager manager) {
        super(manager);
        resetDirtyTracking();
    }

    public boolean isDirty() {
        return dirty || !changedAttributes.isEmpty();
    }

    public HashMap<String, Object> getChangedAttributes() {
        return changedAttributes;
    }

    public void resetDirtyTracking() {
        changedAttributes = new HashMap<>();
        dirty = false;
    }

    @Override
    public Object getAttribute(String name) {
        return super.getAttribute(name);
    }

    @Override
    public void setAttribute(String key, Object value) {
        if (manualDirtyTrackingSupportEnabled && manualDirtyTrackingAttributeKey.equals(key)) {
            dirty = true;
            return;
        }

        Object oldValue = getAttribute(key);
        super.setAttribute(key, value);

        if ( (value != null || oldValue != null)
                && ( value == null && oldValue != null
                || oldValue == null && value != null
                || !value.getClass().isInstance(oldValue)
                || !value.equals(oldValue) ) ) {
            if (this.manager instanceof RedisSessionManager
                    && ((RedisSessionManager)this.manager).getSaveOnChange()) {
                try {
                    ((RedisSessionManager)this.manager).save(this, true);
                } catch (IOException ex) {
                    log.error("Error saving session on setAttribute (triggered by saveOnChange=true): " + ex.getMessage());
                }
            } else {
                changedAttributes.put(key, value);
            }
        }
    }

    @Override
    public void removeAttribute(String name) {
        super.removeAttribute(name);
        if (this.manager instanceof RedisSessionManager
                && ((RedisSessionManager)this.manager).getSaveOnChange()) {
            try {
                ((RedisSessionManager)this.manager).save(this, true);
            } catch (IOException ex) {
                log.error("Error saving session on setAttribute (triggered by saveOnChange=true): " + ex.getMessage());
            }
        } else {
            dirty = true;
        }
    }

    @Override
    public void setId(String id) {
        this.id = id;
    }

    @Override
    public void access() {
        if (getMaxInactiveInterval() > 0) {
            Jedis jedis = null;
            try {
                jedis = ((RedisSessionManager) manager).acquireConnection();
                jedis.expire(getId().getBytes(), getMaxInactiveInterval());
            } finally {
                if (jedis != null) {
                    jedis.close();
                }
            }
        }
    }

}
