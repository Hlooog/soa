package com.hl.soa.framework.provider;

import com.google.common.collect.Lists;
import com.hl.soa.framework.annotation.Service;
import com.hl.soa.framework.helper.IpHelper;
import com.hl.soa.framework.helper.PropertyConfigHelper;
import com.hl.soa.framework.model.ProviderService;
import com.hl.soa.framework.zookeeper.RegisterCenter;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationContext;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Map;

/**
 * 服务bean发布入口
 *
 * @author Hl
 * @create 2021/11/23 20:58
 */
@Component
public class RegisterProviderService implements InitializingBean {

    @Autowired
    private ApplicationContext application;

    // 服务端口
    private String serverPort = PropertyConfigHelper.getServerPort();
    // 权重
    private int weight = PropertyConfigHelper.getWeight();
    // 服务标识
    private String appKey = PropertyConfigHelper.getAppKey();
    // 组名
    private String groupName = PropertyConfigHelper.getGroupName();


    @Override
    public void afterPropertiesSet() throws Exception {
        // 启动netty服务
        NettyServer.getInstance().start(Integer.parseInt(serverPort));

        // 注册服务到zookeeper
        List<ProviderService> providerServices = buildProviderServiceInfos();
        RegisterCenter registerCenter = RegisterCenter.getInstance();
        registerCenter.registerProvider(providerServices);
    }

    private List<ProviderService> buildProviderServiceInfos() {
        List<ProviderService> providerList = Lists.newArrayList();

        Map<String, Object> beans = application.getBeansWithAnnotation(Service.class);

        for (Object serviceObject : beans.values()) {
            Service service = serviceObject.getClass().getAnnotation(Service.class);
            long timeout = service.timeout();
            int workerThread = service.workerThread();
            Class<?>[] interfaces = serviceObject.getClass().getInterfaces();
//            Method[] methods = serviceObject.getClass().getDeclaredMethods();
            /*for (Method method : methods) {

                ProviderService providerService = new ProviderService(interfaces[0],
                        serviceObject,
                        method,
                        IpHelper.localIp(),
                        Integer.valueOf(serverPort),
                        timeout,
                        weight,
                        workerThread,
                        appKey,
                        groupName);

                providerList.add(providerService);
            }*/

            ProviderService providerService = new ProviderService(interfaces[0],
                    serviceObject,
                    IpHelper.localIp(),
                    Integer.valueOf(serverPort),
                    timeout,
                    weight,
                    workerThread,
                    appKey,
                    groupName);
            providerList.add(providerService);
        }

        return providerList;

    }
}
