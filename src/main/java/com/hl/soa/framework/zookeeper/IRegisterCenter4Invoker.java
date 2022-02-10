package com.hl.soa.framework.zookeeper;

import com.hl.soa.framework.model.InvokerService;
import com.hl.soa.framework.model.ProviderService;

import java.util.List;
import java.util.Map;

/**
 * @author Hl
 * @create 2021/12/4 13:30
 */
public interface IRegisterCenter4Invoker {

    /**
     * 消费端初始化服务提供者信息本地缓存
     * @param remoteAppKey
     * @param groupName
     */
    void initProviderMap(String remoteAppKey, String groupName);

    /**
     * 消费端获取服务提供者信息
     * @return
     */
    Map<String, List<ProviderService>> getServiceMetaDataMap4Consume();

    /**
     * 消费端将消费者信息注册到zk节点下
     * @param invokerService
     */
    void registerInvoker(InvokerService invokerService);
}
