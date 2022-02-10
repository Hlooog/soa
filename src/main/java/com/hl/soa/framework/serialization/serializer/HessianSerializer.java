package com.hl.soa.framework.serialization.serializer;

import com.caucho.hessian.io.HessianInput;
import com.caucho.hessian.io.HessianOutput;
import org.springframework.stereotype.Component;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;

/**
 * @author Hl
 * @create 2021/11/19 18:45
 */
@Component
public class HessianSerializer implements ISerializer {
    @Override
    public <T> byte[] serialize(T o) {
        if (o == null) throw new NullPointerException();

        ByteArrayOutputStream baops = new ByteArrayOutputStream();
        HessianOutput hop = new HessianOutput(baops);
        try {
            hop.writeObject(o);
            return baops.toByteArray();
        } catch (Exception e) {
            throw new RuntimeException(e);
        } finally {
            try {
                hop.close();
                baops.close();
            } catch (IOException e) {
                throw new RuntimeException(e);
            }

        }

    }

    @Override
    public <T> T deserialize(byte[] data, Class<T> clazz) {
        if (data == null) throw new NullPointerException();

        ByteArrayInputStream baips = new ByteArrayInputStream(data);
        HessianInput hip = new HessianInput(baips);
        try {
            return (T) hip.readObject(clazz);
        } catch (Exception e) {
            throw new RuntimeException(e);
        } finally {
            try {
                hip.close();
                baips.close();
            } catch (IOException e) {
                throw new RuntimeException(e);
            }
        }
    }

}
