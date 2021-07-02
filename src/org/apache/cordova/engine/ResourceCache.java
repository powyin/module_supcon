//package org.apache.cordova.engine;
//
//import android.content.Context;
//import android.content.SharedPreferences;
//import android.text.TextUtils;
//import android.util.Log;
//import android.webkit.WebResourceResponse;
//import android.webkit.WebView;
//
//import com.supconit.hcmobile.HcmobileApp;
//import com.supconit.hcmobile.net.DownInfo;
//import com.supconit.hcmobile.net.HttpManager;
//import com.supconit.hcmobile.net.NetState;
//
//import org.apache.cordova.PackageUtil;
//
//import java.io.File;
//import java.io.FileInputStream;
//import java.util.HashMap;
//import java.util.List;
//import java.util.Map;
//import java.util.Set;
//import java.util.concurrent.atomic.AtomicInteger;
//import java.util.regex.Pattern;
//
//import okhttp3.Headers;
//
//public class ResourceCache {
//
//    private static final Map<String, CacheStatus> mFileUrlMap = new HashMap<>();
//
//    private static class CacheStatus {
//        // todo case 0 : 初始化下载中 本地无文件缓存                          不能使用
//        // todo case 1 : 文件下载中                                          不能使用
//        // todo case 5 : 本地有过期文件缓存                                   可以直接使用
//        // todo case 9 : 本地文件有效                                        可以直接使用
//        // todo case 10 : 不支持缓存远程文件可变(http有自己的缓存控制)          不能使用
//        AtomicInteger code = new AtomicInteger();
//        long updateTime;
//        String cacheFile;
//        String httpPath;
//    }
//
////    public static void init(Context context) {
////        SharedPreferences sharedPreferences = context.getSharedPreferences("powyin_web_view_cache", Context.MODE_PRIVATE);
////        Map<String, ?> caches = sharedPreferences.getAll();
////        SharedPreferences.Editor edit = sharedPreferences.edit();
////        if (caches == null) {
////            return;
////        }
////        Set<? extends Map.Entry<String, ?>> entries = caches.entrySet();
////        for (Map.Entry<String, ?> entry : entries) {
////            String key = entry.getKey();
////            long value = Long.parseLong(entry.getValue().toString());
////            File file = new File(key);
////            if (!file.exists() || file.length() <= 0) {
////                edit.remove(key);
////                continue;
////            }
////            CacheStatus cacheStatus = new CacheStatus();
////            cacheStatus.updateTime = value;
////            cacheStatus.cacheFile = key;
////            if (value == 0) {
////                cacheStatus.code.set(10);
////                mFileUrlMap.put(key, cacheStatus);
////            } else {
////                if (System.currentTimeMillis() - value > SystemWebViewClient.mCacheTime) {
////                    cacheStatus.code.set(5);
////                } else {
////                    cacheStatus.code.set(9);
////                    mFileUrlMap.put(key, cacheStatus);
////                }
////            }
////
////            // mFileUrlMap.put(key, cacheStatus);
////        }
////        edit.apply();
////    }
//
//    public static void clearCache() {
//        try {
//            String cacheFile = HcmobileApp.getApplication().getCacheDir() + "/powyin_web_cache/";
//            File file = new File(cacheFile);
//            File[] files = file.exists() ? file.listFiles() : null;
//            for (int i = 0; files != null && i < files.length; i++) {
//                if (!files[i].delete()) {
//                    Log.e("powyin", "clear cache file fail" + files[i]);
//                }
//            }
//        } catch (Exception e) {
//            e.printStackTrace();
//        }
//        mFileUrlMap.clear();
//        SharedPreferences sharedPreferences = HcmobileApp.getApplication().getSharedPreferences("powyin_web_view_cache", Context.MODE_PRIVATE);
//        sharedPreferences.edit().clear().apply();
//    }
//
//
//    static WebResourceResponse getWebResource(WebView webView, String url) throws Exception {
//        if (SystemWebViewClient.mForbiddenCache || !url.startsWith("http")) {
//            return null;
//        }
//
//        String fileName = getFileNameFromUrl(url);
//        int dotPos = fileName.lastIndexOf('.');
//        String urlSuffixName = dotPos >= 0 ? fileName.substring(dotPos + 1) : "";
//        if (!"js".equals(urlSuffixName)) return null;
//
//        String cacheFile = webView.getContext().getCacheDir() + "/powyin_web_cache/" + PackageUtil.hashKey(url) + "_" + fileName;
//        CacheStatus cacheStatus = mFileUrlMap.get(cacheFile);
//        if (cacheStatus == null) {
//            cacheStatus = new CacheStatus();
//            cacheStatus.cacheFile = cacheFile;
//            cacheStatus.code.set(0);
//            synchronized (mFileUrlMap) {
//                if (mFileUrlMap.containsKey(cacheFile)) {
//                    cacheStatus = mFileUrlMap.get(cacheFile);
//                } else {
//                    mFileUrlMap.put(cacheFile, cacheStatus);
//                }
//            }
//        }
//
//        // todo 不支持缓存远程文件可变(http有自己的缓存控制)
//        if (cacheStatus.code.get() == 10) {
//            return null;
//        }
//
//        // todo 判断资源是否过期
//        if ((cacheStatus.code.get() == 9) && (System.currentTimeMillis() - cacheStatus.updateTime >= SystemWebViewClient.mCacheTime)) {
//            cacheStatus.code.compareAndSet(9, 5);
//        }
//
//        // todo 缓存有效 直接使用
//        File cacheFileStream;
//        if (cacheStatus.code.get() == 9 && (cacheFileStream = new File(cacheFile)).exists() && cacheFileStream.length() > 0) {
//            Log.e("powyin", "- cache user -> " + cacheFile);
//            return new WebResourceResponse("text/plain", "UTF-8", new FileInputStream(cacheFileStream));
//        }
//
//        // todo 重新下载资源
//        if (cacheStatus.code.get() == 5) {
//            cacheStatus.code.compareAndSet(5, 0);
//        }
//        if (cacheStatus.code.get() == 0) {
//            cacheStatus.httpPath = url;
//            if (downLoadCacheFile(cacheStatus)) {
//                Log.e("powyin", "- cache success and use -> " + cacheFile);
//                return new WebResourceResponse("text/plain", "UTF-8", new FileInputStream(cacheFile));
//            }
//        }
//
//        return null;
//    }
//
//    // todo 开始下载文件 手动保存缓存
//    private static boolean downLoadCacheFile(final CacheStatus cache) {
//        if (cache == null || !cache.code.compareAndSet(0, 1)) {
//            return false;
//        }
//
//        Log.e("powyin", "- cache begin -> " + cache.cacheFile);
//        Map<String, File> down = new HashMap<>();
//        down.put(cache.httpPath, new File(cache.cacheFile));
//
//        DownInfo downLoadInfo = null;
//        try {
//            List<DownInfo> downLoadInfos = HttpManager.fileDownLoadAsy(down);
//            downLoadInfo = downLoadInfos.get(0);
//        } catch (Exception e) {
//            e.printStackTrace();
//        }
//
//        if (downLoadInfo != null && downLoadInfo.status == NetState.FINISH && downLoadInfo.localFilePath.exists() && downLoadInfo.localFilePath.length() > 0) {
//
//            Headers headers = downLoadInfo.responseHeader;
//            Set<String> names = headers == null ? null : headers.names();
//            if (names == null) {
//                Log.e("powyin", "- cache skip no head-> " + cache.httpPath);
//                cache.code.set(10);
//                cache.updateTime = 0;
//                return false;
//            }
//            for (String string : names) {
//                if ("etag".equals(string.toLowerCase())) {   //  || "last-modified".equals(string.toLowerCase())
//                    Log.e("powyin", "- cache skip -> " + cache.httpPath);
//                    cache.code.set(10);
//                    cache.updateTime = 0;
//                    return false;
//                }
//            }
//            cache.code.set(9);
//            cache.updateTime = System.currentTimeMillis();
//            SharedPreferences sharedPreferences = HcmobileApp.getApplication().getSharedPreferences("powyin_web_view_cache", Context.MODE_PRIVATE);
//            sharedPreferences.edit().putLong(cache.cacheFile, System.currentTimeMillis()).apply();
//            Log.e("powyin", "- cache success -> " + cache.cacheFile);
//            return true;
//        } else {
//            cache.code.set(0);
//            Log.e("powyin", "- cache error -> " + cache.cacheFile);
//            return false;
//        }
//    }
//
//
//    private static String getFileNameFromUrl(String url) {
//        if (!TextUtils.isEmpty(url)) {
//            int fragment = url.lastIndexOf('#');
//            if (fragment > 0) {
//                url = url.substring(0, fragment);
//            }
//            int query = url.lastIndexOf('?');
//            if (query > 0) {
//                url = url.substring(0, query);
//            }
//            int filenamePos = url.lastIndexOf('/');
//            String filename = 0 <= filenamePos ? url.substring(filenamePos + 1) : url;
//            if (!filename.isEmpty() &&
//                    Pattern.matches("[a-zA-Z_0-9\\.\\-\\(\\)\\%]+", filename)) {
//                return filename;
//            }
//        }
//        return "";
//    }
//
//}