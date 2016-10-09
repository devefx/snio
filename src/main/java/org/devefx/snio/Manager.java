package org.devefx.snio;

import java.beans.PropertyChangeListener;
import java.io.IOException;

public interface Manager {

    Container getContainer();

    void setContainer(Container container);

    String getInfo();

    int getMaxInactiveInterval();

    void setMaxInactiveInterval(int interval);

    int getSessionIdLength();

    void setSessionIdLength(int length);

    int getSessionCounter();

    void setSessionCounter(int counter);

    int getMaxActive();

    void setMaxActive(int maxActive);

    int getActiveSessions();

    int getExpiredSessions();

    void setExpiredSessions(int expiredSessions);

    int getRejectedSessions();

    void setRejectedSessions(int rejectedSessions);

    int getSessionMaxAliveTime();

    void setSessionMaxAliveTime(int maxAliveTime);

    int getSessionAverageAliveTime();

    void setSessionAverageAliveTime(int averageAliveTime);

    void add(Session session);

    void remove(Session session);

    void addPropertyChangeListener(PropertyChangeListener listener);

    void removePropertyChangeListener(PropertyChangeListener listener);

    Session createEmptySession();

    Session createSession(String id);

    Session findSession(String id) throws IOException;

    Session[] findSessions();
    
    String[] findSessionIds();

    void backgroundProcess();
}
