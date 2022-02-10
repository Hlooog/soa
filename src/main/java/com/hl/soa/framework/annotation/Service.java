package com.hl.soa.framework.annotation;

import org.springframework.stereotype.Component;

import java.lang.annotation.*;

/**
 * @author Hl
 * @create 2021/11/25 17:59
 */
@Target({ElementType.TYPE})
@Retention(RetentionPolicy.RUNTIME)
@Component
public @interface Service {
    // 服务线程数
    int workerThread() default 10;
    // 服务超时时间
    long timeout() default 1000;
}
