package com.hl.soa.framework.cluster;

/**
 * @author Hl
 * @create 2021/12/6 22:00
 */
public enum ClusterStrategyEnum {
    HASH(),
    POLLING(),
    WEIGHT_POLLING(),
    RANDOM(),
    WEIGHT_RANDOM()
    ;
    ClusterStrategyEnum() {
    }

}
