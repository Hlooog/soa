package com.hl.soa.framework.serialization.serializer;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.serializer.SerializerFeature;
import org.springframework.stereotype.Component;

/**
 * @author Hl
 * @create 2021/11/19 18:35
 */
@Component
public class JSONSerializer implements ISerializer {

    @Override
    public <T> byte[] serialize(T o) {
        JSON.DEFFAULT_DATE_FORMAT = "yyyy-MM-dd HH:mm:ss";
        return JSON.toJSONString(o, SerializerFeature.WriteDateUseDateFormat).getBytes();
    }

    @Override
    public <T> T deserialize(byte[] data, Class<T> clazz) {
        return (T) JSON.parseObject(new String(data), clazz);
    }

}
