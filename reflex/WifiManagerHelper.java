package com.wif.baseservice.reflex;

import android.annotation.SuppressLint;
import android.content.Context;
import android.net.wifi.ScanResult;
import android.net.wifi.WifiConfiguration;
import android.net.wifi.WifiInfo;
import android.net.wifi.WifiManager;
import android.os.Build;
import android.text.TextUtils;
import android.util.Log;

import java.lang.reflect.Constructor;
import java.net.InetAddress;
import java.util.ArrayList;
import java.util.List;

public class WifiManagerHelper {
    public static final String TAG = "WifiManagerHelper";

    /**
     * These values are matched in string arrays -- changes must be kept in sync
     */
    public static final int SECURITY_NONE = 0;
    public static final int SECURITY_WEP = 1;
    public static final int SECURITY_PSK = 2;
    public static final int SECURITY_EAP = 3;

    public static WifiManagerHelper sInstance;

    public static WifiManagerHelper getInstance(Context context) {
        if (sInstance == null) {
            synchronized (WifiManagerHelper.class) {
                if (sInstance == null) {
                    sInstance = new WifiManagerHelper(context);
                }
            }
        }
        return sInstance;
    }

    private final WifiManager mWifiManager;

    private WifiManagerHelper(Context context) {
        mWifiManager = (WifiManager) context.getApplicationContext().getSystemService(Context.WIFI_SERVICE);
    }

    /**
     * 创建 Wifi 配置信息
     *
     * @param ssid Wifi ssid
     * @param pwd  Wifi 密码
     * @param type Wifi 加密类型
     * @return {@link WifiConfiguration}
     */
    private WifiConfiguration createWifiConfig(final String ssid, final String pwd, final int type) {
        Log.i(TAG, "createWifiConfig: " + ssid);
        try {
            WifiConfiguration wifiConfig = new WifiConfiguration();
            wifiConfig.allowedAuthAlgorithms.clear();
            wifiConfig.allowedGroupCiphers.clear();
            wifiConfig.allowedKeyManagement.clear();
            wifiConfig.allowedPairwiseCiphers.clear();
            wifiConfig.allowedProtocols.clear();
            wifiConfig.priority = 0;
            wifiConfig.SSID = "\"" + ssid + "\"";
            switch (type) {
                case SECURITY_NONE:
                    wifiConfig.hiddenSSID = true;
                    wifiConfig.allowedKeyManagement.set(WifiConfiguration.KeyMgmt.NONE);
                    break;
                case SECURITY_WEP:
                    wifiConfig.hiddenSSID = true;
                    wifiConfig.wepKeys[0] = "\"" + pwd + "\"";
                    wifiConfig.allowedKeyManagement.set(WifiConfiguration.KeyMgmt.NONE);
                    wifiConfig.allowedAuthAlgorithms.set(WifiConfiguration.AuthAlgorithm.OPEN);
                    // wifiConfig.allowedAuthAlgorithms.set(WifiConfiguration.AuthAlgorithm.SHARED);
                    wifiConfig.allowedGroupCiphers.set(WifiConfiguration.GroupCipher.CCMP);
                    wifiConfig.allowedGroupCiphers.set(WifiConfiguration.GroupCipher.TKIP);
                    wifiConfig.allowedGroupCiphers.set(WifiConfiguration.GroupCipher.WEP40);
                    wifiConfig.allowedGroupCiphers.set(WifiConfiguration.GroupCipher.WEP104);
                    wifiConfig.wepTxKeyIndex = 0;
                    break;
                case SECURITY_PSK:
                    wifiConfig.preSharedKey = "\"" + pwd + "\"";
                    wifiConfig.hiddenSSID = true;
                    wifiConfig.allowedAuthAlgorithms.set(WifiConfiguration.AuthAlgorithm.OPEN);
                    wifiConfig.allowedKeyManagement.set(WifiConfiguration.KeyMgmt.WPA_PSK);
//					wifiConfig.allowedProtocols.set(WifiConfiguration.Protocol.WPA);
                    wifiConfig.allowedGroupCiphers.set(WifiConfiguration.GroupCipher.CCMP);
                    wifiConfig.allowedGroupCiphers.set(WifiConfiguration.GroupCipher.TKIP);
                    wifiConfig.allowedPairwiseCiphers.set(WifiConfiguration.PairwiseCipher.CCMP);
                    wifiConfig.allowedPairwiseCiphers.set(WifiConfiguration.PairwiseCipher.TKIP);
                    wifiConfig.status = WifiConfiguration.Status.ENABLED;
                    break;
                case SECURITY_EAP:
                    Log.e(TAG, "SECURITY_EAP not support");
            }
            return wifiConfig;
        } catch (Exception e) {
            e.printStackTrace();
        }
        return null;
    }

