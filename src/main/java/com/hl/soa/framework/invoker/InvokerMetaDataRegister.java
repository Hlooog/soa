package com.hl.soa.framework.invoker;

import com.hl.soa.framework.helper.PropertyConfigHelper;
import com.hl.soa.framework.model.InvokerService;
import com.hl.soa.framework.model.ProviderService;
import com.hl.soa.framework.zookeeper.IRegisterCenter4Invoker;
import com.hl.soa.framework.zookeeper.RegisterCenter;
import org.apache.commons.collections.MapUtils;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Map;

/**
 * @author Hl
 * @create 2022/2/4 13:34
 */
@Component
public class InvokerMetaDataRegister implements InitializingBean {
    // 服务提供者唯一标识
    private String remoteAppKey = PropertyConfigHelper.getAppKey();
    // 服务分组组名
    private String groupName = PropertyConfigHelper.getGroupName();

    @Override
    public void afterPropertiesSet() throws Exception {
        IRegisterCenter4Invoker center = RegisterCenter.getInstance();

        center.initProviderMap(remoteAppKey, groupName);

        // 初始化Netty Channel
        Map<String, List<ProviderService>> providerMap = center.getServiceMetaDataMap4Consume();
        if (!MapUtils.isEmpty(providerMap)) {
            NettyChannelPoolFactory.getInstance().initChannelPoolFactory(providerMap);
        }


        //将消费者信息注册到注册中心
        InvokerService invoker = new InvokerService();
        invoker.setRemoteAppKey(remoteAppKey);
        invoker.setGroupName(groupName);
        center.registerInvoker(invoker);
    }
}
