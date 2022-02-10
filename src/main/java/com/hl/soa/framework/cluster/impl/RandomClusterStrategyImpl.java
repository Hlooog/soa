package com.hl.soa.framework.cluster.impl;

import com.hl.soa.framework.cluster.ClusterStrategy;
import com.hl.soa.framework.model.ProviderService;
import org.apache.commons.lang3.RandomUtils;

import java.util.List;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;

/**
 * 随机软负载均衡算法实现
 * @author Hl
 * @create 2021/12/6 21:47
 */
public class RandomClusterStrategyImpl implements ClusterStrategy {


    @Override
    public ProviderService select(List<ProviderService> providerServiceList) {
        int size = providerServiceList.size();
        int index = RandomUtils.nextInt(0, size - 1);
        return providerServiceList.get(index).copy();
    }
}
