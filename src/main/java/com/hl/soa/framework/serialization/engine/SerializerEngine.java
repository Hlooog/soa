package com.hl.soa.framework.serialization.engine;

import com.hl.soa.framework.serialization.serializer.ISerializer;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationContext;
import org.springframework.stereotype.Component;

import java.util.HashMap;
import java.util.Map;

/**
 * @author Hl
 * @create 2021/11/21 17:59
 */
@Component
public class SerializerEngine implements InitializingBean {

    private static final Map<Class<? extends ISerializer>, ISerializer> CACHE = new HashMap<>();

    @Autowired
    private ApplicationContext application;

    @Override
    public void afterPropertiesSet() throws Exception {
        Map<String, ISerializer> beans = application.getBeansOfType(ISerializer.class);
        for (ISerializer bean : beans.values()) {
            CACHE.put(bean.getClass(), bean);
        }
    }

    /**
     * 序列化方法
     * @param obj           序列化对象
     * @param engine        序列化工具类对象
     * @param <T>           序列化对象类型
     * @return
     */
    public static <T> byte[] serialize(T obj, Class<? extends ISerializer> engine) {
        ISerializer serializer = CACHE.get(engine);
        if (serializer == null) {
            throw new RuntimeException("obtain serialize error");
        }
        try {
            return serializer.serialize(obj);
        } catch (Exception e) {
            throw new RuntimeException("serialize error");
        }
    }

    /**
     * 反序列化方法
     * @param data          反序列化数组
     * @param clazz         反序列化的类对象
     * @param engine        序列化的工具类对象
     * @param <T>           反序列化对象类型
     * @return
     */
    public static <T> T deserialize(byte[] data, Class<T> clazz, Class<? extends ISerializer> engine){
        ISerializer serializer = CACHE.get(engine);
        if (serializer == null) {
            throw new RuntimeException("obtain serialize error");
        }
        try {
            return serializer.deserialize(data, clazz);
        } catch (Exception e) {
            throw new RuntimeException("serialize error");
        }
    }

}
