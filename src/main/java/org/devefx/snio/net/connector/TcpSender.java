package org.devefx.snio.net.connector;

import io.netty.buffer.ByteBuf;
import io.netty.buffer.Unpooled;
import io.netty.channel.Channel;
import org.devefx.snio.net.Sender;

import java.net.SocketAddress;

public class TcpSender implements Sender {

	private Channel channel;
	
	public TcpSender(Channel channel) {
		this.channel = channel;
	}
	
	@Override
	public boolean isActive() {
		return isActive();
	}
	
	@Override
	public boolean isWritable() {
		return channel.isWritable();
	}

	@Override
	public SocketAddress remoteAddress() {
		return channel.remoteAddress();
	}

	@Override
	public void writeAndFlush(ByteBuf buf) {
		channel.writeAndFlush(buf);
	}
	
	@Override
	public void writeAndFlush(byte[] bytes) {
		writeAndFlush(Unpooled.copiedBuffer(bytes));
	}
	
	@Override
	public void close() {
		channel.close();
	}

}
