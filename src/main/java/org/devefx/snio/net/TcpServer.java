package org.devefx.snio.net;

import io.netty.bootstrap.ServerBootstrap;
import io.netty.channel.*;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.SocketChannel;
import io.netty.channel.socket.nio.NioServerSocketChannel;
import io.netty.handler.codec.LengthFieldBasedFrameDecoder;
import io.netty.handler.codec.LengthFieldPrepender;
import io.netty.handler.logging.LogLevel;
import io.netty.handler.logging.LoggingHandler;
import org.devefx.snio.codec.MessageToMessageDecoder;
import org.devefx.snio.core.ServerBase;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class TcpServer extends ServerBase implements Runnable {
    private static final Logger log = LoggerFactory.getLogger(TcpServer.class);
    private ServerBootstrap bootstrap;
    private EventLoopGroup boosGroup;
    private EventLoopGroup workerGroup;
    private Thread thread;
    private MessageToMessageDecoder decoder;
    @Override
    public String getInfo() {
        return "TcpServer/1.0";
    }

    public void setDecoder(MessageToMessageDecoder decoder) {
        this.decoder = decoder;
    }

    @Override
    public void initialize() {
        if (!initialized) {
            initialized = true;
            boosGroup = new NioEventLoopGroup();
            workerGroup = new NioEventLoopGroup();
            bootstrap = new ServerBootstrap();
            bootstrap.group(boosGroup, workerGroup)
                    .channel(NioServerSocketChannel.class)
                    .option(ChannelOption.SO_BACKLOG, 1024)
                    .handler(new LoggingHandler(LogLevel.INFO))
                    .childHandler(new MyChannelInitializer());
        }
    }

    @Override
    public void start() {
        if (!started) {
            initialize();
            lifecycle.fireLifecycleEvent(BEFORE_START_EVENT, this);
            thread = new Thread(this, "TcpServer");
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
                    	if (log.isInfoEnabled()) {
                    		log.info("");
						}
                    } else {
                        
                    }
                }
            }).sync().channel().closeFuture().await();
        } catch (InterruptedException e) {
            e.printStackTrace();
        } finally {
            boosGroup.shutdownGracefully();
            workerGroup.shutdownGracefully();
        }
    }

    public class MyChannelInitializer extends ChannelInitializer<SocketChannel> {
        @Override
        protected void initChannel(SocketChannel ch) throws Exception {
            ChannelPipeline pipeline =  ch.pipeline();
            pipeline.addLast(new LengthFieldBasedFrameDecoder(1024, 0, 2, 0, 2));
            pipeline.addLast(new LengthFieldPrepender(2));
            pipeline.addLast(new NetChannelHandler(TcpServer.this, decoder));
        }
    }
}
