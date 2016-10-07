package org.devefx.snio.session;

import org.devefx.snio.Container;
import org.devefx.snio.Manager;
import org.devefx.snio.Session;
import org.devefx.snio.util.StringManager;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.beans.PropertyChangeListener;
import java.beans.PropertyChangeSupport;
import java.io.DataInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.Map;
import java.util.Random;
import java.util.concurrent.ConcurrentHashMap;

public abstract class ManagerBase implements Manager {
    protected Logger log = LoggerFactory.getLogger(ManagerBase.class);
    protected DataInputStream randomIS = null;
    protected String devRandomSource = "/dev/urandom";
    protected static final String DEFAULT_ALGORITHM = "MD5";
    protected String algorithm = "MD5";
    protected MessageDigest digest = null;
    protected Container container;
    protected int maxInactiveInterval = 60;
    protected int sessionIdLength = 16;
    protected Random random = null;
    protected String randomClass = "java.security.SecureRandom";
    protected int sessionMaxAliveTime;
    protected int sessionAverageAliveTime;
    protected int expiredSessions = 0;
    protected Map<String, Session> sessions = new ConcurrentHashMap<>();
    protected int sessionCounter = 0;
    protected int maxActive = 0;
    protected int duplicates = 0;
    protected boolean initialized = false;
    protected long processingTime = 0L;
    private int count = 0;
    protected int processExpiresFrequency = 6;
    protected StringManager sm = StringManager.getManager("org.devefx.snio.session");
    protected PropertyChangeSupport support = new PropertyChangeSupport(this);

    @Override
    public Container getContainer() {
        return container;
    }

    @Override
    public void setContainer(Container container) {
        Container oldContainer = this.container;
        this.container = container;
        support.firePropertyChange("container", oldContainer, container);
    }

    @Override
    public String getInfo() {
        return "ManagerBase/1.0";
    }

    @Override
    public int getMaxInactiveInterval() {
        return maxInactiveInterval;
    }

    @Override
    public void setMaxInactiveInterval(int interval) {
        int oldMaxInactiveInterval = maxInactiveInterval;
        maxInactiveInterval = interval;
        support.firePropertyChange("maxInactiveInterval", new Integer(oldMaxInactiveInterval), new Integer(maxInactiveInterval));
    }

    @Override
    public int getSessionIdLength() {
        return sessionIdLength;
    }

    @Override
    public void setSessionIdLength(int length) {
        int oldSessionIdLength = sessionIdLength;
        sessionIdLength = length;
        support.firePropertyChange("sessionIdLength", new Integer(oldSessionIdLength), new Integer(sessionIdLength));
    }

    @Override
    public int getSessionCounter() {
        return sessionCounter;
    }

    @Override
    public void setSessionCounter(int counter) {
        this.sessionCounter = counter;
    }

    public int getDuplicates() {
        return duplicates;
    }

    public void setDuplicates(int duplicates) {
        this.duplicates = duplicates;
    }

    @Override
    public int getMaxActive() {
        return maxActive;
    }

    @Override
    public void setMaxActive(int maxActive) {
        this.maxActive = maxActive;
    }

    @Override
    public int getActiveSessions() {
        return sessions.size();
    }

    @Override
    public int getExpiredSessions() {
        return expiredSessions;
    }

    @Override
    public void setExpiredSessions(int expiredSessions) {
        this.expiredSessions = expiredSessions;
    }

    @Override
    public int getSessionMaxAliveTime() {
        return sessionMaxAliveTime;
    }

    @Override
    public void setSessionMaxAliveTime(int maxAliveTime) {
        this.sessionMaxAliveTime = maxAliveTime;
    }

    @Override
    public int getSessionAverageAliveTime() {
        return sessionAverageAliveTime;
    }

    @Override
    public void setSessionAverageAliveTime(int averageAliveTime) {
        this.sessionAverageAliveTime = averageAliveTime;
    }

