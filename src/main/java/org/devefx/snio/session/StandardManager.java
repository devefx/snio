package org.devefx.snio.session;

import org.devefx.snio.LifecycleException;
import org.devefx.snio.Session;

public class StandardManager extends ManagerBase {
    protected int maxActiveSessions = -1;
    protected boolean started = false;
    protected int rejectedSessions = 0;

    @Override
    public String getName() {
        return "StandardManager";
    }

    @Override
    public String getInfo() {
        return "StandardManager/1.0";
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
    public void start() throws LifecycleException {
        if (!initialized) {
            init();
        }

        if (!started) {
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
