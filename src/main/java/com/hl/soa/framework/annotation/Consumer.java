package com.hl.soa.framework.annotation;

import com.hl.soa.framework.cluster.ClusterStrategyEnum;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * @author Hl
 * @create 2022/2/4 13:02
 */
@Target(ElementType.FIELD)
@Retention(RetentionPolicy.RUNTIME)
public @interface Consumer {

    // 负载均衡策略
    ClusterStrategyEnum cluster() default ClusterStrategyEnum.POLLING;

    // 调用服务超时时间
    long timeout() default 1000;

}
