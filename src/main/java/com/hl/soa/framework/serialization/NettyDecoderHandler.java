package com.hl.soa.framework.serialization;

import com.hl.soa.framework.model.IResponse;
import com.hl.soa.framework.serialization.engine.SerializerEngine;
import com.hl.soa.framework.serialization.serializer.ISerializer;
import io.netty.buffer.ByteBuf;
import io.netty.channel.ChannelHandlerContext;
import io.netty.handler.codec.ByteToMessageDecoder;

import java.util.List;

/**
 * @author Hl
 * @create 2021/11/23 18:30
 */
public class NettyDecoderHandler extends ByteToMessageDecoder {

    private Class<?> genericClass;
    private Class<? extends ISerializer> serializeType;

    public NettyDecoderHandler(Class<?> genericClass, Class<? extends ISerializer> serializeType) {
        this.genericClass = genericClass;
        this.serializeType = serializeType;
    }

    @Override
    protected void decode(ChannelHandlerContext ctx, ByteBuf in, List<Object> out) throws Exception {
        if (in.readableBytes() < 4) {
            return;
        }
        // 标记可读长度
        in.markReaderIndex();
        // 获取协议的可读长度
        int dataLength = in.readInt();
        if (dataLength < 0) ctx.close();
        // 剩余可读的字节长度小于获取的可读长度 返回
        if (in.readableBytes() < dataLength) {
            in.resetReaderIndex();
            return;
        }
        byte[] data = new byte[dataLength];
        in.readBytes(data);
        Object o = SerializerEngine.deserialize(data, genericClass, serializeType);
        out.add(o);
    }
}
