package com.hl.soa.framework.cluster.impl;

import com.hl.soa.framework.cluster.ClusterStrategy;
import com.hl.soa.framework.model.ProviderService;

import java.util.List;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;

/**
 * 轮询软负载均衡算法实现
 * @author Hl
 * @create 2021/12/6 21:21
 */
public class PollingClusterStrategyImpl implements ClusterStrategy {

    private int index = 0;

    private Lock lock = new ReentrantLock();


    @Override
    public ProviderService select(List<ProviderService> providerServiceList) {

        ProviderService service = null;

        try {
            lock.tryLock(10, TimeUnit.MILLISECONDS);
            int size = providerServiceList.size();
            service = providerServiceList.get(index % size);
            index = (index + 1) % Integer.MAX_VALUE;
            return service.copy();
        } catch (InterruptedException e) {
            e.printStackTrace();
        } finally {
            lock.unlock();
        }
        return providerServiceList.get(0).copy();
    }
}
