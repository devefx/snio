package org.devefx.snio.codec;

import io.netty.buffer.ByteBuf;
import org.devefx.snio.Message;

public interface MessageToMessageDecoder {

    Message decode(ByteBuf msg);

}
