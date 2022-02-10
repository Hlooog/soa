package com.hl.soa.framework.serialization.serializer;

import org.jboss.marshalling.*;
import org.springframework.stereotype.Component;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;

/**
 * @author Hl
 * @create 2021/11/21 17:45
 */
@Component
public class MarshallingSerializer implements ISerializer {

    private static final MarshallingConfiguration CONFIGURATION  = new MarshallingConfiguration();
    private static final MarshallerFactory MARSHALLER_FACTORY = Marshalling.getProvidedMarshallerFactory("serial");

    static {
        CONFIGURATION.setVersion(5);
    }

    @Override
    public <T> byte[] serialize(T o) {

        final ByteArrayOutputStream baops = new ByteArrayOutputStream();
        try {
            final Marshaller marshaller = MARSHALLER_FACTORY.createMarshaller(CONFIGURATION);
            marshaller.start(Marshalling.createByteOutput(baops));
            marshaller.writeObject(o);
            marshaller.finish();
            return baops.toByteArray();
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    public <T> T deserialize(byte[] data, Class<T> clazz) {
        final ByteArrayInputStream baips = new ByteArrayInputStream(data);
        final Unmarshaller unmarshaller;
        try {
            unmarshaller = MARSHALLER_FACTORY.createUnmarshaller(CONFIGURATION);
            unmarshaller.start(Marshalling.createByteInput(baips));
            T t = unmarshaller.readObject(clazz);
            unmarshaller.finish();
            return t;
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

}
