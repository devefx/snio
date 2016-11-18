package org.devefx.snio.net;

import io.netty.channel.Channel;
import org.devefx.snio.Server;

public interface ServerInitializer {

    void initChannel(Channel channel, Server server) throws Exception;

}
