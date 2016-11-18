package org.example.config;

import io.netty.channel.Channel;
import io.netty.channel.ChannelPipeline;
import org.devefx.snio.Server;
import org.devefx.snio.codec.SnioDecoder;
import org.devefx.snio.net.ServerInitializer;

public class UDPServerInitializer implements ServerInitializer {
    @Override
    public void initChannel(Channel channel, Server server) throws Exception {
        ChannelPipeline pipeline = channel.pipeline();
        // 自定义编、解码器
        // 最后的消息包必须封装成 RequestEvent 对象
        pipeline.addLast(new SnioDecoder(server.getContainer().getManager()));
    }
}
