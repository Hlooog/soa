package com.hl.soa.framework.serialization.serializer;

import org.apache.thrift.TBase;
import org.apache.thrift.TDeserializer;
import org.apache.thrift.TException;
import org.apache.thrift.TSerializer;
import org.apache.thrift.protocol.TBinaryProtocol;
import org.springframework.stereotype.Component;

/**
 * @author Hl
 * @create 2021/11/20 18:37
 */
@Component
public class ThriftSerializer implements ISerializer {
    @Override
    public <T> byte[] serialize(T o) {
        if (!(o instanceof TBase)) {
            throw new UnsupportedOperationException("not support obj type");
        }

        try {
            TSerializer serializer = new TSerializer(new TBinaryProtocol.Factory());
            return serializer.serialize((TBase) o);
        } catch (TException e) {
            throw new RuntimeException(e);
        }

    }

    @Override
    public <T> T deserialize(byte[] data, Class<T> clazz) {
        if (! TBase.class.isAssignableFrom(clazz)) {
            throw new UnsupportedOperationException("not support class type");
        }

        try {
            TBase obj = (TBase) clazz.newInstance();
            TDeserializer deserializer = new TDeserializer(new TBinaryProtocol.Factory());
            deserializer.deserialize(obj, data);
            return (T) obj;
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

}
