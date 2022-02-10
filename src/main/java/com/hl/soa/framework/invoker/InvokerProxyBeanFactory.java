package com.hl.soa.framework.invoker;

import com.hl.soa.framework.cluster.ClusterStrategyEnum;
import com.hl.soa.framework.cluster.engine.ClusterEngine;
import com.hl.soa.framework.helper.PropertyConfigHelper;
import com.hl.soa.framework.model.IRequest;
import com.hl.soa.framework.model.IResponse;
import com.hl.soa.framework.model.ProviderService;
import com.hl.soa.framework.zookeeper.IRegisterCenter4Invoker;
import com.hl.soa.framework.zookeeper.RegisterCenter;

import java.lang.reflect.InvocationHandler;
import java.lang.reflect.Method;
import java.lang.reflect.Proxy;
import java.net.InetSocketAddress;
import java.util.List;
import java.util.UUID;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;

/**
 * @author Hl
 * @create 2022/1/30 22:37
 */
public class InvokerProxyBeanFactory implements InvocationHandler {

    private ExecutorService fixedThreadPool = null;

    // 服务接口
    private Class<?> targetInterface;
    // 超时时间
    private long consumeTimeout;
    // 调用者线程数
    private static int threadWorkerNumber = 10;
    // 负载均衡算法
    private ClusterStrategyEnum clusterStrategy;

    public InvokerProxyBeanFactory(Class<?> targetInterface, long consumeTimeout, ClusterStrategyEnum clusterStrategy) {
        this.targetInterface = targetInterface;
        this.consumeTimeout = consumeTimeout;
        this.clusterStrategy = clusterStrategy;
    }

    @Override
    public Object invoke(Object proxy, Method method, Object[] args) throws Throwable {

        // 服务接口名称
        java.lang.String serviceKey = targetInterface.getName();
        // 获取某个接口的服务提供者列表
        IRegisterCenter4Invoker center = RegisterCenter.getInstance();
        List<ProviderService> providerServices = center.getServiceMetaDataMap4Consume().get(serviceKey);
        // 根据软负载均衡策略从服务提供者列表选取本次调用的服务提供者
        ProviderService provider = ClusterEngine.queryClusterStrategy(clusterStrategy).select(providerServices);
        ProviderService newProvider = provider.copy();
        newProvider.setServiceItf(targetInterface);

        // 构建调用信息
        IRequest request = new IRequest();
        // 设置本次调用唯一标识
        request.setUniqueKey(UUID.randomUUID().toString() + "-" + Thread.currentThread().getId());
        // 设置本次调用服务提供者信息
        request.setProviderService(newProvider);
        // 设置本次调用超时时间
        request.setInvokeTimeout(consumeTimeout);
        // 设置本次调用方法名称
        request.setInvokedMethodName(method.getName());
        // 设置本次调用方法参数
        request.setArgs(args);
        request.setAppName(PropertyConfigHelper.getAppKey());

        try {
            // 构建用来发起调用的线程池
            if (fixedThreadPool == null) {
                synchronized (InvokerProxyBeanFactory.class) {
                    if (fixedThreadPool == null) {
                        fixedThreadPool = Executors.newFixedThreadPool(threadWorkerNumber);
                    }
                }
            }
            String serverIp = request.getProviderService().getServerIp();
            int serverPort = request.getProviderService().getServerPort();
            InetSocketAddress socket = new InetSocketAddress(serverIp, serverPort);
            Future<IResponse> future =
                    fixedThreadPool.submit(InvokerServiceCallable.of(socket, request));
            IResponse response = future.get(request.getInvokeTimeout(), TimeUnit.SECONDS);
            if (response != null) {
                return response.getResult();
            }
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
        return null;
    }

    public Object getProxy(){
        return  Proxy.newProxyInstance(Thread.currentThread().getContextClassLoader(), new Class<?>[]{targetInterface}, this);
    }


    private static volatile InvokerProxyBeanFactory singleton;

    public static InvokerProxyBeanFactory singleton(Class<?> targetInterface, long consumeTimeout, ClusterStrategyEnum clusterStrategy) {
        if (null == singleton) {
            synchronized (InvokerProxyBeanFactory.class) {
                if (null == null) {
                    singleton = new InvokerProxyBeanFactory(targetInterface, consumeTimeout, clusterStrategy);
                }
            }
        }
        return singleton;
    }
}