    @SuppressLint("MissingPermission")
    public List<WifiConfiguration> getConfiguration() {
        try {
            return mWifiManager.getConfiguredNetworks();
        } catch (Exception e) {
            e.printStackTrace();
        }
        return null;
    }

    public WifiConfiguration getConfiguration(String ssid) {
        List<WifiConfiguration> configList = getConfiguration();
        if (configList == null || configList.isEmpty()) {
            return null;
        }

        for (WifiConfiguration config : configList) {
            if (config == null) {
                continue;
            }
            if (config.SSID.equals(ssid)) {
                return config;
            }
        }

        return null;
    }

    private void removeOldConfiguration(String ssid) {
        WifiConfiguration configuration = getConfiguration(ssid);
        if (configuration == null) {
            return;
        }

        Log.i(TAG, "removeOldConfiguration: " + ssid);
        mWifiManager.removeNetwork(configuration.networkId);
        mWifiManager.saveConfiguration();
    }

    public WifiInfo getWifiInfo() {
        try {
            return mWifiManager.getConnectionInfo();
        } catch (Exception e) {
            e.printStackTrace();
        }
        return null;
    }

    private void disableCurrentWifi() {
        WifiInfo wifiInfo = getWifiInfo();
        if (wifiInfo == null) {
            return;
        }
        Log.i(TAG, "disableCurrentWifi: " + wifiInfo.getSSID());
        int networkId = wifiInfo.getNetworkId();
        mWifiManager.disableNetwork(networkId);
        mWifiManager.disconnect();
    }

    private static int getSecurity(ScanResult result) {
        if (result.capabilities.contains("WEP")) {
            Log.i(TAG, "SECURITY_WEP");
            return SECURITY_WEP;
        } else if (result.capabilities.contains("PSK")) {
            Log.i(TAG, "SECURITY_PSK");
            return SECURITY_PSK;
        } else if (result.capabilities.contains("EAP")) {
            Log.i(TAG, "SECURITY_EAP");
            return SECURITY_EAP;
        }

        Log.i(TAG, "SECURITY_NONE");
        return SECURITY_NONE;
    }

    private List<ScanResult> getWifiList() {
        try {
            return mWifiManager.getScanResults();
        } catch (Exception e) {
            e.printStackTrace();
        }
        return null;
    }

    private boolean isOpenWifi() {
        return mWifiManager.isWifiEnabled();
    }

    public boolean connectWifi(final String ssid, final String pwd,
                               final boolean isStaticIp, final String ip, final String dns) {
        Log.i(TAG, "connectWifi: " + ssid + ", " + isStaticIp + ", " + ip + ", " + dns);
        if (TextUtils.isEmpty(ssid)) {
            return false;
        }

        if (!isOpenWifi()) {
            Log.e(TAG, "Please turn on the wifi switch");
            return false;
        }

        try {
            List<ScanResult> scanResults = getWifiList();
            if (scanResults == null || scanResults.isEmpty()) {
                Log.e(TAG, "scan result is empty");
                return false;
            }
            for (ScanResult result : scanResults) {
                if (ssid.equals(result.SSID)) {
                    int type = getSecurity(result);
                    return connectWifi(ssid, pwd, type, isStaticIp, ip, dns);
                }
            }
            Log.e(TAG, ssid + " is not found");
        } catch (Exception e) {
            e.printStackTrace();
        }
        return false;
    }

