package com.hl.soa.framework.model;

import java.io.Serializable;
import java.lang.reflect.Method;

/**
 * 服务调用者
 *
 * @author Hl
 * @create 2021/11/23 19:00
 */
public class InvokerService implements Serializable {

    private static final long serialVersionUID = -315874840799008353L;


    private String invokerIp;
    private int invokerPort;
    // 服务提供者唯一标识
    private String remoteAppKey;
    // 服务分组组名
    private String groupName = "default";

    public InvokerService() {
    }

    public InvokerService(
                          String invokerIp,
                          int invokerPort,
                          String remoteAppKey,
                          String groupName) {

        this.invokerIp = invokerIp;
        this.invokerPort = invokerPort;
        this.remoteAppKey = remoteAppKey;
        this.groupName = groupName;
    }


    public String getInvokerIp() {
        return invokerIp;
    }

    public void setInvokerIp(String invokerIp) {
        this.invokerIp = invokerIp;
    }

    public int getInvokerPort() {
        return invokerPort;
    }

    public void setInvokerPort(int invokerPort) {
        this.invokerPort = invokerPort;
    }


    public String getRemoteAppKey() {
        return remoteAppKey;
    }

    public void setRemoteAppKey(String remoteAppKey) {
        this.remoteAppKey = remoteAppKey;
    }

    public String getGroupName() {
        return groupName;
    }

    public void setGroupName(String groupName) {
        this.groupName = groupName;
    }
}
