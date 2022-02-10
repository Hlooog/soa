package com.hl.soa.framework.invoker;

import com.google.common.collect.Maps;
import com.hl.soa.framework.model.IResponse;
import com.hl.soa.framework.model.IResponseWrapper;

import java.util.Map;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

/**
 * @author Hl
 * @create 2022/1/29 22:11
 */
public class InvokerResponseHolder {

    // 服务返回结果Map
    private static final Map<String, IResponseWrapper> responseMap = Maps.newConcurrentMap();
    // 清除过期结果
    private static final ExecutorService removeExpireKeyExecutor = Executors.newSingleThreadExecutor();

    static {
        // 删除超时未获取到结果的key，防止内存泄漏
        removeExpireKeyExecutor.execute(() -> {
            while (true) {
                try {
                    for (Map.Entry<String, IResponseWrapper> entry : responseMap.entrySet()) {
                        boolean isExpire = entry.getValue().isExpire();
                        if (isExpire) {
                            responseMap.remove(entry.getKey());
                        }
                        Thread.sleep(10);
                    }
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            }
        });
    }

    /**
     * 初始化返回结果容器，responseUniqueKey唯一标识
     * @param responseUniqueKey
     */
    public static void initResponseData(String responseUniqueKey){
        responseMap.put(responseUniqueKey, IResponseWrapper.of());
    }

    /**
     * 将Netty异步调用返回结果放入阻塞队列
     * @param response
     */
    public static void putResultValue(IResponse response){
        long currentTimeMillis = System.currentTimeMillis();
        IResponseWrapper responseWrapper =
                responseMap.get(response.getUniqueKey());
        responseWrapper.setResponseTime(currentTimeMillis);
        responseWrapper.getResponseQueue().add(response);
    }

    /**
     * 从阻塞队列中获取Netty异步返回结果值
     * @param responseUniqueKey
     * @param timeout
     * @return
     */
    public static IResponse getValue(String responseUniqueKey, long timeout){
        IResponseWrapper responseWrapper = responseMap.get(responseUniqueKey);
        try {
            return responseWrapper.getResponseQueue().poll(timeout, TimeUnit.SECONDS);
        } catch (InterruptedException e) {
            throw new RuntimeException(e);
        }
    }

}
