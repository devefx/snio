package org.devefx.snio.session.redis;

import com.esotericsoftware.kryo.Kryo;
import com.esotericsoftware.kryo.io.Input;
import com.esotericsoftware.kryo.io.Output;
import org.devefx.snio.*;
import org.devefx.snio.session.StandardManager;
import org.devefx.snio.util.LifecycleSupport;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import redis.clients.jedis.*;
import redis.clients.util.Pool;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

public class RedisSessionManager extends StandardManager {

    enum SessionPersistPolicy {
        DEFAULT,
        SAVE_ON_CHANGE,
        ALWAYS_SAVE_AFTER_REQUEST;

        static SessionPersistPolicy fromName(String name) {
            for (SessionPersistPolicy policy : SessionPersistPolicy.values()) {
                if (policy.name().equalsIgnoreCase(name)) {
                    return policy;
                }
            }
            throw new IllegalArgumentException("Invalid session persist policy [" + name + "]. Must be one of " + Arrays.asList(SessionPersistPolicy.values())+ ".");
        }
    }

    private static final Logger log = LoggerFactory.getLogger(RedisSessionManager.class);

    protected byte[] NULL_SESSION = "null".getBytes();
    protected String host = Protocol.DEFAULT_HOST;
    protected int port = Protocol.DEFAULT_PORT;
    protected int database = Protocol.DEFAULT_DATABASE;
    protected String password = null;
    protected int timeout = Protocol.DEFAULT_TIMEOUT;
    protected String sentinelMaster = null;
    protected Set<String> sentinelSet = null;

    protected Pool<Jedis> connectionPool;
    protected JedisPoolConfig connectionPoolConfig = new JedisPoolConfig();

    protected ThreadLocal<RedisSession> currentSession = new ThreadLocal<>();
    protected ThreadLocal<String> currentSessionId = new ThreadLocal<>();
    protected ThreadLocal<Boolean> currentSessionIsPersisted = new ThreadLocal<>();

    protected static final Kryo kryo;

    protected EnumSet<SessionPersistPolicy> sessionPersistPoliciesSet = EnumSet.of(SessionPersistPolicy.DEFAULT);

    protected int maxActiveSessions = -1;
    protected int rejectedSessions = 0;
    protected Map<String, Sender> senders = new ConcurrentHashMap<>();
    /**
     * The lifecycle event support for this component.
     */
    protected LifecycleSupport lifecycle = new LifecycleSupport(this);

    @Override
    public String getName() {
        return "RedisSessionManager";
    }

    @Override
    public String getInfo() {
        return "RedisSessionManager/1.1";
    }

    public String getHost() {
        return host;
    }

    public void setHost(String host) {
        this.host = host;
    }

    public int getPort() {
        return port;
    }

    public void setPort(int port) {
        this.port = port;
    }

    public int getDatabase() {
        return database;
    }

    public void setDatabase(int database) {
        this.database = database;
    }

    public String getPassword() {
        return password;
    }

    public void setPassword(String password) {
        this.password = password;
    }

    public int getTimeout() {
        return timeout;
    }

    public void setTimeout(int timeout) {
        this.timeout = timeout;
    }

    public String getSessionPersistPolicies() {
        StringBuilder policies = new StringBuilder();
        for (Iterator<SessionPersistPolicy> iter = this.sessionPersistPoliciesSet.iterator(); iter.hasNext();) {
            SessionPersistPolicy policy = iter.next();
            policies.append(policy.name());
            if (iter.hasNext()) {
                policies.append(",");
            }
        }
        return policies.toString();
    }

    public void setSessionPersistPolicies(String sessionPersistPolicies) {
        String[] policyArray = sessionPersistPolicies.split(",");
        EnumSet<SessionPersistPolicy> policySet = EnumSet.of(SessionPersistPolicy.DEFAULT);
        for (String policyName : policyArray) {
            SessionPersistPolicy policy = SessionPersistPolicy.fromName(policyName);
            policySet.add(policy);
        }
        this.sessionPersistPoliciesSet = policySet;
    }

    public boolean getSaveOnChange() {
        return this.sessionPersistPoliciesSet.contains(SessionPersistPolicy.SAVE_ON_CHANGE);
    }

    public boolean getAlwaysSaveAfterRequest() {
        return this.sessionPersistPoliciesSet.contains(SessionPersistPolicy.ALWAYS_SAVE_AFTER_REQUEST);
    }

    public String getSentinels() {
        StringBuilder sentinels = new StringBuilder();
        for (Iterator<String> iter = sentinelSet.iterator(); iter.hasNext();) {
            sentinels.append(iter.next());
            if (iter.hasNext()) {
                sentinels.append(",");
            }
        }
        return sentinels.toString();
    }

    public void setSentinels(String sentinels) {
        if (null == sentinels) {
            sentinels = "";
        }
        String[] sentinelArray = sentinels.split(",");
        this.sentinelSet = new HashSet<String>(Arrays.asList(sentinelArray));
    }

