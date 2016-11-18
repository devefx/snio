package org.example.service;

import io.netty.util.CharsetUtil;
import org.devefx.snio.Manager;
import org.devefx.snio.Request;
import org.devefx.snio.Service;

import java.text.SimpleDateFormat;
import java.util.Date;

public class ExampleService implements Service {

    @Override
    public Object getType() {
        return DEFAULT_TYPE;
    }

    @Override
    public void service(Request request, Manager manager) {

        byte[] bytes = request.readerObject(byte[].class);

        System.out.println(new String(bytes));

        SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd hh:mm:ss");
        String dataString = dateFormat.format(new Date());

        request.getSession().getSender().writeAndFlush(("now time: " + dataString).getBytes(CharsetUtil.UTF_8));

    }
}
