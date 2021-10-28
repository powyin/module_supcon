package com.supconit.hcmobile.util;

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.SharedPreferences;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.net.wifi.WifiInfo;
import android.net.wifi.WifiManager;
import android.os.Build;
import android.text.TextUtils;
import android.util.Log;

import com.supconit.hcmobile.HcmobileApp;

import java.io.FileReader;
import java.io.InputStreamReader;
import java.io.LineNumberReader;
import java.io.Reader;
import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.net.NetworkInterface;
import java.net.Socket;
import java.net.SocketException;
import java.util.Collections;
import java.util.Enumeration;
import java.util.Locale;

public class NetUtil {

    //todo ------------------------------------------------------------------------------------net sta------------------------------------------------------------------------//
    // 网络
    // network sum wifi
    public static final String WIFI = "wifi";
    public static final String WIMAX = "wimax";
    // network sum ethernet
    public static final String ETHERNET = "ethernet";
    public static final String ETHERNET_SHORT = "eth";
    // network sum mobile
    public static final String MOBILE = "mobile";
    public static final String CELLULAR = "cellular";

    // 2G network types
    public static final String TWO_G = "2g";
    public static final String GSM = "gsm";
    public static final String GPRS = "gprs";
    public static final String EDGE = "edge";
    // 3G network types
    public static final String THREE_G = "3g";
    public static final String CDMA = "cdma";
    public static final String UMTS = "umts";
    public static final String HSPA = "hspa";
    public static final String HSUPA = "hsupa";
    public static final String HSDPA = "hsdpa";
    public static final String ONEXRTT = "1xrtt";
    public static final String EHRPD = "ehrpd";
    // 4G network types
    public static final String FOUR_G = "4g";
    public static final String LTE = "lte";
    public static final String UMB = "umb";
    public static final String HSPA_PLUS = "hspa+";

    // return type
    public static final String TYPE_WIFI = "wifi";
    public static final String TYPE_ETHERNET = "ethernet";
    public static final String TYPE_2G = "2g";
    public static final String TYPE_3G = "3g";
    public static final String TYPE_4G = "4g";
    public static final String TYPE_UNKNOWN = "unknown";
    public static final String TYPE_NONE = "none";

    /**
     * 获取内网ip v4
     */
    public static String getIP() {
        String ip = "";
        try {
            Enumeration<NetworkInterface> en = NetworkInterface.getNetworkInterfaces();
            while (en.hasMoreElements()) {
                NetworkInterface nif = en.nextElement();
                Enumeration<InetAddress> inet = nif.getInetAddresses();
                while (inet.hasMoreElements()) {
                    InetAddress ipV4 = inet.nextElement();
                    String current = ipV4.getHostAddress();
                    if (!ipV4.isLoopbackAddress() && current.length() <= 16) {
                        return current;
                    }
                }
            }
        } catch (SocketException e) {
            e.printStackTrace();
        }
        return ip;
    }

