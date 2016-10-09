package org.devefx.snio;

import java.security.Principal;
import java.util.Enumeration;

public interface Session {

    long getCreationTime();

    void setCreationTime(long time);

    String getId();

    String getIdInternal();

    void setId(String id);

    String getInfo();

    long getLastAccessedTime();

    long getLastAccessedTimeInternal();

    Manager getManager();

    void setManager(Manager manager);

    int getMaxInactiveInterval();

    void setMaxInactiveInterval(int interval);

    boolean isNew();

    void setNew(boolean isNew);

    Principal getPrincipal();

    void setPrincipal(Principal principal);

    void setValid(boolean valid);

    boolean isValid();

    void invalidate();

    void access();

    void endAccess();

    void addSessionListener(SessionListener listener);

    void removeSessionListener(SessionListener listener);

    Object getAttribute(String name);

    Enumeration<String> getAttributeNames();

    void setAttribute(String name, Object value);

    void removeAttribute(String name);

    void expire();

    void recycle();

}
