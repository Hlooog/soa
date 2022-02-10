package com.hl.soa.framework.cluster.impl;

import com.hl.soa.framework.cluster.ClusterStrategy;
import com.hl.soa.framework.model.ProviderService;

import java.util.List;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;

/**
 * 加权轮询软负载均衡算法实现
 * @author Hl
 * @create 2021/12/6 21:26
 */
public class WeightPollingClusterStrategyImpl implements ClusterStrategy {

    private int weight = 0;
    private Lock lock = new ReentrantLock();

    @Override
    public ProviderService select(List<ProviderService> providerServiceList) {
        try {
            lock.tryLock( 10, TimeUnit.MILLISECONDS);
            int weightSum = 0;
            for (ProviderService service : providerServiceList) {
                weightSum = weightSum + service.getWeight();
            }
            int tmp = weight;
            weight = (weight + 1) % weightSum;
            for (ProviderService service : providerServiceList) {
                if (tmp < service.getWeight()) return service.copy();
                tmp -= service.getWeight();
            }
        } catch (InterruptedException e) {
            e.printStackTrace();
        } finally {
            lock.unlock();
        }
        return providerServiceList.get(0).copy();
    }
}
