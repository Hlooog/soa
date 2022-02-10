package com.hl.soa.framework.zookeeper;

import com.google.common.base.Function;
import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import com.google.common.collect.Sets;
import com.hl.soa.framework.helper.IpHelper;
import com.hl.soa.framework.helper.PropertyConfigHelper;
import com.hl.soa.framework.model.InvokerService;
import com.hl.soa.framework.model.ProviderService;
import org.I0Itec.zkclient.ZkClient;
import org.I0Itec.zkclient.serialize.SerializableSerializer;
import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.collections.MapUtils;
import org.apache.commons.lang3.ClassUtils;
import org.apache.commons.lang3.StringUtils;

import java.util.*;

/**
 * 注册中心
 *
 * @author Hl
 * @create 2021/12/4 13:35
 */
public class RegisterCenter implements IRegisterCenter4Invoker, IRegisterCenter4Provider {

    private static RegisterCenter registerCenter = new RegisterCenter();

    // 服务提供者列表 key 服务提供者接口 value 服务提供者服务方法列表
    private static final Map<String, List<ProviderService>> providerServiceMap = Maps.newConcurrentMap();

    // 服务端zk服务元信息，选择服务（第一次直接从zk拉取， 后续由zk的监听机制更新）
    private static final Map<String, List<ProviderService>> serviceMetaDataMap4Consume = Maps.newConcurrentMap();

    private static String ZK_SERVICE = PropertyConfigHelper.getZkService();
    private static int ZK_SESSION_TIME_OUT = PropertyConfigHelper.getZkSessionTimeOut();
    private static int ZK_CONNECTION_TIME_OUT = PropertyConfigHelper.getZkConnectionTimeOut();
    private static String ROOT_PATH = "/config_register";
    private static String PROVIDER_TYPE = "provider";
    private static String INVOKER_TYPE = "consumer";
    private static volatile ZkClient zkClient = null;

    public RegisterCenter() {
    }

    public static RegisterCenter getInstance() {
        return registerCenter;
    }


    @Override
    public void initProviderMap(String remoteAppKey, String groupName) {
        if (MapUtils.isEmpty(serviceMetaDataMap4Consume)) {
            serviceMetaDataMap4Consume.putAll(fetchOrUpdateServiceMetaData(remoteAppKey, groupName));
        }
    }

    @Override
    public Map<String, List<ProviderService>> getServiceMetaDataMap4Consume() {
        return serviceMetaDataMap4Consume;
    }

    @Override
    public void registerInvoker(InvokerService invokerService) {
        if (invokerService == null) return;

        synchronized (RegisterCenter.class) {
            if (zkClient == null)
                zkClient = new ZkClient(ZK_SERVICE, ZK_SESSION_TIME_OUT, ZK_CONNECTION_TIME_OUT, new SerializableSerializer());
            if (!zkClient.exists(ROOT_PATH)) zkClient.createPersistent(ROOT_PATH, true);
            String remoteAppKey = invokerService.getRemoteAppKey();
            String groupName = invokerService.getGroupName();
            String servicePath = ROOT_PATH + "/" + remoteAppKey + "/" + groupName + "/" + INVOKER_TYPE;
            if (!zkClient.exists(servicePath)) zkClient.createPersistent(servicePath, true);
            String localIp = IpHelper.localIp();
            String currentServiceIpNode = servicePath + "/" + localIp;
            if (!zkClient.exists(currentServiceIpNode)) zkClient.createEphemeral(currentServiceIpNode);
        }
    }

    @Override
    public void registerProvider(List<ProviderService> serviceMetaData) {
        if (CollectionUtils.isEmpty(serviceMetaData)) {
            return;
        }
        synchronized (RegisterCenter.class) {
            for (ProviderService provider : serviceMetaData) {
                String serviceItfKey = provider.getServiceItf().getName();
                List<ProviderService> providers = providerServiceMap.get(serviceItfKey);
                if (providers == null) {
                    providers = Lists.newArrayList();
                    providerServiceMap.put(serviceItfKey, providers);
                }
                providers.add(provider);
            }
            if (zkClient == null) {
                zkClient = new ZkClient(ZK_SERVICE, ZK_SESSION_TIME_OUT, ZK_CONNECTION_TIME_OUT, new SerializableSerializer());
            }
            // 创建zk命名空间/当前部署应用app命名空间/
            String appKey = serviceMetaData.get(0).getAppKey();
            String zkPath = ROOT_PATH + "/" + appKey;
            if (!zkClient.exists(zkPath)) {
                zkClient.createPersistent(zkPath, true);
            }
            for (Map.Entry<String, List<ProviderService>> entry : providerServiceMap.entrySet()) {
                ProviderService service = entry.getValue().get(0);
                // 服务分组
                String groupName = service.getGroupName();
                // 创建服务提供者
                String serviceNode = entry.getKey();
                String servicePath = zkPath + "/" + groupName + "/" + PROVIDER_TYPE + "/" + serviceNode;
                if (!zkClient.exists(servicePath)) {
                    zkClient.createPersistent(servicePath, true);
                }
                // 创建当前服务器节点
                int serverPort = service.getServerPort();
                int weight = service.getWeight();
                int workThreads = service.getWorkerThread();
                String localIp = IpHelper.localIp();
                String currentServiceIpNode = servicePath + "/" + localIp + "|" + serverPort + "|" + weight + "|" + workThreads + "|" + groupName;
                if (!zkClient.exists(currentServiceIpNode)) {
                    // 创建临时节点
                    zkClient.createEphemeral(currentServiceIpNode);
                }
                // 监听注册服务的变化， 同时更新本地缓存
                zkClient.subscribeChildChanges(servicePath, (parentPath, currentChildList) -> {
                    if (currentChildList == null) currentChildList = Lists.newArrayList();
                    // 存活的服务ip列表
                    HashSet<String> activityServiceIpSet = Sets.newHashSet(Lists.transform(currentChildList, new Function<String, String>() {
                        @Override
                        public String apply(String s) {
                            return StringUtils.split(s, '|')[0];
                        }
                    }));
                    refreshActivityService(activityServiceIpSet);
                });
            }
        }
    }


