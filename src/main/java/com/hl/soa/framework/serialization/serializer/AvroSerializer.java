package com.hl.soa.framework.serialization.serializer;

import org.apache.avro.io.BinaryDecoder;
import org.apache.avro.io.BinaryEncoder;
import org.apache.avro.io.DecoderFactory;
import org.apache.avro.io.EncoderFactory;
import org.apache.avro.specific.SpecificDatumReader;
import org.apache.avro.specific.SpecificDatumWriter;
import org.apache.avro.specific.SpecificRecordBase;
import org.springframework.stereotype.Component;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;

/**
 * @author Hl
 * @create 2021/11/21 17:30
 */
@Component
public class AvroSerializer implements ISerializer {


    @Override
    public <T> byte[] serialize(T o) {

        if (! (o instanceof SpecificRecordBase)) {
            throw new UnsupportedOperationException("not support obj type");
        }

        SpecificDatumWriter writer = new SpecificDatumWriter(o.getClass());

        ByteArrayOutputStream baops = new ByteArrayOutputStream();
        BinaryEncoder encoder = EncoderFactory.get().directBinaryEncoder(baops, null);

        try {
            writer.write(o, encoder);
            return baops.toByteArray();
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    public <T> T deserialize(byte[] data, Class<T> clazz) {
        if (! SpecificRecordBase.class.isAssignableFrom(clazz)) {
            throw new UnsupportedOperationException("not support class type");
        }
        SpecificDatumReader reader = new SpecificDatumReader(clazz);
        BinaryDecoder decoder = DecoderFactory.get()
                .directBinaryDecoder(new ByteArrayInputStream(data), null);
        try {
            return (T) reader.read(clazz.newInstance(), decoder);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

}
