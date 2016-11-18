package org.devefx.snio.http;

import io.netty.channel.ChannelInitializer;
import io.netty.channel.ChannelPipeline;
import io.netty.channel.socket.SocketChannel;
import io.netty.handler.codec.http.HttpObjectAggregator;
import io.netty.handler.codec.http.HttpRequestDecoder;
import io.netty.handler.codec.http.HttpResponseEncoder;
import io.netty.handler.stream.ChunkedWriteHandler;
import org.devefx.snio.net.tcp.TCPServer;

public class HttpServer extends TCPServer {

    @Override
    public String getInfo() {
        return "HttpServer/1.1";
    }

    @Override
    protected ChannelInitializer<SocketChannel> channelInitializer() {
        return new ChannelInitializer<SocketChannel>() {
            @Override
            protected void initChannel(SocketChannel ch) throws Exception {
                ChannelPipeline pipeline = ch.pipeline();
                pipeline.addLast(new HttpRequestDecoder());
                pipeline.addLast(new HttpObjectAggregator(65536));
                pipeline.addLast(new HttpResponseEncoder());
                pipeline.addLast(new ChunkedWriteHandler());
                pipeline.addLast(new HttpServerHandler(HttpServer.this));
            }
        };
    }
}
