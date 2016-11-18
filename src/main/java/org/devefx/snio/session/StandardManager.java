package org.devefx.snio.session;

import org.devefx.snio.Lifecycle;
import org.devefx.snio.LifecycleException;
import org.devefx.snio.LifecycleListener;
import org.devefx.snio.Session;
import org.devefx.snio.util.LifecycleSupport;

public class StandardManager extends ManagerBase implements Lifecycle {
    protected LifecycleSupport lifecycle = new LifecycleSupport(this);
    protected int maxActiveSessions = -1;
    protected boolean started = false;
    protected int rejectedSessions = 0;

    @Override
    public String getName() {
        return "StandardManager";
    }

    @Override
    public String getInfo() {
        return "StandardManager/1.1";
    }

    public int getMaxActiveSessions() {
        return this.maxActiveSessions;
    }

    public void setMaxActiveSessions(int max) {
        int oldMaxActiveSessions = maxActiveSessions;
        maxActiveSessions = max;
        support.firePropertyChange("maxActiveSessions", new Integer(oldMaxActiveSessions), new Integer(maxActiveSessions));
    }

    @Override
    public int getRejectedSessions() {
        return rejectedSessions;
    }

    @Override
    public void setRejectedSessions(int rejectedSessions) {
        this.rejectedSessions = rejectedSessions;
    }

    @Override
    public Session createSession(String sessionId) {
        if(maxActiveSessions >= 0 && sessions.size() >= maxActiveSessions) {
            ++rejectedSessions;
            throw new IllegalStateException(sm.getString("standardManager.createSession.ise"));
        }
        return super.createSession(sessionId);
    }

    @Override
    public void addLifecycleListener(LifecycleListener listener) {
        lifecycle.addLifecycleListener(listener);
    }

    @Override
    public LifecycleListener[] findLifecycleListeners() {
        return lifecycle.findLifecycleListeners();
    }

    @Override
    public void removeLifecycleListener(LifecycleListener listener) {
        lifecycle.removeLifecycleListener(listener);
    }

    @Override
    public void start() throws LifecycleException {
        if (!started) {
            init();
            lifecycle.fireLifecycleEvent(START_EVENT, null);
            started = true;
            if (log.isDebugEnabled()) {
                log.debug("Force random number initialization starting");
            }
            String dummy = generateSessionId();
            if (dummy != null && log.isDebugEnabled()) {
                log.debug("Force random number initialization completed");
            }
        }
    }

    @Override
    public void stop() throws LifecycleException {
        if(log.isDebugEnabled()) {
            log.debug("Stopping");
        }

        if(!started) {
            throw new IllegalStateException(sm.getString("standardManager.notStarted"));
        } else {
            lifecycle.fireLifecycleEvent(STOP_EVENT, null);
            started = false;

            Session[] sessions = findSessions();
            for(int i = 0; i < sessions.length; ++i) {
                Session session = sessions[i];
                try {
                    if(session.isValid()) {
                        session.expire();
                    }
                } catch (Throwable throwable) {

                } finally {
                    session.recycle();
                }
            }

            random = null;
            if (initialized) {
                destroy();
            }
        }
    }
}
