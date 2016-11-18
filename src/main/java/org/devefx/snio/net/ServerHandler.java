package org.devefx.snio.net;

import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.SimpleChannelInboundHandler;
import org.devefx.snio.event.RequestDispatcher;
import org.devefx.snio.event.RequestEvent;

public class ServerHandler extends SimpleChannelInboundHandler<RequestEvent> {

    protected RequestDispatcher dispatcher;

    public ServerHandler(RequestDispatcher dispatcher) {
        this.dispatcher = dispatcher;
    }

    @Override
    protected void channelRead0(ChannelHandlerContext ctx, RequestEvent msg) throws Exception {
        dispatcher.push(msg);
    }

    @Override
    public void channelReadComplete(ChannelHandlerContext ctx) throws Exception {
        ctx.flush();
    }
}