    @Override
    public Map<String, List<ProviderService>> getProviderServiceMap() {
        return providerServiceMap;
    }


    private void refreshActivityService(Set<String> serviceIpSet) {
        if (serviceIpSet == null) serviceIpSet = Sets.newHashSet();

        Map<String, List<ProviderService>> currentServiceMetaDataMap = Maps.newConcurrentMap();
        for (Map.Entry<String, List<ProviderService>> entry : providerServiceMap.entrySet()) {
            String key = entry.getKey();
            List<ProviderService> value = entry.getValue();
            List<ProviderService> serviceMetaDataList = currentServiceMetaDataMap.getOrDefault(key, Lists.newArrayList());
            for (ProviderService service : value) {
                if (serviceIpSet.contains(service.getServerIp())) {
                    serviceMetaDataList.add(service);
                }
            }
            currentServiceMetaDataMap.put(key, serviceMetaDataList);
        }
        providerServiceMap.clear();
        providerServiceMap.putAll(currentServiceMetaDataMap);
    }

    private Map<String, List<ProviderService>> fetchOrUpdateServiceMetaData(String remoteAppKey, String groupName) {
        final Map<String, List<ProviderService>> providerServiceMap = Maps.newConcurrentMap();
        synchronized (RegisterCenter.class) {
            if (zkClient == null)
                zkClient = new ZkClient(ZK_SERVICE, ZK_SESSION_TIME_OUT, ZK_CONNECTION_TIME_OUT, new SerializableSerializer());
        }
        // 从zk获取服务提供者列表
        String providerPath = ROOT_PATH + "/" + remoteAppKey + "/" + groupName + "/" + PROVIDER_TYPE;
        if (zkClient.exists(providerPath)) {

            List<String> providerServices = zkClient.getChildren(providerPath);
            for (String serviceName : providerServices) {
                String servicePath = providerPath + "/" + serviceName;
                List<String> ipPathList = zkClient.getChildren(servicePath);
                for (String ipPath : ipPathList) {
                    String[] message = StringUtils.split(ipPath, '|');
                    String serverIp = message[0];
                    String serverPort = message[1];
                    int weight = Integer.valueOf(message[2]);
                    int workThreads = Integer.valueOf(message[3]);
                    String group = message[4];

                    List<ProviderService> providerServiceList = providerServiceMap.getOrDefault(serviceName, Lists.newArrayList());
                    ProviderService providerService = new ProviderService();
                    try {
                        providerService.setServiceItf(ClassUtils.getClass(serviceName));
                    } catch (ClassNotFoundException e) {
                        throw new RuntimeException(e);
                    }
                    providerService.setServerIp(serverIp);
                    providerService.setServerPort(Integer.valueOf(serverPort));
                    providerService.setWeight(weight);
                    providerService.setWorkerThread(workThreads);
                    providerService.setGroupName(group);
                    providerServiceList.add(providerService);
                    providerServiceMap.put(serviceName, providerServiceList);
                }

                zkClient.subscribeChildChanges(servicePath, (parentPath, currentChildList) -> {
                    if (currentChildList == null) currentChildList = Lists.newArrayList();
                    Set<String> serviceMetaSet = Sets.newHashSet(Lists.transform(currentChildList, new Function<String, String>() {
                        @Override
                        public String apply(String s) {
                            return StringUtils.split(s, '|')[0];
                        }
                    }));
                    refreshServiceMetaMap(serviceMetaSet);
                });
            }
        }
        return providerServiceMap;
    }

    private void refreshServiceMetaMap(Set<String> serviceMetaSet) {
        if (serviceMetaSet == null) serviceMetaSet = Sets.newHashSet();
        Map<String, List<ProviderService>> currentServiceMetaMap = Maps.newConcurrentMap();
        for (Map.Entry<String, List<ProviderService>> entry : serviceMetaDataMap4Consume.entrySet()) {
            String serviceItfKey = entry.getKey();
            List<ProviderService> serviceList = entry.getValue();
            List<ProviderService> providerServiceList = currentServiceMetaMap.getOrDefault(serviceItfKey, Lists.newArrayList());
            for (ProviderService serviceMetaData : serviceList) {
                if (serviceMetaSet.contains(serviceMetaData.getServerIp())) {
                    providerServiceList.add(serviceMetaData);
                }
            }
            currentServiceMetaMap.put(serviceItfKey, providerServiceList);
        }
        serviceMetaDataMap4Consume.clear();
        serviceMetaDataMap4Consume.putAll(currentServiceMetaMap);
    }
}