    private boolean connectWifi(final String ssid, final String pwd, final int type,
                                final boolean isStaticIp, final String ip, final String dns) {
        WifiConfiguration connWifiConfig = createWifiConfig(ssid, pwd, type);

        if (isStaticIp && (!TextUtils.isEmpty(ip))) {
            WifiConfiguration staticWifiConfig = setStaticWifiConfig(connWifiConfig, ip, dns);
            if (staticWifiConfig == null) {
                Log.e(TAG, "setStaticWifiConfig fail");
                return false;
            } else {
                connWifiConfig = staticWifiConfig;
            }
        }

        removeOldConfiguration(ssid);

        Log.i(TAG, "addNetwork: " + ssid);
        int networkId = mWifiManager.addNetwork(connWifiConfig);
        if (networkId == -1) {
            Log.e(TAG, "addNetwork fail");
            return false;
        }

        disableCurrentWifi();

        Log.i(TAG, "enableNetwork: " + ssid);
        boolean enabled = mWifiManager.enableNetwork(networkId, true);
        if (!enabled) {
            Log.e(TAG, "enableNetwork fail");
        }
        return enabled;
    }

    private int inetAddressToInt(final InetAddress inetAddress) {
        byte[] data = inetAddress.getAddress();
        if (data.length != 4) {
            throw new IllegalArgumentException("Not an IPv4 address");
        }
        return ((data[3] & 0xff) << 24) | ((data[2] & 0xff) << 16)
                | ((data[1] & 0xff) << 8) | (data[0] & 0xff);
    }

    private String ipTransformGateway(String ip) {
        try {
            InetAddress inetAddress = InetAddress.getByName(ip);
            int intIp = inetAddressToInt(inetAddress);
            String gateway = (intIp & 0xFF) + "." + ((intIp >> 8) & 0xFF) + "." + ((intIp >> 16) & 0xFF) + ".1";
            Log.i(TAG, "ipTransformGateway: " + gateway);
            return gateway;
        } catch (Exception e) {
            e.printStackTrace();
        }
        return null;
    }

    private WifiConfiguration setStaticWifiConfig(WifiConfiguration configuration, String ip, String dns) {
        Log.i(TAG, "setStaticWifiConfig------ip: " + ip + ", dns: " + dns);
        if (configuration == null || TextUtils.isEmpty(ip)) {
            return null;
        }

        if (TextUtils.isEmpty(dns)) {
            dns = "8.8.8.8";
        }

        String gateway = ipTransformGateway(ip);
        if (gateway == null) {
            return null;
        }

        if (Build.VERSION.SDK_INT <= Build.VERSION_CODES.KITKAT_WATCH) {
            return setStaticWifiConfigAndroid4(configuration, ip, gateway, dns);
        } else {
            return setStaticWifiConfigAndroid7(configuration, ip, gateway, dns);
        }

    }

    @SuppressWarnings("all")
    private WifiConfiguration setStaticWifiConfigAndroid7(WifiConfiguration wifiConfig, String ip, String gateway, String dns) {
        Log.i(TAG, "setStaticWifiConfigAndroid7: " + wifiConfig.SSID);
        try {
            InetAddress inetAddress = InetAddress.getByName(ip);
            Object object = Reflex.getDeclaredField(wifiConfig, "mIpConfiguration");
            Reflex.setEnumField(object, "STATIC", "ipAssignment");
            Reflex.setEnumField(object, "NONE", "proxySettings");

            Object staticIpConfigClass = Reflex.getField(object, "staticIpConfiguration");
            if (staticIpConfigClass == null) {
                staticIpConfigClass = Class.forName("android.net.StaticIpConfiguration").newInstance();
            }
            Class<?> laClass = Class.forName("android.net.LinkAddress");
            Constructor laConstructor = laClass.getConstructor(InetAddress.class, int.class);
            Object linkAddress = laConstructor.newInstance(InetAddress.getByName(ip), 24); //prefixLength
            Reflex.setValueField(staticIpConfigClass, linkAddress, "ipAddress");
            Reflex.setValueField(staticIpConfigClass, InetAddress.getByName(gateway), "gateway");
            if (!TextUtils.isEmpty(dns)) {
                List<InetAddress> mDnses = (ArrayList<InetAddress>) Reflex.getDeclaredField(
                        staticIpConfigClass, "dnsServers");
                mDnses.clear(); // or add a new dns address, here I just want to replace DNS1
                mDnses.add(InetAddress.getByName(dns));
            }

            Log.i(TAG, "set static ip configuration now");
            Reflex.setValueField(object, staticIpConfigClass, "staticIpConfiguration");
            return wifiConfig;
        } catch (Exception e) {
            e.printStackTrace();
        }
        return null;
    }

