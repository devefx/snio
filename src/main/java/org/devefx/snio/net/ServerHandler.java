package org.devefx.snio.net;

import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.SimpleChannelInboundHandler;
import org.devefx.snio.Request;
import org.devefx.snio.core.Dispatcher;

public class ServerHandler extends SimpleChannelInboundHandler<Request> {

    protected Dispatcher dispatcher;

    public ServerHandler(Dispatcher dispatcher) {
        this.dispatcher = dispatcher;
    }

    @Override
    protected void channelRead0(ChannelHandlerContext ctx, Request request) throws Exception {
        dispatcher.push(request);
    }

    @Override
    public void channelReadComplete(ChannelHandlerContext ctx) throws Exception {
        ctx.flush();
    }
}
