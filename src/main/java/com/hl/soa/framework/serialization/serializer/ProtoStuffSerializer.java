package com.hl.soa.framework.serialization.serializer;

import com.dyuproject.protostuff.LinkedBuffer;
import com.dyuproject.protostuff.ProtostuffIOUtil;
import com.dyuproject.protostuff.Schema;
import com.dyuproject.protostuff.runtime.RuntimeSchema;
import org.objenesis.Objenesis;
import org.objenesis.ObjenesisStd;
import org.objenesis.instantiator.ObjectInstantiator;
import org.springframework.stereotype.Component;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * @author Hl
 * @create 2021/11/20 18:14
 */
@Component
public class ProtoStuffSerializer implements ISerializer {

    private static Map<Class<?>, Schema<?>> CACHE_SCHEMA = new ConcurrentHashMap<>();
    private static Objenesis OBJENESIS = new ObjenesisStd(true);

    private static <T> Schema<T> getSchema(Class<T> cls) {
        Schema<T> schema = (Schema<T>) CACHE_SCHEMA.getOrDefault(cls, RuntimeSchema.createFrom(cls));
        CACHE_SCHEMA.putIfAbsent(cls, schema);
        return schema;
    }

    @Override
    public <T> byte[] serialize(T o) {
        Class<T> clz = (Class<T>) o.getClass();
        LinkedBuffer buffer = LinkedBuffer.allocate(LinkedBuffer.DEFAULT_BUFFER_SIZE);
        Schema<T> schema = getSchema(clz);
        try {
            return ProtostuffIOUtil.toByteArray(o, schema, buffer);
        } catch (Exception e) {
            throw new RuntimeException(e);
        } finally {
            buffer.clear();
        }

    }

    @Override
    public <T> T deserialize(byte[] data, Class<T> clazz) {
        ObjectInstantiator<T> instantiator = OBJENESIS.getInstantiatorOf(clazz);
        T message = instantiator.newInstance();
        Schema<T> schema = getSchema(clazz);
        ProtostuffIOUtil.mergeFrom(data, message, schema);
        return message;
    }

}
