package snio.service;

import org.devefx.snio.Request;
import org.devefx.snio.Service;

public class LoginService implements Service {

	@Override
    public Short getType() {
        return 1;
    }

    @Override
    public void service(Request request) {
    	
    }
}
