package com.hl.soa.framework.model;

import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.BlockingQueue;

/**
 * @author Hl
 * @create 2022/1/29 22:17
 */
public class IResponseWrapper {

    // 储存返回结果的阻塞队列
    private BlockingQueue<IResponse> responseQueue = new ArrayBlockingQueue<>(1);
    // 结果返回时间
    private long responseTime;

    /**
     * 计算返回结果是否已经过期
     *
     * @return
     */
    public boolean isExpire() {
        IResponse response = responseQueue.peek();
        if (response == null) {
            return false;
        }
        long timeout = response.getInvokeTimeout();
        return System.currentTimeMillis() - responseTime > timeout;
    }

    public static IResponseWrapper of() {
        return new IResponseWrapper();
    }

    public BlockingQueue<IResponse> getResponseQueue() {
        return responseQueue;
    }

    public long getResponseTime() {
        return responseTime;
    }

    public void setResponseTime(long responseTime) {
        this.responseTime = responseTime;
    }
}
