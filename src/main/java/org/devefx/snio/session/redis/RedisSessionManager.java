package org.devefx.snio.session.redis;

import com.esotericsoftware.kryo.Kryo;
import com.esotericsoftware.kryo.io.Output;
import org.devefx.snio.Lifecycle;
import org.devefx.snio.LifecycleException;
import org.devefx.snio.LifecycleListener;
import org.devefx.snio.Session;
import org.devefx.snio.session.ManagerBase;
import org.devefx.snio.util.LifecycleSupport;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import redis.clients.jedis.Jedis;
import redis.clients.jedis.JedisPoolConfig;
import redis.clients.util.Pool;

import java.io.ByteArrayOutputStream;

public class RedisSessionManager extends ManagerBase implements Lifecycle {
    private static final Logger log = LoggerFactory.getLogger(RedisSessionManager.class);
    protected byte[] NULL_SESSION = "null".getBytes();
    protected String host = "localhost";
    protected int port = 6379;
    protected int database = 0;
    protected String password = null;
    protected int timeout = 60;

    protected Pool<Jedis> connectionPool;
    protected JedisPoolConfig connectionPoolConfig = new JedisPoolConfig();
    protected Kryo kryo;

    protected LifecycleSupport lifecycle = new LifecycleSupport(this);
    protected int maxActiveSessions = -1;
    protected int rejectedSessions = 0;

    @Override
    public String getName() {
        return "RedisSessionManager";
    }

    @Override
    public String getInfo() {
        return "RedisSessionManager/1.0";
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

    @Override
    public int getRejectedSessions() {
        return rejectedSessions;
    }

    @Override
    public void setRejectedSessions(int rejectedSessions) {
        this.rejectedSessions = rejectedSessions;
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

    }

    @Override
    public void stop() throws LifecycleException {

    }

    @Override
    public Session createSession(String sessionId) {
        if(maxActiveSessions >= 0 && sessions.size() >= maxActiveSessions) {
            ++rejectedSessions;
            throw new IllegalStateException(sm.getString("standardManager.createSession.ise"));
        } else {

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
                    Session session = createEmptySession();
                    session.setNew(true);
                    session.setValid(true);
                    session.setCreationTime(System.currentTimeMillis());
                    session.setMaxInactiveInterval(maxInactiveInterval);
                    session.setId(sessionId);
                    return session;
                }
            } finally {
                if (jedis != null) {
                    jedis.close();
                }
            }
            return null;
        }
    }

    @Override
    public Session createEmptySession() {
        return new RedisSession(this);
    }

    @Override
    public void add(Session session) {
        Jedis jedis = null;
        try {
            jedis = acquireConnection();

            //

            int size = sessions.size();
            if(size > maxActive) {
                maxActive = size;
            }
        } finally {
            if (jedis != null) {
                jedis.close();
            }
        }
    }

    public Kryo getKryo() {
        if (kryo == null) {
            kryo = new Kryo();
            kryo.register(RedisSession.class);
        }
        return kryo;
    }

    public byte[] serializer(Object object) {
        Output out = null;
        try {
            ByteArrayOutputStream bytesOut = new ByteArrayOutputStream();
            out = new Output(bytesOut);
            getKryo().writeObject(out, object);
            out.flush();
            return bytesOut.toByteArray();
        } finally {
            if (out != null) {
                out.close();
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


}
