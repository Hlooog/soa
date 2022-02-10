package com.hl.soa.framework.serialization;

import com.hl.soa.framework.serialization.engine.SerializerEngine;
import com.hl.soa.framework.serialization.serializer.ISerializer;
import io.netty.buffer.ByteBuf;
import io.netty.channel.ChannelHandlerContext;
import io.netty.handler.codec.MessageToByteEncoder;

/**
 * @author Hl
 * @create 2021/11/23 18:30
 */
public class NettyEncoderHandler extends MessageToByteEncoder {

    private Class<? extends ISerializer> serializeType;


    public NettyEncoderHandler(Class<? extends ISerializer> serializeType) {
        this.serializeType = serializeType;
    }

    @Override
    protected void encode(ChannelHandlerContext ctx, Object o, ByteBuf out) throws Exception {
        byte[] data = SerializerEngine.serialize(o, serializeType);
        out.writeInt(data.length);
        out.writeBytes(data);
    }
}
