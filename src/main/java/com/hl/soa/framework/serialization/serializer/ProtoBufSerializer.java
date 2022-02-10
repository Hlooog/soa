package com.hl.soa.framework.serialization.serializer;

import com.google.protobuf.GeneratedMessageV3;
import org.apache.commons.lang3.reflect.MethodUtils;
import org.springframework.stereotype.Component;

/**
 * @author Hl
 * @create 2021/11/20 17:50
 */
@Component
public class ProtoBufSerializer implements ISerializer {


    @Override
    public <T> byte[] serialize(T o) {

        if (!(o instanceof GeneratedMessageV3)) {
            throw new UnsupportedOperationException("not supported obj type");
        }
        try {
            return (byte[]) MethodUtils.invokeMethod(o, "toByteArray");
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    public <T> T deserialize(byte[] data, Class<T> clazz) {
        if (!GeneratedMessageV3.class.isAssignableFrom(clazz)) {
            throw new UnsupportedOperationException("not supported class type");
        }

        try {
            return (T) MethodUtils.invokeStaticMethod(clazz, "parseFrom", new Object[]{data});
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

}