    private WifiConfiguration setStaticWifiConfigAndroid4(WifiConfiguration wifiConfig, String ip, String gateway, String dns) {
        try {
            InetAddress inetAddress = InetAddress.getByName(ip);
            Reflex.setEnumField(wifiConfig, "STATIC", "ipAssignment");
            Reflex.setEnumField(wifiConfig, "NONE", "proxySettings");
            setIpAddress(inetAddress, 24, wifiConfig); //prefixLength
            setGateway(InetAddress.getByName(gateway), wifiConfig);
            if (!TextUtils.isEmpty(dns)) {
                setDNS(InetAddress.getByName(dns), wifiConfig);
            }
            return wifiConfig;
        } catch (Exception e) {
            e.printStackTrace();
        }
        return null;
    }

    /**
     * 设置 DNS
     *
     * @param dns        DNS
     * @param wifiConfig Wifi 配置信息
     * @throws Exception 设置失败, 抛出异常
     */
    @SuppressWarnings("all")
    private void setDNS(final InetAddress dns, final WifiConfiguration wifiConfig) throws Exception {
        Object linkProperties = Reflex.getField(wifiConfig, "linkProperties");
        if (linkProperties == null) {
            throw new NullPointerException();
        }

        List<InetAddress> mDnses = (ArrayList<InetAddress>) Reflex.getDeclaredField(
                linkProperties, "mDnses");
        mDnses.clear(); // or add a new dns address, here I just want to replace DNS1
        mDnses.add(dns);
    }

    /**
     * 设置网关
     *
     * @param gateway    网关
     * @param wifiConfig Wifi 配置信息
     * @throws Exception 设置失败, 抛出异常
     */
    @SuppressWarnings("all")
    private void setGateway(final InetAddress gateway, final WifiConfiguration wifiConfig) throws Exception {
        Object linkProperties = Reflex.getField(wifiConfig, "linkProperties");
        if (linkProperties == null) {
            throw new NullPointerException();
        }

        Class<?> routeInfoClass = Class.forName("android.net.RouteInfo");
        Constructor routeInfoConstructor = routeInfoClass.getConstructor(InetAddress.class);
        Object routeInfo = routeInfoConstructor.newInstance(gateway);
        ArrayList mRoutes = (ArrayList) Reflex.getDeclaredField(linkProperties, "mRoutes");
        mRoutes.clear();
        mRoutes.add(routeInfo);
    }

    /**
     * 设置 IP 地址
     *
     * @param address      IP 地址
     * @param prefixLength 网络前缀长度
     * @param wifiConfig   Wifi 配置信息
     * @throws Exception 设置失败, 抛出异常
     */
    @SuppressWarnings("all")
    private void setIpAddress(final InetAddress address, final int prefixLength,
                              final WifiConfiguration wifiConfig) throws Exception {
        Object linkProperties = Reflex.getField(wifiConfig, "linkProperties");
        if (linkProperties == null) {
            throw new NullPointerException();
        }

        Class<?> laClass = Class.forName("android.net.LinkAddress");
        Constructor laConstructor = laClass.getConstructor(InetAddress.class, int.class);
        Object linkAddress = laConstructor.newInstance(address, prefixLength);
        ArrayList mLinkAddresses = (ArrayList) Reflex.getDeclaredField(linkProperties, "mLinkAddresses");
        mLinkAddresses.clear();
        mLinkAddresses.add(linkAddress);
    }

}
