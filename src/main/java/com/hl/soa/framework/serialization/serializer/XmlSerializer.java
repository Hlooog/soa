package com.hl.soa.framework.serialization.serializer;

import com.thoughtworks.xstream.XStream;
import com.thoughtworks.xstream.io.xml.DomDriver;
import org.springframework.stereotype.Component;

/**
 * @author Hl
 * @create 2021/11/19 18:02
 */
@Component
public class XmlSerializer implements ISerializer {

    private static final XStream X_STREAM = new XStream(new DomDriver());

    @Override
    public <T> byte[] serialize(T o) {
        return X_STREAM.toXML(o).getBytes();
    }

    @Override
    public <T> T deserialize(byte[] data, Class<T> clazz) {
        String xml = new String(data);
        return (T) X_STREAM.fromXML(xml);
    }

}
