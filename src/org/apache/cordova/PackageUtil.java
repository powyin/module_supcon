package org.apache.cordova;

import android.content.Context;
import android.graphics.Bitmap;
import android.os.Environment;
import android.text.TextUtils;
import android.util.Log;

import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.Enumeration;

public class PackageUtil {

    /**
     * 提取url中 前缀名 后缀名 完整名
     *
     * @return https://github.com/apache/incubator-weex.txt#cc=23   return   ["incubator-weex","txt","incubator-weex.txt"]
     */
    public static String[] getFileNameFromUrl(String url) {
        String[] preName_SuffixName_FullName = new String[3];
        preName_SuffixName_FullName[0] = "";
        preName_SuffixName_FullName[1] = "";
        preName_SuffixName_FullName[2] = "";
        if (url != null && url.length() > 0) {
            int fragment = url.lastIndexOf('#');
            if (fragment > 0) {
                url = url.substring(0, fragment);
            }
            int query = url.lastIndexOf('?');
            if (query > 0) {
                url = url.substring(0, query);
            }
            int filenamePos = url.lastIndexOf('/');
            String filename = 0 <= filenamePos ? url.substring(filenamePos + 1) : url;
            preName_SuffixName_FullName[2] = filename;
            int dotPos = filename.lastIndexOf('.');
            if (dotPos >= 0) {
                preName_SuffixName_FullName[0] = filename.substring(0, dotPos);
                preName_SuffixName_FullName[1] = filename.substring(dotPos + 1);
            } else {
                preName_SuffixName_FullName[0] = filename;
            }
        }
        return preName_SuffixName_FullName;
    }


    /**
     * 读取本地文件为String
     */
    public static String readLocalFileAsString(String path) {
        if (!TextUtils.isEmpty(path)) {
            File file = new File(path);
            if (file.exists()) {
                try {
                    FileInputStream fis = new FileInputStream(file);
                    return _readStreamAsString(fis);
                } catch (FileNotFoundException e) {
                    e.printStackTrace();
                }
            }
        }
        return "";
    }

    /**
     * 读取stream流为String
     */
    private static String _readStreamAsString(InputStream inputStream) {
        BufferedReader bufferedReader = null;
        try {
            StringBuilder builder = new StringBuilder(inputStream.available() + 10);
            bufferedReader = new BufferedReader(new InputStreamReader(inputStream));
            char[] data = new char[4096];
            int len = -1;
            while ((len = bufferedReader.read(data)) > 0) {
                builder.append(data, 0, len);
            }

            return builder.toString();
        } catch (IOException e) {
            e.printStackTrace();
        } finally {
            try {
                if (bufferedReader != null)
                    bufferedReader.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
            try {
                if (inputStream != null)
                    inputStream.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }

        return null;
    }


    /**
     * cdf协议文件转换成本地文件系统
     */
    @SuppressWarnings("unchecked")
    public static String cdvFileToLocal(CordovaWebView webView, String cdvFilePath) {
        if (cdvFilePath == null || !cdvFilePath.startsWith("cdv")) {
            Log.d("powyin", "cdvfile transfor error " + cdvFilePath);
            return cdvFilePath;
        }


        Class webViewClass = webView.getClass();
        PluginManager pm = null;
        try {
            Method gpm = webViewClass.getMethod("getPluginManager");
            pm = (PluginManager) gpm.invoke(webView);
        } catch (Exception e) {
            e.printStackTrace();
        }
        if (pm == null) {
            try {
                Field pmf = webViewClass.getField("pluginManager");
                pm = (PluginManager) pmf.get(webView);
            } catch (Exception e) {
                e.printStackTrace();
            }
        }

        Object filePlugin = pm == null ? null : pm.getPlugin("File");
        if (filePlugin == null) {
            Log.d("powyin", "cdvfile transfor error cannot get PluginManager " + pm);
            return cdvFilePath;
        }
        try {
            Method resolveLocalFileSystemURI = filePlugin.getClass().getDeclaredMethod("resolveLocalFileSystemURI", String.class);
            resolveLocalFileSystemURI.setAccessible(true);
            JSONObject invoke = (JSONObject) resolveLocalFileSystemURI.invoke(filePlugin, cdvFilePath);
            String value = invoke.getString("nativeURL");
            if (value.startsWith("file")) {
                value = value.replace("file://", "");
            }
            return value;
        } catch (Exception e) {
            e.printStackTrace();
            Log.d("powyin", "cdvfile transfor error 发生了严重错误");
        }
        return cdvFilePath;
    }

    /**
     * 拼接路径/xxx/yyy/zzz
     */
    public static String pathAppend(String host, String... paths) {
        host = host == null ? "" : host;
        while (host.endsWith("/")) {
            host = host.substring(0, host.length() - 1);
        }

        StringBuilder builder = new StringBuilder(host);
        for (String path : paths) {
            if (path != null && !path.equals(""))
                builder.append(normalizeDashes(path));
        }

        return builder.toString();
    }


    private static String normalizeDashes(String path) {
        if (!path.startsWith("/")) {
            path = "/" + path;
        }

        if (path.endsWith("/")) {
            path = path.substring(0, path.length() - 1);
        }

        return path;
    }

    /**
     * 获取hash值
     */
    public static String hashKey(String key) {
        String hashKey;
        try {
            final MessageDigest mDigest = MessageDigest.getInstance("MD5");
            mDigest.update(key.getBytes());
            hashKey = bytesToHexString(mDigest.digest());
        } catch (NoSuchAlgorithmException e) {
            hashKey = String.valueOf(key.hashCode());
        }
        return hashKey;
    }

    private static String bytesToHexString(byte[] bytes) {
        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < bytes.length; i++) {
            String hex = Integer.toHexString(0xFF & bytes[i]);
            if (hex.length() == 1) {
                sb.append('0');
            }
            sb.append(hex);
        }
        return sb.toString();
    }



}
