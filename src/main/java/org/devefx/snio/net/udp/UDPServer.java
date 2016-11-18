package org.devefx.snio.net.udp;

import io.netty.bootstrap.Bootstrap;
import io.netty.channel.*;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.nio.NioDatagramChannel;
import org.devefx.snio.LifecycleException;
import org.devefx.snio.net.ServerBase;
import org.devefx.snio.net.ServerHandler;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class UDPServer extends ServerBase implements Runnable {

    private static final Logger log = LoggerFactory.getLogger(UDPServer.class);
    private Bootstrap bootstrap;
    private EventLoopGroup group;

    @Override
    public String getInfo() {
        return "UdpServer/1.1";
    }

    @Override
    public void initialize() throws LifecycleException {
        if (!initialized) {
            initialized = true;
            group = new NioEventLoopGroup();
            bootstrap = new Bootstrap();
            bootstrap.group(group)
                    .channel(NioDatagramChannel.class)
                    .option(ChannelOption.SO_BROADCAST, true)
                    .handler(channelInitializer());
        }
    }

    @Override
    public void run() {
        try {
            ChannelFuture future = bootstrap.bind(bindAddress()).addListener(new ChannelFutureListener() {
                @Override
                public void operationComplete(ChannelFuture future) throws Exception {
                    if (future.isSuccess()) {
                        log.info("Start UDP server [" + bindAddress() + "]");
                    }
                }
            }).sync();
            future.channel().closeFuture().await();
        } catch (InterruptedException e) {
            log.error("Start UDP server failure", e);
        } finally {
            group.shutdownGracefully();
        }
    }

    protected ChannelInitializer<NioDatagramChannel> channelInitializer() {
        return new ChannelInitializer<NioDatagramChannel>() {
            @Override
            protected void initChannel(NioDatagramChannel ch) throws Exception {
                if (serverInitializer != null) {
                    serverInitializer.initChannel(ch, UDPServer.this);
                }
                ch.pipeline().addLast(new ServerHandler(dispatcher));
            }
        };
    }

}