    /**
     * 检查本机端口被占用
     */
    public static boolean isPortUsing(int port) {
        boolean flag = true;
        Socket socket = null;
        try {
            socket = new Socket();
            socket.bind(new InetSocketAddress("127.0.0.1", port));
            flag = false;
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            if (socket != null) {
                try {
                    socket.close();
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        }
        return flag;
    }

    /**
     * 获取连接类型
     */
    public static String getNetWorkType() {
        ConnectivityManager connectivityManager = (ConnectivityManager) HcmobileApp.getApplication().getSystemService(Context.CONNECTIVITY_SERVICE);
        @SuppressLint("MissingPermission") NetworkInfo info = connectivityManager == null ? null : connectivityManager.getActiveNetworkInfo();
        if (info != null) {
            String type = info.getTypeName().toLowerCase(Locale.US);
            if (type.equals(WIFI)) {
                return TYPE_WIFI;
            } else if (type.toLowerCase().equals(ETHERNET) || type.toLowerCase().startsWith(ETHERNET_SHORT)) {
                return TYPE_ETHERNET;
            } else if (type.equals(MOBILE) || type.equals(CELLULAR)) {
                type = info.getSubtypeName().toLowerCase(Locale.US);
                if (type.equals(GSM) ||
                        type.equals(GPRS) ||
                        type.equals(EDGE) ||
                        type.equals(TWO_G)) {
                    return TYPE_2G;
                } else if (type.startsWith(CDMA) ||
                        type.equals(UMTS) ||
                        type.equals(ONEXRTT) ||
                        type.equals(EHRPD) ||
                        type.equals(HSUPA) ||
                        type.equals(HSDPA) ||
                        type.equals(HSPA) ||
                        type.equals(THREE_G)) {
                    return TYPE_3G;
                } else if (type.equals(LTE) ||
                        type.equals(UMB) ||
                        type.equals(HSPA_PLUS) ||
                        type.equals(FOUR_G)) {
                    return TYPE_4G;
                }
            }
        } else {
            return TYPE_NONE;
        }
        return TYPE_UNKNOWN;
    }


    public static boolean isVpnUsed() {
        try {
            Enumeration<NetworkInterface> niList = NetworkInterface.getNetworkInterfaces();
            if(niList != null) {
                for (NetworkInterface intf : Collections.list(niList)) {
                    if(!intf.isUp() || intf.getInterfaceAddresses().size() == 0) {
                        continue;
                    }
                    Log.d("powyin","isVpnUsed() NetworkInterface Name: " + intf.getName());
                    if ("tun0".equals(intf.getName()) || "ppp0".equals(intf.getName())){
                        return true; // The VPN is up
                    }
                }
            }
        } catch (Throwable e) {
            e.printStackTrace();
        }
        return false;
    }

    public static void disconnect(Context context) {
        ConnectivityManager cm = (ConnectivityManager) context
                .getSystemService(Context.CONNECTIVITY_SERVICE);
        Field fieldIConManager = null;

        try {
            fieldIConManager = cm.getClass().getDeclaredField("mService");
            fieldIConManager.setAccessible(true);
            Object objIConManager = fieldIConManager.get(cm);
            Class clsIConManager = Class.forName(objIConManager.getClass()
                    .getName());
            Method metPrepare = clsIConManager.getDeclaredMethod("prepareVpn",
                    String.class, String.class);
            metPrepare.invoke(objIConManager, "[Legacy VPN]", "[Legacy VPN]");
        } catch (Exception e) {
            e.printStackTrace();
        }
    }


    //todo -------mac -----

    public static String getMac(Context context) {
        if (HcmobileApp.getApplication().getPackageName().equals("com.hcapp.test")){
            SharedPreferences sharedPreferences=HcmobileApp.getApplication().getSharedPreferences("powyin_app_data_is_perssion", Context.MODE_PRIVATE);
            Boolean isPerssion=sharedPreferences.getBoolean("isPerssion",true);
            if (isPerssion){
                return getMacNow(context);
            }else {
                return "";
            }
        }else {
           return getMacNow(context);
        }

    }

    public static String getMacNow(Context context) {
        String strMac = null;
        Log.e("=====", "6.0以下");

        /**6.0以下*/
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.M) {

            strMac = getLocalMacAddressFromWifiInfo(context);
            return strMac;
        }
        /**6.0以上 7.0以下*/
        else if (Build.VERSION.SDK_INT < Build.VERSION_CODES.N
                && Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {

            strMac = getMacAddress(context);
            return strMac;
        }
        /**7.0以上*/
        else if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
            Log.e("=====", "7.0以上");
            if (!TextUtils.isEmpty(getMacAddress())) {

                strMac = getMacAddress();
                return strMac;
            }
//            else if (!TextUtils.isEmpty(getMachineHardwareAddress())) {
//                Log.e("=====", "7.0以上2");
//                Toast.makeText(context, "7.0以上2", Toast.LENGTH_SHORT).show();
//                strMac = getMachineHardwareAddress();
//                return strMac;
//            } else {
//                Log.e("=====", "7.0以上3");
//                Toast.makeText(context, "7.0以上3", Toast.LENGTH_SHORT).show();
//                strMac = getLocalMacAddressFromBusybox();
//                return strMac;
//            }
        }
        return strMac;
    }
    /**
     * 根据wifi信息获取本地mac
     * @param context
     * @return
     */
    private static String getLocalMacAddressFromWifiInfo(Context context){
        WifiManager wifi = (WifiManager) context.getSystemService(Context.WIFI_SERVICE);
        WifiInfo winfo = wifi.getConnectionInfo();
        String mac =  winfo.getMacAddress();
        return mac;
    }
    /**
     * android 6.0及以上、7.0以下 获取mac地址
     *
     * @param context
     * @return
     */
    private static String getMacAddress(Context context) {

        String str = "";
        String macSerial = "";
        try {
            Process pp = Runtime.getRuntime().exec("cat /sys/class/net/wlan0/address");
            InputStreamReader ir = new InputStreamReader(pp.getInputStream());
            LineNumberReader input = new LineNumberReader(ir);
            for (; null != str; ) {
                str = input.readLine();
                if (str != null) {
                    macSerial = str.trim();// 去空格
                    break;
                }
            }
        } catch (Exception ex) {
            Log.e("----->" + "NetInfoManager", "getMacAddress:" + ex.toString());
        }
        if (macSerial == null || "".equals(macSerial)) {
            try {
                return loadFileAsString("/sys/class/net/eth0/address")
                        .toUpperCase().substring(0, 17);
            } catch (Exception e) {
                e.printStackTrace();
                Log.e("----->" + "NetInfoManager",
                        "getMacAddress:" + e.toString());
            }

        }
        return macSerial;
    }

    private static String loadFileAsString(String fileName) throws Exception {
        FileReader reader = new FileReader(fileName);
        String text = loadReaderAsString(reader);
        reader.close();
        return text;
    }
    private static String loadReaderAsString(Reader reader) throws Exception {
        StringBuilder builder = new StringBuilder();
        char[] buffer = new char[4096];
        int readLength = reader.read(buffer);
        while (readLength >= 0) {
            builder.append(buffer, 0, readLength);
            readLength = reader.read(buffer);
        }
        return builder.toString();

    }

    /**
     * 根据IP地址获取MAC地址
     *
     * @return
     */
    private static String getMacAddress() {
        String strMacAddr = null;
        try {
            // 获得IpD地址
            InetAddress ip = getLocalNetAddress();
            byte[] b = NetworkInterface.getByInetAddress(ip)
                    .getHardwareAddress();
            StringBuffer buffer = new StringBuffer();
            for (int i = 0; i < b.length; i++) {
                if (i != 0) {
                    buffer.append(':');
                }
                String str = Integer.toHexString(b[i] & 0xFF);
                buffer.append(str.length() == 1 ? 0 + str : str);
            }
            strMacAddr = buffer.toString().toUpperCase();
        } catch (Exception e) {
        }
        return strMacAddr;
    }
    /**
     * 获取移动设备本地IP
     *
     * @return
     */
    private static InetAddress getLocalNetAddress() {
        InetAddress ip = null;
        try {
            // 列举
            Enumeration<NetworkInterface> en_netInterface = NetworkInterface
                    .getNetworkInterfaces();
            while (en_netInterface.hasMoreElements()) {// 是否还有元素
                NetworkInterface ni = (NetworkInterface) en_netInterface
                        .nextElement();// 得到下一个元素
                Enumeration<InetAddress> en_ip = ni.getInetAddresses();// 得到一个ip地址的列举
                while (en_ip.hasMoreElements()) {
                    ip = en_ip.nextElement();
                    if (!ip.isLoopbackAddress()
                            && ip.getHostAddress().indexOf(":") == -1)
                        break;
                    else
                        ip = null;
                }

                if (ip != null) {
                    break;
                }
            }
        } catch (SocketException e) {
            e.printStackTrace();
        }
        return ip;
    }



}
