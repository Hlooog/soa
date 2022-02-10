package com.hl.soa.framework.serialization.serializer;

import org.springframework.stereotype.Component;

import java.io.*;

/**
 * @author Hl
 * @create 2021/11/19 17:55
 */
@Component
public class DefaultJavaSerializer implements ISerializer {

    @Override
    public <T> byte[] serialize(T o) {
        ByteArrayOutputStream baops = new ByteArrayOutputStream();
        try {
            ObjectOutputStream oops = new ObjectOutputStream(baops);
            oops.writeObject(o);
            oops.close();
        } catch (IOException e) {
            throw new RuntimeException(e);
        } finally {
            try {
                baops.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
        return baops.toByteArray();
    }

    @Override
    public <T> T deserialize(byte[] data, Class<T> clazz) {
        ByteArrayInputStream baips = new ByteArrayInputStream(data);
        try {
            ObjectInputStream oips = new ObjectInputStream(baips);
            oips.close();
            Object o = oips.readObject();
            return (T) o;
        } catch (Exception e) {
            throw new RuntimeException();
        } finally {
            try {
                baips.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

}
