package com.wif.settings.reflex;

import android.annotation.SuppressLint;
import android.content.Context;

import java.lang.reflect.Constructor;
import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.net.InetAddress;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

public class EthernetReflex {

    public static final String ETHERNET = "ethernet";

    /**
     * 设置以太网静态IP地址
     *
     * @param address ip地址
     * @param mask    子网掩码
     * @param gate    网关
     * @param dns1     dns
     * @param dns2     dns
     */
    @SuppressLint({"WrongConstant", "PrivateApi"})
    public static void setEthernetStaticIp(Context context, String address, String mask, String gate,
                                              String dns1, String dns2) {
        try {
            Class<?> ethernetManagerCls = Class.forName("android.net.EthernetManager");
            Object ethManager = context.getSystemService(ETHERNET);
            Object staticIpConfiguration = newStaticIpConfiguration(address, gate, mask, dns1, dns2);
            Object ipConfiguration = newIpConfiguration(staticIpConfiguration);
            Method setConfigurationMethod = ethernetManagerCls.getDeclaredMethod("setConfiguration", ipConfiguration.getClass());
            setConfigurationMethod.invoke(ethManager, ipConfiguration);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    /**
     * 获取StaticIpConfiguration实例
     */
    @SuppressLint("PrivateApi")
    private static Object newStaticIpConfiguration(String address, String gate, String mask,
                                                   String dns1, String dns2) throws Exception {
        Class<?> staticIpConfigurationCls = Class.forName("android.net.StaticIpConfiguration");
        Object staticIpConfiguration = staticIpConfigurationCls.newInstance();
        Field ipAddress = staticIpConfigurationCls.getField("ipAddress");
        Field gateway = staticIpConfigurationCls.getField("gateway");
        Field domains = staticIpConfigurationCls.getField("domains");
        Field dnsServers = staticIpConfigurationCls.getField("dnsServers");
        //设置ipAddress
        ipAddress.set(staticIpConfiguration, newLinkAddress(address, mask));
        //设置网关
        gateway.set(staticIpConfiguration, InetAddress.getByName(gate));
        //设置掩码
        domains.set(staticIpConfiguration, mask);
        //设置dns
        ArrayList<InetAddress> dnsList = (ArrayList<InetAddress>) dnsServers.get(staticIpConfiguration);
        dnsList.add(InetAddress.getByName(dns1));
        dnsList.add(InetAddress.getByName(dns2));
        return staticIpConfiguration;
    }

    /**
     * 获取IpConfiguration实例
     */
    @SuppressLint("PrivateApi")
    private static Object newIpConfiguration(Object staticIpConfiguration) throws Exception {
        Class<?> ipConfigurationCls = Class.forName("android.net.IpConfiguration");
        Object ipConfiguration = ipConfigurationCls.newInstance();
        //设置StaticIpConfiguration
        Field staticIpConfigurationField = ipConfigurationCls.getField("staticIpConfiguration");
        staticIpConfigurationField.set(ipConfiguration, staticIpConfiguration);
        //获取ipAssignment、proxySettings的枚举值
        Map<String, Object> ipConfigurationEnum = getIpConfigurationEnum(ipConfigurationCls);
        //设置ipAssignment
        Field ipAssignment = ipConfigurationCls.getField("ipAssignment");
        ipAssignment.set(ipConfiguration, ipConfigurationEnum.get("IpAssignment.STATIC"));
        //设置proxySettings
        Field proxySettings = ipConfigurationCls.getField("proxySettings");
        proxySettings.set(ipConfiguration, ipConfigurationEnum.get("ProxySettings.STATIC"));
        return ipConfiguration;
    }

    /**
     * 获取IpConfiguration的枚举值
     */
    private static Map<String, Object> getIpConfigurationEnum(Class<?> ipConfigurationCls) {
        Map<String, Object> enumMap = new HashMap<>();
        Class<?>[] enumClass = ipConfigurationCls.getDeclaredClasses();
        for (Class<?> enumC : enumClass) {
            Object[] enumConstants = enumC.getEnumConstants();
            if (enumConstants == null) continue;
            for (Object enu : enumConstants) {
                enumMap.put(enumC.getSimpleName() + "." + enu.toString(), enu);
            }
        }
        return enumMap;
    }


    /**
     * 获取LinkAddress实例
     */
    private static Object newLinkAddress(String address, String mask) throws Exception {
        Class<?> linkAddressCls = Class.forName("android.net.LinkAddress");
        Constructor<?> linkAddressConstructor = linkAddressCls.getDeclaredConstructor(InetAddress.class, int.class);
        return linkAddressConstructor.newInstance(InetAddress.getByName(address), getPrefixLength(mask));
    }

    /**
     * 获取长度
     */
    private static int getPrefixLength(String mask) {
        String[] strs = mask.split("\\.");
        int count = 0;
        for (String str : strs) {
            if (str.equals("255")) {
                ++count;
            }
        }
        return count * 8;
    }

}
