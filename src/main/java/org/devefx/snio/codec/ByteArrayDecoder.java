package org.devefx.snio.codec;

import io.netty.buffer.ByteBuf;
import org.devefx.snio.Message;

public class ByteArrayDecoder implements MessageToMessageDecoder {
    @Override
    public Message decode(ByteBuf msg) {
        short type = msg.readShort();
        byte[] array = new byte[msg.readableBytes()];
        msg.readBytes(array);
        return new Message(type, array);
    }
}
