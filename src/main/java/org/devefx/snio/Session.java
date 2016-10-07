package org.devefx.snio;

import org.devefx.snio.net.Sender;

import java.io.IOException;
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

    Sender getSender();

    void setSender(Sender sender);

    void addSessionListener(SessionListener listener);

    void removeSessionListener(SessionListener listener);

    Object getAttribute(String name);

    Enumeration<String> getAttributeNames();

    void setAttribute(String name, Object value);

    void removeAttribute(String name);

    void expire();

    void recycle();

}
