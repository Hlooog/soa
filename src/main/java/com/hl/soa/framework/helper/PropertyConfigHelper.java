package com.hl.soa.framework.helper;

import com.hl.soa.framework.serialization.serializer.ISerializer;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.util.Properties;

/**
 * @author Hl
 * @create 2021/11/22 19:11
 */
public class PropertyConfigHelper {
    private static final String PROPERTY_CLASSPATH = "/soa.properties";
    private static final Properties properties = new Properties();

    // zk服务地址
    private static String zkService = "";
    // zk session超时时间
    private static int zkSessionTimeOut;
    // zk connection超时时间
    private static int zkConnectionTimeOut;
    // 序列化算法类型
    private static Class<? extends ISerializer> serialize;
    // 每个服务端提供者的Netty连接数
    private static int channelConnectionSize;
    // 服务端接口
    private static String serverPort;
    // 权重
    private static int weight;
    // appkey
    private static String appKey;
    //gourpName
    private static String groupName;

    /**
     * 初始化
     */
    static {
        InputStream is = null;
        try {
            is = PropertyConfigHelper.class.getResourceAsStream(PROPERTY_CLASSPATH);
            properties.load(is);
            zkService = properties.getProperty("zk_service");
            zkConnectionTimeOut = Integer.valueOf(properties.getProperty("zk_connection_timeout"));
            zkSessionTimeOut = Integer.valueOf(properties.getProperty("zk_session_timeout"));
            String classPath = properties.getProperty("serialize_type");
            serialize = (Class<? extends ISerializer>) Class.forName(classPath);
            if (serialize == null) {
                throw new RuntimeException("load serialize error");
            }
            channelConnectionSize = Integer.valueOf(properties.getProperty("channel_connection_size"));
            serverPort = properties.getProperty("server_port");
            weight = Integer.parseInt(properties.getProperty("weight"));
            appKey = properties.getProperty("app_key");
            groupName = properties.getProperty("group_name");
        } catch (Exception e) {
            throw new RuntimeException(e);
        } finally {
            if (is != null) {
                try {
                    is.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }
    }

    public static String getZkService() {
        return zkService;
    }

    public static Integer getZkSessionTimeOut() {
        return zkSessionTimeOut;
    }

    public static Integer getZkConnectionTimeOut() {
        return zkConnectionTimeOut;
    }

    public static Class<? extends ISerializer> getSerialize() {
        return serialize;
    }

    public static int getChannelConnectionSize() {
        return channelConnectionSize;
    }

    public static String getServerPort() {
        return serverPort;
    }

    public static int getWeight() {
        return weight;
    }

    public static String getAppKey() {
        return appKey;
    }

    public static String getGroupName() {
        return groupName;
    }
}
