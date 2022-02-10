package com.hl.soa.framework.cluster;

import com.hl.soa.framework.model.ProviderService;

import java.util.List;

/**
 *
 * @author Hl
 * @create 2021/12/6 21:15
 */
public interface ClusterStrategy {

    ProviderService select(List<ProviderService> providerServiceList);

}
