package snio.service;

import org.devefx.snio.Request;
import org.devefx.snio.Service;
import org.devefx.snio.session.redis.RedisSessionManager;

import javax.annotation.Resource;

public class LoginService implements Service {

    @Resource
    private RedisSessionManager manager;

    @Override
    public Short getType() {
        return 1;
    }

    @Override
    public void service(Request request) {

    }
}