    @Override
    public void add(Session session) {
        sessions.put(session.getIdInternal(), session);
        int size = sessions.size();
        if(size > maxActive) {
            maxActive = size;
        }
    }

    @Override
    public void remove(Session session) {
        sessions.remove(session.getIdInternal());
    }

    @Override
    public void addPropertyChangeListener(PropertyChangeListener listener) {
        support.addPropertyChangeListener(listener);
    }

    @Override
    public void removePropertyChangeListener(PropertyChangeListener listener) {
        support.removePropertyChangeListener(listener);
    }

    @Override
    public Session createEmptySession() {
        return new StandardSession(this);
    }

    @Override
    public Session createSession(String sessionId) {
        Session session = this.createEmptySession();
        session.setNew(true);
        session.setValid(true);
        session.setCreationTime(System.currentTimeMillis());
        session.setMaxInactiveInterval(maxInactiveInterval);
        if(sessionId == null) {
            sessionId = generateSessionId();
        }
        session.setId(sessionId);
        ++sessionCounter;
        return session;
    }

    @Override
    public Session findSession(String id) throws IOException {
        return id == null ? null : sessions.get(id);
    }

    @Override
    public Session[] findSessions() {
        return sessions.values().toArray(new Session[0]);
    }

    @Override
    public void backgroundProcess() {
        count = (count + 1) % processExpiresFrequency;
        if(count == 0) {
            processExpires();
        }
    }

    public void processExpires() {
        long timeNow = System.currentTimeMillis();
        Session[] sessions = findSessions();
        int expireHere = 0;
        if(log.isDebugEnabled()) {
            log.debug("Start expire sessions " + getName() + " at " + timeNow + " sessioncount " + sessions.length);
        }

        for(int timeEnd = 0; timeEnd < sessions.length; ++timeEnd) {
            if(sessions[timeEnd] != null && !sessions[timeEnd].isValid()) {
                ++expireHere;
            }
        }

        long t2 = System.currentTimeMillis();
        if(log.isDebugEnabled()) {
            log.debug("End expire sessions " + getName() + " processingTime " + (t2 - timeNow) + " expired sessions: " + expireHere);
        }

        processingTime += t2 - timeNow;
    }

    public void init() {
        if (!initialized) {
            initialized = true;
            getRandomBytes(new byte[16]);
            if(log.isDebugEnabled()) {
                log.debug("Registering");
            }
        }
    }

    public void destroy() {
        if (randomIS != null) {
            try {
                randomIS.close();
            } catch (IOException e) {
                log.warn("Failed to close randomIS.");
            }
            randomIS = null;
        }

        initialized = false;
    }

    public String getName() {
        return "ManagerBase";
    }

    public String getAlgorithm() {
        return algorithm;
    }

    public void setAlgorithm(String algorithm) {
        String oldAlgorithm = this.algorithm;
        this.algorithm = algorithm;
        support.firePropertyChange("algorithm", oldAlgorithm, algorithm);
    }

    public long getProcessingTime() {
        return processingTime;
    }

    public void setProcessingTime(long processingTime) {
        this.processingTime = processingTime;
    }

    public int getProcessExpiresFrequency() {
        return processExpiresFrequency;
    }

    public void setProcessExpiresFrequency(int processExpiresFrequency) {
        if(processExpiresFrequency > 0) {
            int oldProcessExpiresFrequency = this.processExpiresFrequency;
            this.processExpiresFrequency = processExpiresFrequency;
            support.firePropertyChange("processExpiresFrequency", new Integer(oldProcessExpiresFrequency), new Integer(processExpiresFrequency));
        }
    }

    public String getRandomClass() {
        return randomClass;
    }

    public void setRandomClass(String randomClass) {
        String oldRandomClass = this.randomClass;
        this.randomClass = randomClass;
        support.firePropertyChange("randomClass", oldRandomClass, randomClass);
    }

