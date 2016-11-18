package org.devefx.snio.net.tcp;

import io.netty.bootstrap.ServerBootstrap;
import io.netty.channel.*;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.SocketChannel;
import io.netty.channel.socket.nio.NioServerSocketChannel;
import io.netty.handler.codec.LengthFieldBasedFrameDecoder;
import io.netty.handler.codec.LengthFieldPrepender;
import io.netty.handler.logging.LogLevel;
import io.netty.handler.logging.LoggingHandler;
import org.devefx.snio.LifecycleException;
import org.devefx.snio.codec.SnioDecoder;
import org.devefx.snio.net.ServerBase;
import org.devefx.snio.net.ServerHandler;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class TCPServer extends ServerBase implements Runnable {

    private static final Logger log = LoggerFactory.getLogger(TCPServer.class);
    private ServerBootstrap bootstrap;
    private EventLoopGroup boosGroup;
    private EventLoopGroup workerGroup;
    private int lengthFieldLength = 0;
    private int lengthFieldOffset = 0;

    @Override
    public String getInfo() {
        return "TcpServer/1.1";
    }

    public void setLengthFieldLength(int lengthFieldLength) {
        this.lengthFieldLength = lengthFieldLength;
    }

    public void setLengthFieldOffset(int lengthFieldOffset) {
        this.lengthFieldOffset = lengthFieldOffset;
    }

    @Override
    public void initialize() throws LifecycleException {
        if (!initialized) {
            initialized = true;
            boosGroup = new NioEventLoopGroup();
            workerGroup = new NioEventLoopGroup();
            bootstrap = new ServerBootstrap();
            bootstrap.group(boosGroup, workerGroup)
                    .channel(NioServerSocketChannel.class)
                    .option(ChannelOption.SO_BACKLOG, 1024)
                    .handler(new LoggingHandler(LogLevel.INFO))
                    .childHandler(channelInitializer());
        }
    }

    @Override
    public void run() {
        try {
            ChannelFuture future = bootstrap.bind(bindAddress()).addListener(new ChannelFutureListener() {
                @Override
                public void operationComplete(ChannelFuture future) throws Exception {
                    if (future.isSuccess() && log.isInfoEnabled()) {
                        log.info("Start TCP server [" + bindAddress() + "]");
                    }
                }
            }).sync();
            future.channel().closeFuture().await();
        } catch (InterruptedException e) {
            log.error("Start TCP server failure", e);
        } finally {
            boosGroup.shutdownGracefully();
            workerGroup.shutdownGracefully();
            started = false;
        }
    }

    protected ChannelInitializer<SocketChannel> channelInitializer() {
        return new ChannelInitializer<SocketChannel>() {
            @Override
            protected void initChannel(SocketChannel ch) throws Exception {
                if (lengthFieldLength > 0) {
                    int maxFrameLength = (int) Math.pow(256, lengthFieldLength) - 1;
                    ch.pipeline().addLast(new LengthFieldBasedFrameDecoder(maxFrameLength,
                            lengthFieldOffset, lengthFieldLength, 0, lengthFieldLength));
                    ch.pipeline().addLast(new LengthFieldPrepender(lengthFieldLength));
                }
                if (serverInitializer != null) {
                    serverInitializer.initChannel(ch, TCPServer.this);
                } else {
                    ch.pipeline().addLast(new SnioDecoder(getContainer().getManager()));
                }
                ch.pipeline().addLast(new ServerHandler(dispatcher));
            }
        };
    }

}
