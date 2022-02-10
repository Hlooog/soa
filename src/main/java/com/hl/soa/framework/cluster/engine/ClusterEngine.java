package com.hl.soa.framework.cluster.engine;

import com.google.common.collect.Maps;
import com.hl.soa.framework.cluster.ClusterStrategy;
import com.hl.soa.framework.cluster.ClusterStrategyEnum;
import com.hl.soa.framework.cluster.impl.*;

import java.util.Map;

/**
 * @author Hl
 * @create 2022/1/30 22:48
 */
public class ClusterEngine {

    private static final Map<ClusterStrategyEnum, ClusterStrategy> clusterStrategyMap = Maps.newConcurrentMap();

    static {
        clusterStrategyMap.put(ClusterStrategyEnum.HASH, new HashClusterStrategyImpl());
        clusterStrategyMap.put(ClusterStrategyEnum.POLLING, new PollingClusterStrategyImpl());
        clusterStrategyMap.put(ClusterStrategyEnum.RANDOM, new RandomClusterStrategyImpl());
        clusterStrategyMap.put(ClusterStrategyEnum.WEIGHT_POLLING, new WeightPollingClusterStrategyImpl());
        clusterStrategyMap.put(ClusterStrategyEnum.WEIGHT_RANDOM, new WeightRandomClusterStrategyImpl());
    }

    public static ClusterStrategy queryClusterStrategy(ClusterStrategyEnum clusterStrategy) {
        if (clusterStrategy == null) {
            //默认选择随机算法
            return new RandomClusterStrategyImpl();
        }

        return clusterStrategyMap.get(clusterStrategy);
    }

}
