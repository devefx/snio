package org.devefx.snio.net;

import io.netty.bootstrap.Bootstrap;
import io.netty.channel.ChannelFuture;
import io.netty.channel.ChannelFutureListener;
import io.netty.channel.ChannelOption;
import io.netty.channel.EventLoopGroup;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.nio.NioDatagramChannel;
import org.devefx.snio.codec.MessageToMessageDecoder;
import org.devefx.snio.core.ServerBase;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class UdpServer extends ServerBase implements Runnable {
    private static final Logger log = LoggerFactory.getLogger(UdpServer.class);
    private Bootstrap bootstrap;
    private EventLoopGroup group;
    private Thread thread;
    private MessageToMessageDecoder decoder;
    @Override
    public String getInfo() {
        return "UdpServer/1.0";
    }

    public void setDecoder(MessageToMessageDecoder decoder) {
        this.decoder = decoder;
    }

    @Override
    public void initialize() {
        if (!initialized) {
            initialized = true;
            group = new NioEventLoopGroup();
            bootstrap = new Bootstrap();
            bootstrap.group(group)
                    .channel(NioDatagramChannel.class)
                    .option(ChannelOption.SO_BROADCAST, true)
                    .handler(new NetChannelHandler(UdpServer.this, decoder));
        }
    }

    @Override
    public void start() {
        if (!started) {
            initialize();
            lifecycle.fireLifecycleEvent(BEFORE_START_EVENT, this);
            thread = new Thread(this, "UdpServer");
            thread.setDaemon(true);
            thread.start();
            lifecycle.fireLifecycleEvent(START_EVENT, this);
            started = true;
            lifecycle.fireLifecycleEvent(AFTER_START_EVENT, this);
        }
    }

    @Override
    public void stop() {
        if (started) {
            try {
                lifecycle.fireLifecycleEvent(BEFORE_STOP_EVENT, this);
                thread.interrupt();
                thread.join();
                lifecycle.fireLifecycleEvent(STOP_EVENT, this);
                started = false;
                lifecycle.fireLifecycleEvent(AFTER_STOP_EVENT, this);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }
    }

    @Override
    public void run() {
        try {
            bootstrap.bind(getEngine().getDefaultHost(), getPort()).addListener(new ChannelFutureListener() {
                @Override
                public void operationComplete(ChannelFuture future) throws Exception {
                    if (future.isSuccess()) {

                    } else {

                    }
                }
            }).sync().channel().closeFuture().await();
        } catch (InterruptedException e) {
            e.printStackTrace();
        } finally {
            group.shutdownGracefully();
        }
    }
}
