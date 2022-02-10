package com.hl.soa.framework.model;

import java.io.Serializable;

/**
 * @author Hl
 * @create 2021/11/23 18:45
 */
public class IResponse implements Serializable {

    private static final long serialVersionUID = -7981653666342510474L;

    // UUID 返回值唯一标识
    private String uniqueKey;
    // 客户端指定的服务超时时间
    private long invokeTimeout;
    // 接口调用返回对象
    private Object result;

    public String getUniqueKey() {
        return uniqueKey;
    }

    public void setUniqueKey(String uniqueKey) {
        this.uniqueKey = uniqueKey;
    }

    public long getInvokeTimeout() {
        return invokeTimeout;
    }

    public void setInvokeTimeout(long invokeTimeout) {
        this.invokeTimeout = invokeTimeout;
    }

    public Object getResult() {
        return result;
    }

    public void setResult(Object result) {
        this.result = result;
    }
}
