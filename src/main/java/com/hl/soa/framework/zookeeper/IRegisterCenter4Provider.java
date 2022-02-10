package com.hl.soa.framework.zookeeper;

import com.hl.soa.framework.model.ProviderService;

import java.util.List;
import java.util.Map;

/**
 * @author Hl
 * @create 2021/12/4 13:27
 */
public interface IRegisterCenter4Provider {

    /**
     * 服务提供者信息注册到zk对应的节点下
     * @param serviceMetaData
     */
    void registerProvider(final List<ProviderService> serviceMetaData);

    /**
     * 服务端获取服务提供者信息
     * @return
     */
    Map<String, List<ProviderService>> getProviderServiceMap();

}
