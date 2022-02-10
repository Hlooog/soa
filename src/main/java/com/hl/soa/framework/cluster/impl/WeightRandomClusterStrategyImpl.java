package com.hl.soa.framework.cluster.impl;

import com.hl.soa.framework.cluster.ClusterStrategy;
import com.hl.soa.framework.model.ProviderService;
import org.apache.commons.lang3.RandomUtils;

import java.util.List;

/**
 * 加权随机软负载均衡算法实现
 *
 * @author Hl
 * @create 2021/12/6 21:49
 */
public class WeightRandomClusterStrategyImpl implements ClusterStrategy {

    @Override
    public ProviderService select(List<ProviderService> providerServiceList) {

        int weightSum = 0;
        for (ProviderService service : providerServiceList) {
            weightSum += service.getWeight();
        }
        int tmp = RandomUtils.nextInt(0, weightSum - 1);
        for (ProviderService service : providerServiceList) {
            if (tmp <= service.getWeight()) return service.copy();
            tmp -= service.getWeight();
        }
        return providerServiceList.get(0);
    }
}
