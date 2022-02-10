package com.hl.soa.framework.model;

import com.fasterxml.jackson.annotation.JsonIgnore;

import java.io.Serializable;
import java.lang.reflect.Method;

/**
 * 服务注册中心的服务提供者信息
 *
 * @author Hl
 * @create 2021/11/23 18:55
 */
public class ProviderService implements Serializable {

    private static final long serialVersionUID = -2215338588364720387L;

    private Class<?> serviceItf;
    private transient Object serviceObject;

    private String serverIp;
    private int serverPort;
    private long timeout;
    private int weight;
    private int workerThread;
    private String appKey;
    private String groupName;

    public ProviderService copy() {
        ProviderService target = new ProviderService(serviceItf,
                serviceObject,
                serverIp,
                serverPort,
                timeout,
                weight,
                workerThread,
                appKey,
                groupName
        );
        return target;
    }

    public ProviderService() {
    }

    public ProviderService(Class<?> serviceItf,
                           Object serviceObject,
                           String serverIp,
                           int serverPort,
                           long timeout,
                           int weight,
                           int workerThread,
                           String appKey,
                           String groupName) {
        this.serviceItf = serviceItf;
        this.serviceObject = serviceObject;
        this.serverIp = serverIp;
        this.serverPort = serverPort;
        this.timeout = timeout;
        this.weight = weight;
        this.workerThread = workerThread;
        this.appKey = appKey;
        this.groupName = groupName;
    }

    public Class<?> getServiceItf() {
        return serviceItf;
    }

    public void setServiceItf(Class<?> serviceItf) {
        this.serviceItf = serviceItf;
    }

    public Object getServiceObject() {
        return serviceObject;
    }

    public void setServiceObject(Object serviceObject) {
        this.serviceObject = serviceObject;
    }

    public String getServerIp() {
        return serverIp;
    }

    public void setServerIp(String serverIp) {
        this.serverIp = serverIp;
    }

    public int getServerPort() {
        return serverPort;
    }

    public void setServerPort(int serverPort) {
        this.serverPort = serverPort;
    }

    public long getTimeout() {
        return timeout;
    }

    public void setTimeout(long timeout) {
        this.timeout = timeout;
    }

    public int getWeight() {
        return weight;
    }

    public void setWeight(int weight) {
        this.weight = weight;
    }

    public int getWorkerThread() {
        return workerThread;
    }

    public void setWorkerThread(int workerThread) {
        this.workerThread = workerThread;
    }

    public String getAppKey() {
        return appKey;
    }

    public void setAppKey(String appKey) {
        this.appKey = appKey;
    }

    public String getGroupName() {
        return groupName;
    }

    public void setGroupName(String groupName) {
        this.groupName = groupName;
    }
}
