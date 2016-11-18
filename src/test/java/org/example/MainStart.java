package org.example;

import org.devefx.snio.*;
import org.devefx.snio.core.StandardEngine;
import org.devefx.snio.http.HttpServer;
import org.devefx.snio.net.tcp.TCPServer;
import org.devefx.snio.net.udp.UDPServer;
import org.example.config.TCPServerInitializer;
import org.example.config.UDPServerInitializer;
import org.example.service.ExampleService;

public class MainStart {

    public static void main(String[] args) throws LifecycleException {

        TCPServer tcp = new TCPServer();
        tcp.setLengthFieldLength(1);// 自动粘包，使用第 1 字节描述数据包长度
        tcp.setPort(8888);
        tcp.addService(new ExampleService());
        tcp.setServerInitializer(new TCPServerInitializer());

        UDPServer udp = new UDPServer();
        udp.setPort(9999);
        udp.addService(new ExampleService());
        udp.setServerInitializer(new UDPServerInitializer());

        Server http = new HttpServer();
        http.setPort(7777);

        StandardEngine engine = new StandardEngine();
        engine.setHost("localhost");
        engine.addServer(tcp);
        engine.addServer(udp);
        engine.addServer(http);
        engine.start();
        engine.await();
    }


}
