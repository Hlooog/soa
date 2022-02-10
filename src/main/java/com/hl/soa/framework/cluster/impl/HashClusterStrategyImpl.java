package com.hl.soa.framework.cluster.impl;

import com.hl.soa.framework.cluster.ClusterStrategy;
import com.hl.soa.framework.helper.IpHelper;
import com.hl.soa.framework.model.ProviderService;

import java.util.List;

/**
 * hash软负载均衡算法实现
 * @author Hl
 * @create 2021/12/6 21:18
 */
public class HashClusterStrategyImpl implements ClusterStrategy {
    @Override
    public ProviderService select(List<ProviderService> providerServiceList) {
        String localIp = IpHelper.localIp();
        int hashCode = localIp.hashCode();
        int size = providerServiceList.size();
        return providerServiceList.get(hashCode % size).copy();
    }
}
