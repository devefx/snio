package org.devefx.snio.session.redis;

import org.devefx.snio.Manager;
import org.devefx.snio.session.StandardSession;

import redis.clients.jedis.Jedis;

public class RedisSession extends StandardSession {
	protected boolean dirty;
	
    public RedisSession(Manager manager) {
        super(manager);
    }
    
    @Override
    public void setAttribute(String name, Object value) {
    	Object oldValue = getAttribute(name);
    	super.setAttribute(name, value);
    	
    	if ((value != null && oldValue != null) &&
    			(value == null && oldValue != null
    			|| value != null && oldValue == null
    			|| !value.getClass().isInstance(oldValue)
    			|| !value.equals(oldValue))) {
    		if (manager instanceof RedisSessionManager) {
    			((RedisSessionManager) manager).save(this, true);
    			resetDirtyTracking();
			}
		}
    }
    
    @Override
    public void removeAttribute(String name) {
    	if (getAttribute(name) != null) {
    		super.removeAttribute(name);
    		if (manager instanceof RedisSessionManager) {
    			((RedisSessionManager) manager).save(this, true);
    			resetDirtyTracking();
			}
		}
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
    
    public boolean isDirty() {
    	return dirty;
	}
    
    public void resetDirtyTracking() {
    	dirty = false;
	}
    
}
