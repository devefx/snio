package org.devefx.snio.net.connector;

import io.netty.buffer.ByteBuf;
import io.netty.buffer.Unpooled;
import io.netty.channel.Channel;
import io.netty.channel.socket.DatagramPacket;
import org.devefx.snio.net.Sender;

import java.net.InetSocketAddress;

public class UdpSender implements Sender {

	private Channel channel;
	private InetSocketAddress sender;
	
	public UdpSender(Channel channel, InetSocketAddress sender) {
		this.channel = channel;
		this.sender = sender;
	}
	
	@Override
	public boolean isActive() {
		return channel.isActive();
	}
	
	@Override
	public boolean isWritable() {
		return channel.isWritable();
	}

	@Override
	public InetSocketAddress remoteAddress() {
		return sender;
	}

	@Override
	public void writeAndFlush(ByteBuf buf) {
		channel.writeAndFlush(new DatagramPacket(buf, sender));
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
