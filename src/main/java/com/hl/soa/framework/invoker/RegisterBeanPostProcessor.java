package com.hl.soa.framework.invoker;

import com.hl.soa.framework.annotation.Consumer;
import com.hl.soa.framework.cluster.ClusterStrategyEnum;
import org.springframework.beans.BeansException;
import org.springframework.beans.factory.config.BeanPostProcessor;
import org.springframework.stereotype.Component;

import java.lang.reflect.Field;

/**
 * @author Hl
 * @create 2022/2/4 13:07
 */
@Component
public class RegisterBeanPostProcessor implements BeanPostProcessor {

    @Override
    public Object postProcessAfterInitialization(Object bean, String beanName) throws BeansException {

        Field[] fields = bean.getClass().getDeclaredFields();
        for (Field field : fields) {
            if (field.isAnnotationPresent(Consumer.class)) {
                try {
                    convert(field, bean);
                } catch (IllegalAccessException e) {
                    throw new RuntimeException(e);
                }
            }
        }
        return bean;
    }

    private void convert(Field field, Object bean) throws IllegalAccessException {
        Consumer consumer = field.getAnnotation(Consumer.class);
        ClusterStrategyEnum cluster = consumer.cluster();
        long timeout = consumer.timeout();
        InvokerProxyBeanFactory singleton = InvokerProxyBeanFactory.singleton(field.getType(), timeout, cluster);
        field.setAccessible(true);
        field.set(bean, singleton.getProxy());
    }

    @Override
    public Object postProcessBeforeInitialization(Object bean, String beanName) throws BeansException {
        return bean;
    }
}
