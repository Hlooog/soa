package com.hl.soa.framework.serialization.serializer;

/**
 * @author Hl
 * @create 2021/11/19 17:53
 */
public interface ISerializer {

    <T> byte[] serialize(T o);

    <T> T deserialize(byte[] data, Class<T> clazz);

}