    public synchronized MessageDigest getDigest() {
        if (digest == null) {
            long t1 = System.currentTimeMillis();
            if (log.isDebugEnabled()) {
                log.debug(sm.getString("managerBase.getting", algorithm));
            }
            try {
                digest = MessageDigest.getInstance(algorithm);
            } catch (NoSuchAlgorithmException e) {
                log.error(sm.getString("managerBase.digest", algorithm), e);
                try {
                    digest = MessageDigest.getInstance(DEFAULT_ALGORITHM);
                } catch (NoSuchAlgorithmException ex) {
                    log.error(sm.getString("managerBase.digest", DEFAULT_ALGORITHM), ex);
                    digest = null;
                }
            }

            if(log.isDebugEnabled()) {
                log.debug(sm.getString("managerBase.gotten"));
            }

            long t2 = System.currentTimeMillis();
            if(log.isDebugEnabled()) {
                log.debug("getDigest() " + (t2 - t1));
            }
        }
        return digest;
    }

    public void setRandomFile(String s) {
        try {
            this.devRandomSource = s;
            File file = new File(s);
            if(!file.exists()) {
                return;
            }
            randomIS = new DataInputStream(new FileInputStream(file));
            randomIS.readLong();
            if(log.isDebugEnabled()) {
                log.debug("Opening " + devRandomSource);
            }
        } catch (IOException e) {
            log.warn("Error reading " + devRandomSource, e);
            if(randomIS != null) {
                try {
                    randomIS.close();
                } catch (Exception var4) {
                    log.warn("Failed to close randomIS.");
                }
            }
            devRandomSource = null;
            randomIS = null;
        }
    }

    public String getRandomFile() {
        return devRandomSource;
    }

    public Random getRandom() {
        if (random == null) {
            long seed = System.currentTimeMillis();
            long t1 = seed;

            try {
                Class<?> clazz = Class.forName(randomClass);
                random = (Random) clazz.newInstance();
                random.setSeed(seed);
            } catch (Exception e) {
                log.error(sm.getString("managerBase.random", this.randomClass), e);
                random = new Random();
                random.setSeed(seed);
            }

            if (log.isDebugEnabled()) {
                long t2 = System.currentTimeMillis();
                if (t2 - t1 > 100L) {
                    log.debug(sm.getString("managerBase.seeding", randomClass) + " " + (t2 - t1));
                }
            }
        }
        return random;
    }

    protected void getRandomBytes(byte[] bytes) {
        if (devRandomSource != null && randomIS == null) {
            setRandomFile(devRandomSource);
        }
        if (randomIS != null) {
            try {
                int e = randomIS.read(bytes);
                if(e == bytes.length) {
                    return;
                }
                if(log.isDebugEnabled()) {
                    log.debug("Got " + e + " " + bytes.length);
                }
            } catch (Exception e) {
            }

            devRandomSource = null;

            try {
                randomIS.close();
            } catch (Exception e) {
                log.warn("Failed to close randomIS.");
            }
            randomIS = null;
        }
        getRandom().nextBytes(bytes);
    }

    protected synchronized String generateSessionId() {
        byte[] random = new byte[16];
        String result = null;
        StringBuffer buffer = new StringBuffer(sessionIdLength);

        do {
            int resultLenBytes = 0;
            if (result != null) {
                buffer = new StringBuffer();
                ++duplicates;
            }
            while (resultLenBytes < sessionIdLength) {
                getRandomBytes(random);
                random = getDigest().digest(random);

                for (int i = 0; i < random.length && resultLenBytes < sessionIdLength; i++) {
                    byte b1 = (byte)((random[i] & 240) >> 4);
                    byte b2 = (byte)(random[i] & 15);
                    if(b1 < 10) {
                        buffer.append((char)(48 + b1));
                    } else {
                        buffer.append((char)(65 + (b1 - 10)));
                    }
                    if(b2 < 10) {
                        buffer.append((char)(48 + b2));
                    } else {
                        buffer.append((char)(65 + (b2 - 10)));
                    }
                    ++resultLenBytes;
                }

                result = buffer.toString();
            }
        } while (sessions.containsKey(result));

        return result;
    }
}
