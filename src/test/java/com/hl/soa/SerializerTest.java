package com.hl.soa;


import com.hl.PayResponse;
import com.hl.soa.framework.config.SpringConfig;
import com.hl.soa.framework.provider.NettyServer;
import com.hl.soa.framework.serialization.serializer.DefaultJavaSerializer;
import org.junit.Test;
import org.springframework.context.annotation.AnnotationConfigApplicationContext;

/**
 * @author Hl
 * @create 2021/11/20 11:58
 */
public class SerializerTest {

    @Test
    public void test() {
        DefaultJavaSerializer defaultJavaSerializer = new DefaultJavaSerializer();
        PayResponse response = new PayResponse();
        response.setCode(200);
        response.setMsg("成功");
        byte[] serialize = defaultJavaSerializer.serialize(response);
        PayResponse deserialize = defaultJavaSerializer.deserialize(serialize, PayResponse.class);
        System.out.println(response);
        System.out.println(serialize);
        System.out.println(deserialize.getCode() + "=========" + deserialize.getMsg());
    }

}