    public Set<String> getSentinelSet() {
        return sentinelSet;
    }

    public String getSentinelMaster() {
        return sentinelMaster;
    }

    public void setSentinelMaster(String sentinelMaster) {
        this.sentinelMaster = sentinelMaster;
    }

    public int getMaxActiveSessions() {
        return this.maxActiveSessions;
    }

    public void setMaxActiveSessions(int max) {
        int oldMaxActiveSessions = this.maxActiveSessions;
        this.maxActiveSessions = max;
        this.support.firePropertyChange("maxActiveSessions", new Integer(oldMaxActiveSessions), new Integer(this.maxActiveSessions));
    }

    @Override
    public int getRejectedSessions() {
        return rejectedSessions;
    }

    public void setRejectedSessions(int rejectedSessions) {
        this.rejectedSessions = rejectedSessions;
    }

    @Override
    public int getActiveSessions() {
        Jedis jedis = null;
        try {
            jedis = acquireConnection();
            return jedis.dbSize().intValue();
        } finally {
            if (jedis != null) {
                jedis.close();
            }
        }
    }

    @Override
    public Session findSession(String id) {
        RedisSession session = null;
        if (id == null) {
            currentSessionIsPersisted.set(false);
            currentSession.set(null);
            currentSessionId.set(null);
        } else if (id.equals(currentSessionId.get())) {
            session = currentSession.get();
        } else {
            byte[] data = loadSessionDataFromRedis(id);
            if (data != null && (session = deserializer(data)) != null) {
                currentSession.set(session);
                currentSessionIsPersisted.set(true);
                currentSessionId.set(id);
            } else {
                currentSessionIsPersisted.set(false);
                currentSession.set(null);
                currentSessionId.set(null);
            }
        }
        if (session != null) {
            Sender sender = senders.get(session.getId());
            session.setSender(sender);
        }
        return session;
    }

    @Override
    public Session[] findSessions() {
        Jedis jedis = null;
        try {
            jedis = acquireConnection();
            Set<String> keySet = jedis.keys("*");
            Session[] sessions = new RedisSession[keySet.size()];
            int i = 0;
            for (String key : keySet) {
                byte[] data = loadSessionDataFromRedis(key);
                sessions[i++] = deserializer(data);
            }
            for (Session session : sessions) {
                Sender sender = senders.get(session.getId());
                session.setSender(sender);
            }
            return sessions;
        } finally {
            if (jedis != null) {
                jedis.close();
            }
        }
    }

    @Override
    public String[] findSessionIds() {
        Jedis jedis = null;
        try {
            jedis = acquireConnection();
            Set<String> keySet = jedis.keys("*");
            return keySet.toArray(new String[0]);
        } finally {
            if (jedis != null) {
                jedis.close();
            }
        }
    }

    @Override
    public Session createSession(String sessionId) {
        if(maxActiveSessions >= 0 && sessions.size() >= maxActiveSessions) {
            ++rejectedSessions;
            throw new IllegalStateException(sm.getString("standardManager.createSession.ise"));
        } else {
            RedisSession session = null;
            Jedis jedis = null;
            try {
                jedis = acquireConnection();

                if (sessionId == null) {
                    do {
                        sessionId = generateSessionId();
                    } while (jedis.exists(sessionId.getBytes()));
                } else {
                    if (jedis.exists(sessionId.getBytes())) {
                        sessionId = null;
                    }
                }

                if (sessionId != null) {
                    session = new RedisSession(this);
                    session.setNew(true);
                    session.setValid(true);
                    session.setCreationTime(System.currentTimeMillis());
                    session.setMaxInactiveInterval(maxInactiveInterval);
                    session.setId(sessionId);
                }
                currentSession.set(session);
                currentSessionId.set(sessionId);
                currentSessionIsPersisted.set(false);

                if (session != null) {
                    try {
                        save(session);
                    } catch (Exception e) {
                        log.error("Error saving newly created session: " + e.getMessage());
                        currentSession.set(null);
                        currentSessionId.set(null);
                        session = null;
                    }
                }
            } finally {
                if (jedis != null) {
                    jedis.close();
                }
            }
            return session;
        }
    }

    @Override
    public void add(Session session) {
        try {
            save(session);
        } catch (Exception e) {
            log.warn("Unable to add to session manager store: " + e.getMessage());
            throw new RuntimeException("Unable to add to session manager store.", e);
        }
    }

    @Override
    public void remove(Session session) {
        Jedis jedis = null;
        try {
            jedis = acquireConnection();
            jedis.del(session.getId());
        } finally {
            if (jedis != null) {
                jedis.close();
            }
        }
    }

    @Override
    public void processExpires() {

    }

    @Override
    public void start() throws LifecycleException {
        if (!started) {
            initializeDatabaseConnection();
            initializeSubscribe();
        }
        super.start();
    }

