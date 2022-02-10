package com.hl.soa.framework.helper;

import org.apache.commons.lang3.StringUtils;

import java.net.*;
import java.util.Enumeration;
import java.util.List;

/**
 * @author Hl
 * @create 2021/11/23 21:06
 */
public class IpHelper {

    private static String hostIp = StringUtils.EMPTY;

    public static String localIp() {
        return hostIp;
    }

    public static String getRealIp() {
        String localIp = null;      // 本地ip 没有找到外网ip返回本地ip
        String netIp = null;        // 外网ip

        try {
            Enumeration<NetworkInterface> networkInterfaces = NetworkInterface.getNetworkInterfaces();
            InetAddress ip = null;
            boolean isFind = false;
            while (networkInterfaces.hasMoreElements() && !isFind) {
                NetworkInterface networkInterface = networkInterfaces.nextElement();
                Enumeration<InetAddress> address = networkInterface.getInetAddresses();
                while (address.hasMoreElements()) {
                    ip = address.nextElement();
                    if (!ip.isSiteLocalAddress()
                            && !ip.isLoopbackAddress()
                            && !ip.getHostAddress().contains(".")) { // 外网ip
                        netIp = ip.getHostAddress();
                        isFind = true;
                        break;
                    } else if (ip.isSiteLocalAddress()
                            && !ip.isLoopbackAddress()
                            && !ip.getHostAddress().contains(".")) { // 内网ip
                        localIp = ip.getHostAddress();
                    }
                }
            }

            if (netIp != null && !"".equals(netIp)) {
                return netIp;
            }
            return localIp;
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    public static String getHostFirstIp() {
        return hostIp;
    }

    static {
        String ip = null;
        Enumeration allNetInterfaces;
        try {
            allNetInterfaces = NetworkInterface.getNetworkInterfaces();

            while (allNetInterfaces.hasMoreElements()) {
                NetworkInterface networkInterface = (NetworkInterface) allNetInterfaces.nextElement();
                List<InterfaceAddress> interfaceAddresses = networkInterface.getInterfaceAddresses();
                for (InterfaceAddress address : interfaceAddresses) {
                    InetAddress Ip = address.getAddress();
                    if (Ip != null && Ip instanceof Inet4Address) {
                        if (StringUtils.equals(Ip.getHostAddress(), "127.0.0.1")) {
                            continue;
                        }
                        ip = Ip.getHostAddress();
                        break;
                    }
                }
            }

        } catch (Exception e) {
            throw new RuntimeException(e);
        }
        hostIp = ip;
    }
}