    @Override
    public void stop() throws LifecycleException {
        if (started && connectionPool != null) {
            try {
                connectionPool.destroy();
                connectionPool = null;
            } catch (Exception e) {
            }
        }
        super.stop();
    }

    @Override
    public boolean containsSession(String id) {
        Jedis jedis = null;
        try {
            jedis = acquireConnection();
            return jedis.exists(id.getBytes());
        } finally {
            if (jedis != null) {
                jedis.close();
            }
        }
    }

    public void save(Session session) throws IOException {
        save(session, false);
    }

    public void save(Session session, boolean forceSave) throws IOException {
        Jedis jedis = null;
        try {
            jedis = acquireConnection();
            saveInternal(jedis, session, forceSave);
        } finally {
            if (jedis != null) {
                jedis.close();
            }
        }
    }

    protected void saveInternal(Jedis jedis, Session session, boolean forceSave) throws IOException {
        log.trace("Saving session " + session + " into Redis");

        RedisSession redisSession = (RedisSession) session;

        byte[] binaryId = redisSession.getId().getBytes();

        Boolean isCurrentSessionPersisted;
        if (forceSave || redisSession.isDirty()
                || (isCurrentSessionPersisted = currentSessionIsPersisted.get()) == null
                || !isCurrentSessionPersisted) {

            log.trace("Save was determined to be necessary");

            if (getMaxInactiveInterval() > 0) {
                jedis.setex(binaryId, getMaxInactiveInterval(), serializer(session));
            } else if (getMaxInactiveInterval() < 0) {
                jedis.set(binaryId, serializer(session));
            }

            redisSession.resetDirtyTracking();
            currentSessionIsPersisted.set(true);

            senders.put(session.getId(), session.getSender());
        } else {
            log.trace("Save was determined to be unnecessary");
        }
    }

    public byte[] loadSessionDataFromRedis(String id) {
        Jedis jedis = null;
        try {
            log.trace("Attempting to load session " + id + " from Redis");

            jedis = acquireConnection();
            byte[] data = jedis.get(id.getBytes());
            if (data == null) {
                log.trace("Session " + id + " not found in Redis");
            }
            return data;
        } finally {
            if (jedis != null) {
                jedis.close();
            }
        }
    }

    protected Jedis acquireConnection() {
        Jedis jedis = connectionPool.getResource();

        if (getDatabase() != 0) {
            jedis.select(getDatabase());
        }
        return jedis;
    }

    private void initializeDatabaseConnection() throws LifecycleException {
        if (getSentinelMaster() != null) {
            Set<String> sentinelSet = getSentinelSet();
            if (sentinelSet != null && !sentinelSet.isEmpty()) {
                connectionPool = new JedisSentinelPool(getSentinelMaster(), sentinelSet, connectionPoolConfig, getTimeout(), getPassword());
            } else {
                throw new LifecycleException("Error configuring Redis Sentinel connection pool: expected both `sentinelMaster` and `sentiels` to be configured");
            }
        } else {
            connectionPool = new JedisPool(this.connectionPoolConfig, getHost(), getPort(), getTimeout(), getPassword());
        }
    }

    private void initializeSubscribe() {
        lifecycle.addLifecycleListener(new LifecycleListener() {
            @Override
            public void lifecycleEvent(LifecycleEvent event) {
                if (event.getType().equals(SESSION_EXPIRED_EVENT)) {
                    String sessionId = (String) event.getData();
                    senders.remove(sessionId);
                }
            }
        });

        Thread thread = new Thread(new KeySubscribe(), "RedisSubscribe");
        thread.setDaemon(true);
        thread.start();
    }

    protected RedisSession deserializer(byte[] data) {
        Input input;
        try {
            input = new Input(data);
            return kryo.readObject(input, RedisSession.class);
        } catch (Exception e) {
            return null;
        }
    }

    protected byte[] serializer(Object object) {
        Output out = null;
        try {
            ByteArrayOutputStream bytesOut = new ByteArrayOutputStream();
            out = new Output(bytesOut);
            kryo.writeObject(out, object);
            out.flush();
            return bytesOut.toByteArray();
        } finally {
            if (out != null) {
                out.close();
            }
        }
    }

    static {
        kryo = new Kryo();
        kryo.register(RedisSession.class);
    }

    class KeySubscribe extends JedisPubSub implements Runnable {
        @Override
        public void onPMessage(String pattern, String channel, String message) {
            if (log.isInfoEnabled()) {
                log.info(pattern + "=" + channel + "=" + message);
            }
            lifecycle.fireLifecycleEvent(SESSION_EXPIRED_EVENT, message);
        }
        @Override
        public void run() {
            Jedis jedis = null;
            try {
                jedis = acquireConnection();
                jedis.psubscribe(this, "*");
            } finally {
                if (jedis != null) {
                    jedis.close();
                }
            }
        }
    }

}
