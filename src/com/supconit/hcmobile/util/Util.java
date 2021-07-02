package com.supconit.hcmobile.util;

import android.Manifest;
import android.app.Activity;
import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.ApplicationInfo;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.content.pm.Signature;
import android.content.res.XmlResourceParser;
import android.location.Location;
import android.net.Uri;
import android.os.Build;
import android.support.v4.content.FileProvider;
import android.text.TextUtils;
import android.util.Log;
import android.util.TypedValue;
import android.view.View;
import android.view.inputmethod.InputMethodManager;
import android.webkit.MimeTypeMap;
import android.webkit.WebView;
import android.widget.EditText;

import com.supconit.hcmobile.HcmobileApp;
import com.supconit.hcmobile.center.dialog.TopProgressDialog;
import com.supconit.hcmobile.net.DownInfo;
import com.supconit.hcmobile.net.HttpManager;
import com.supconit.hcmobile.net.Progress;
import com.supconit.inner_hcmobile.R;
import com.supconit.hcmobile.permissions.Permission;
import com.supconit.hcmobile.permissions.PermissionsActivityPart;
import com.supconit.hcmobile.permissions.RxPermissions;


import org.apache.cordova.LOG;
import org.xmlpull.v1.XmlPullParser;
import org.xmlpull.v1.XmlPullParserException;

import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.lang.reflect.Method;
import java.lang.reflect.Type;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import io.reactivex.ObservableSource;
import io.reactivex.Observer;
import io.reactivex.Single;
import io.reactivex.SingleEmitter;
import io.reactivex.SingleObserver;
import io.reactivex.SingleOnSubscribe;
import io.reactivex.SingleSource;
import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.disposables.Disposable;
import io.reactivex.functions.Function;
import io.reactivex.schedulers.Schedulers;

public class Util implements Cloneable{

    // todo app 运行重要权限集合 以及对应UI提示语
    private static HashMap<String, String> mImpPermission = new HashMap<>();

    static {
        mImpPermission.put(Manifest.permission.INTERNET, "应用运行需要连接网络\n是否跳转权限管理页面进行授权");
        mImpPermission.put(Manifest.permission.ACCESS_COARSE_LOCATION, "应用运行需要获取网络位置\n是否跳转权限管理页面进行授权");
        mImpPermission.put(Manifest.permission.WRITE_EXTERNAL_STORAGE, "应用运行需要读写文件权限\n是否跳转权限管理页面进行授权");
        mImpPermission.put(Manifest.permission.ACCESS_FINE_LOCATION, "应用运行需要GPS定位权限\n是否跳转权限管理页面进行授权");
    }

    private static boolean hasNotify;

    /**
     * 请求android权限 简化权限操作
     *
     * @param activity    如果需要使用此方法必须确保activity继承自BasePermissionsActivity的activity
     * @param permissions 请求的权限数组
     * @param tip         获取权限的 提示语
     */
    public static Single<Boolean> askPermission(final Context activity, String[] permissions, final String tip) {
        if (activity instanceof PermissionsActivityPart) {
            return new RxPermissions((PermissionsActivityPart) activity).requestEach(permissions)
                    .toList().subscribeOn(Schedulers.io()).observeOn(Schedulers.io())
                    .map(new Function<List<Permission>, Boolean>() {
                        @Override
                        public Boolean apply(List<Permission> permissions) throws Exception {
                            // todo 如果有重要的权限没有授权 引导用户进入权限设置页面 启动app一次引导一次
                            boolean hasUnPermission = false;
                            String mImpUnPermission = null;
                            for (Permission permission : permissions) {
                                if (!permission.granted) {
                                    hasUnPermission = true;
                                    if (mImpPermission.containsKey(permission.name)) {
                                        mImpUnPermission = permission.name;
                                    }
                                }
                            }
                            if (mImpUnPermission != null && !hasNotify) {
                                hasNotify = true;
                                final String message = mImpPermission.get(mImpUnPermission);
                                HcmobileApp.getHandle().post(new Runnable() {
                                    @Override
                                    public void run() {
                                        AlertDialog.Builder builder = new AlertDialog.Builder(activity, AlertDialog.THEME_DEVICE_DEFAULT_LIGHT);
                                        builder.setMessage(message);
                                        builder.setNegativeButton("取消", new DialogInterface.OnClickListener() {
                                            @Override
                                            public void onClick(DialogInterface dialogInterface, int i) {
                                                dialogInterface.dismiss();
                                            }
                                        });
                                        builder.setPositiveButton("继续", new DialogInterface.OnClickListener() {
                                            @Override
                                            public void onClick(DialogInterface dialogInterface, int type) {
                                                dialogInterface.dismiss();
                                                try {
                                                    HcmobileApp.getApplication().startActivity(IntentUtil.getPermissionSettingIntent());
                                                } catch (Exception e) {
                                                    e.printStackTrace();
                                                }
                                            }
                                        });
                                        builder.show();
                                    }
                                });
                            }
                            if (hasUnPermission) {
                                throw new RuntimeException("权限获取异常");
                            }
                            return true;
                        }
                    }).subscribeOn(Schedulers.io()).observeOn(Schedulers.io());
        }
        throw new RuntimeException("activity not extend BasePermissionsActivity or BasePermissionsAppCompatActivityActivity cannot use this method");
    }


    /**
     * 强制检查网络是否为wifi环境下  构建了一个观察者 简化权限操作
     *
     * @param activity 如果需要使用此方法必须确保activity继承自BasePermissionsActivity的activity
     */
    public static Single<Boolean> askAccessWifi(final Context activity) {
        return Single.create(new SingleOnSubscribe<Boolean>() {
            private final Object mLock = new Object();
            boolean going = false;

            @Override
            public void subscribe(SingleEmitter<Boolean> emitter) throws Exception {
                final String type = NetUtil.getNetWorkType();
                if (!NetUtil.TYPE_WIFI.equals(type) && !NetUtil.TYPE_ETHERNET.equals(type) && !NetUtil.TYPE_UNKNOWN.equals(type)) {
                    if (type.equals(NetUtil.TYPE_NONE)) {
                        emitter.onError(new RuntimeException("网络无法连接"));
                    } else {
                        HcmobileApp.getHandle().post(new Runnable() {
                            @Override
                            public void run() {
                                AlertDialog.Builder builder = new AlertDialog.Builder(activity, AlertDialog.THEME_DEVICE_DEFAULT_LIGHT);
                                builder.setMessage("您正在使用" + type + "流量下载，是否继续？");
                                builder.setNegativeButton("取消", new DialogInterface.OnClickListener() {
                                    @Override
                                    public void onClick(DialogInterface dialogInterface, int i) {
                                        dialogInterface.dismiss();
                                    }
                                });
                                builder.setOnDismissListener(new DialogInterface.OnDismissListener() {
                                    @Override
                                    public void onDismiss(DialogInterface dialog) {
                                        synchronized (mLock) {
                                            mLock.notifyAll();
                                        }
                                    }
                                });
                                builder.setPositiveButton("继续", new DialogInterface.OnClickListener() {
                                    @Override
                                    public void onClick(DialogInterface dialogInterface, int type) {
                                        going = true;
                                        dialogInterface.dismiss();
                                    }
                                });
                                builder.show();
                            }
                        });
                        synchronized (mLock) {
                            try {
                                mLock.wait();
                            } catch (InterruptedException ex) {
                                ex.printStackTrace();
                            }
                        }
                        if (!going) {
                            emitter.onError(new RuntimeException("当前网络非wifi环境 用户取消了操作"));
                            return;
                        }
                        emitter.onSuccess(true);
                    }
                } else {
                    emitter.onSuccess(true);
                }
            }
        }).subscribeOn(Schedulers.io()).observeOn(Schedulers.io());
    }

    /**
     * 单次地址定位 10s更新有效位置
     */
    public static Single<Location> askLocation(final Context context) {
        String[] permissions = {Manifest.permission.ACCESS_COARSE_LOCATION,
                Manifest.permission.ACCESS_FINE_LOCATION};
        return askPermission(context, permissions, "地图定位需要获取GPS权限")
                .flatMap(new Function<Boolean, SingleSource<? extends Location>>() {
                    @Override
                    public SingleSource<? extends Location> apply(final Boolean aBoolean) throws Exception {
                        return LocationFinder.getInstance(HcmobileApp.getApplication()).getLocation();
                    }
                })
                .subscribeOn(Schedulers.io()).observeOn(Schedulers.io());
    }


    /**
     * 单次地址定位 精度优先
     */
    public static Single<Location> askLocationHighAccuracy(final Context context) {
        String[] permissions = {Manifest.permission.ACCESS_COARSE_LOCATION,
                Manifest.permission.ACCESS_FINE_LOCATION};
        return askPermission(context, permissions, "地图定位需要获取GPS权限")
                .flatMap(new Function<Boolean, SingleSource<? extends Location>>() {
                    @Override
                    public SingleSource<? extends Location> apply(final Boolean aBoolean) throws Exception {
                        return LocationFinder.getInstance(HcmobileApp.getApplication()).getLocationHighAccuracy();
                    }
                })
                .subscribeOn(Schedulers.io()).observeOn(Schedulers.io());
    }


    private static String province;
    private static String city;

    /**
     * 单次地址定位 10s更新有效位置
     */
    public static Single<Address> askLocationWithCity(final Context context) {
        return askLocation(context).flatMap(new Function<Location, SingleSource<Address>>() {
            @Override
            public SingleSource<Address> apply(Location location) throws Exception {
                if (!TextUtils.isEmpty(province)) {
                    return new SingleSource<Address>() {
                        @Override
                        public void subscribe(SingleObserver<? super Address> observer) {
                            Address address = new Address();
                            address.location = location;
                            address.province = province;
                            address.city = city;
                            observer.onSuccess(address);
                        }
                    };
                }

                //用经纬度获取城市名称
                Map<String, String> getP = new HashMap<>();
                String url = "http://api.map.baidu.com/geocoder?output=json&";
                String loLat = String.valueOf(location.getLatitude());
                String loLong = String.valueOf(location.getLongitude());
                String loLl = "location=" + loLat + "," + loLong + "&ak=esNPFDwwsXWtsQfw4NMNmur1";
                //此url拼接为百度接口，传入经纬度拿到城市信息。
                return HttpManager.get(url + loLl, getP, String.class).map(new Function<String, Address>() {
                    @Override
                    public Address apply(String s) throws Exception {
                        Address address = new Address();
                        address.location = location;
                        address.province = JsonUtil.getJsonString(s, "result", "addressComponent", "province");
                        address.city = JsonUtil.getJsonString(s, "result", "addressComponent", "city");
                        province = address.province;
                        city = address.city;
                        return address;
                    }
                });
            }
        }).subscribeOn(Schedulers.io()).observeOn(Schedulers.io());
    }


    /**
     * 打印一个对象的所有get无参方法结果(如果方法返回非基本数据类型 使用className替换结果)
     */
    public static void printObjectGetMethod(Object object) {
        if (object == null) {
            Log.e("powyin", "object == null");
            return;
        }

        StringBuilder builder = new StringBuilder();
        Class<?> aClass = object.getClass();
        builder.append(aClass.getSimpleName()).append(" : show getMethod result\n");
        Method[] methods = aClass.getMethods();
        for (int i = 0; methods != null && i < methods.length; i++) {
            Method method = methods[i];
            String methodName = method.getName();
            if (methodName.startsWith("get") && !methodName.equals("getClass")) {
                Type[] genericParameterTypes = method.getGenericParameterTypes();
                if (genericParameterTypes == null || genericParameterTypes.length == 0) {
                    try {
                        Object invoke = method.invoke(object);
                        if (invoke == null) {
                            builder.append(methodName).append("()<").append("null").append("\n");
                        } else {
                            if ((invoke instanceof Number) || invoke instanceof CharSequence || invoke instanceof Character || invoke instanceof Boolean) {
                                builder.append(methodName).append("()=").append(invoke.toString()).append("\n");
                            } else {
                                builder.append(methodName).append("()>").append(invoke.getClass().getSimpleName()).append("\n");
                            }
                        }
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                }
            }
        }

        builder.setLength(builder.length() - 2);
        Log.e("powyin", builder.toString());

    }


    /**
     * 打印当前函数运行所在位置的日志
     */
    public static void printCurrentMethod(String... args) {

        StackTraceElement[] stackTrace = Thread.currentThread().getStackTrace();

        StackTraceElement stackTraceElement = stackTrace[3];
        String methodName = stackTraceElement.getMethodName();
        String className = stackTraceElement.getClassName();
        int lastIndexOf = className.lastIndexOf(".");
        if (lastIndexOf > 0) {
            className = className.substring(lastIndexOf);
        }

        StringBuilder stringBuilder = new StringBuilder();
        stringBuilder.append(className);
        stringBuilder.append(":  ");
        stringBuilder.append(methodName);
        stringBuilder.append(":  ");
        for (int i = 0; args != null && i < args.length; i++) {
            stringBuilder.append(args[i]);
            stringBuilder.append(" : ");
        }

        stringBuilder.setLength(stringBuilder.length() - 3);
        Log.i("powyin", stringBuilder.toString());
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

    /**
     * 支持 web RRGGBBAA RRGGBB 颜色声明
     */
    public static int parseColor(String rgba) {
        if (rgba == null || rgba.length() < 1) {
            return 0xff000000;
        }
        boolean hasPreMark = rgba.charAt(0) == '#';
        int colorLen = rgba.length() - (hasPreMark ? 1 : 0);
        long color = Long.parseLong(rgba.substring(hasPreMark ? 1 : 0), 16);
        if (colorLen <= 6) {
            // fill alpha value
            color |= 0x00000000ff000000;
            return (int) color;
        }
        if (colorLen == 7) {
            int intColor = (int) color;
            intColor = intColor << 1;
            return (intColor >>> 8) | (intColor << 24);
        } else {
            int intColor = (int) color;
            // int pre = intColor | 0x000000ff;
            return (intColor >>> 8) + (intColor << 24);
        }
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
     * package是否安装
     */
    public static boolean isPackageValid(String packageName) {
        final PackageManager packageManager = HcmobileApp.getApplication().getPackageManager();
        List<PackageInfo> packageInfos = packageManager.getInstalledPackages(0);
        List<String> packageNames = new ArrayList<String>();
        if (packageInfos != null) {
            for (int i = 0; i < packageInfos.size(); i++) {
                String packName = packageInfos.get(i).packageName;
                packageNames.add(packName);
            }
        }
        return packageNames.contains(packageName);
    }


    /**
     * 匹配res/xml/config.xml 文件元素
     */
    public static List<String> getCordovaConfigTagList(String tagName, String attributeName) {
        if (TextUtils.isEmpty(tagName) || TextUtils.isEmpty(attributeName)) {
            return new ArrayList<>();
        }
        Context context = HcmobileApp.getApplication();
        int id = context.getResources().getIdentifier("config", "xml", context.getPackageName());
        XmlResourceParser xml = context.getResources().getXml(id);
        List<String> ret = new ArrayList<>();
        int eventType = -1;
        while (eventType != XmlPullParser.END_DOCUMENT) {
            if (eventType == XmlPullParser.START_TAG) {
                String strNode = xml.getName();
                if (strNode.equals(tagName)) {
                    String value = xml.getAttributeValue(null, attributeName);
                    if (!TextUtils.isEmpty(value)) {
                        ret.add(value);
                    }
                }
            }
            try {
                eventType = xml.next();
            } catch (Exception e) {
                e.printStackTrace();
                break;
            }
        }
        return ret;
    }


    /**
     * 正则匹配xml元素标签
     */
    public static String[] getElementsByTag(String xmlString, String tagName) {
        Pattern p = Pattern.compile("<" + tagName + "[^>]*?((>.*?</" + tagName + ">)|(/>))");
        Matcher m = p.matcher(xmlString);
        ArrayList<String> al = new ArrayList<String>();
        while (m.find())
            al.add(m.group());
        String[] arr = al.toArray(new String[0]);
        al.clear();
        return arr;
    }

    /**
     * 匹配res/xml/config.xml 文件元素
     */
    public static String getCordovaConfigTag(String tagName, String attributeName) {
        if (TextUtils.isEmpty(tagName) || TextUtils.isEmpty(attributeName)) {
            return null;
        }
        Context context = HcmobileApp.getApplication();
        int id = context.getResources().getIdentifier("config", "xml", context.getPackageName());
        XmlResourceParser xml = context.getResources().getXml(id);
        int eventType = -1;
        while (eventType != XmlPullParser.END_DOCUMENT) {
            if (eventType == XmlPullParser.START_TAG) {
                String strNode = xml.getName();
                if (strNode.equals(tagName)) {
                    String value = xml.getAttributeValue(null, attributeName);
                    if (value != null) {
                        return value;
                    }
                }
            }
            try {
                eventType = xml.next();
            } catch (Exception e) {
                e.printStackTrace();
                break;
            }
        }
        return null;
    }


    /**
     * 打开键盘
     */
    public static void showKeyboard(EditText view) {
        view.requestFocus();
        InputMethodManager inputManager =
                (InputMethodManager) view.getContext().getSystemService(
                        Context.INPUT_METHOD_SERVICE);
        if (inputManager != null) {
            inputManager.showSoftInput(view, 0);
        }
    }

    public static void hideKeyboard(Activity activity) {
        InputMethodManager imm =
                (InputMethodManager) activity.getSystemService(Context.INPUT_METHOD_SERVICE);
        View focus = activity.getCurrentFocus();
        if (focus == null || imm == null) return;
        imm.hideSoftInputFromWindow(focus.getWindowToken(), 0);
    }

    /**
     * 获取下载渠道
     *
     * @param ctx
     * @param key
     * @return
     */
    public static String getAppMetaData(Context ctx, String key) {
        if (ctx == null || TextUtils.isEmpty(key)) {
            return null;
        }
        String resultData = null;
        try {
            PackageManager packageManager = ctx.getPackageManager();
            if (packageManager != null) {
                ApplicationInfo applicationInfo = packageManager.getApplicationInfo(ctx.getPackageName(), PackageManager.GET_META_DATA);
                if (applicationInfo != null) {
                    if (applicationInfo.metaData != null) {
                        resultData = applicationInfo.metaData.getString(key);
                    }
                }

            }
        } catch (PackageManager.NameNotFoundException e) {
            e.printStackTrace();
        }

        return resultData;
    }


    public static boolean shouldOverrideUrlLoading(WebView view, String url) {
        if (!TextUtils.isEmpty(url) && url.startsWith("http") && url.endsWith("apk")) {
            Util.askPermission((Activity) view.getContext(),
                    new String[]{
                            Manifest.permission.READ_EXTERNAL_STORAGE,
                            Manifest.permission.WRITE_EXTERNAL_STORAGE},
                    "").flatMapObservable(new Function<Boolean, ObservableSource<DownInfo>>() {
                @Override
                public ObservableSource<DownInfo> apply(Boolean aBoolean) throws Exception {
                    return HttpManager.fileDownLoad(url, -1);
                }
            }).observeOn(AndroidSchedulers.mainThread()).subscribe(new Observer<DownInfo>() {
                @Override
                public void onSubscribe(Disposable d) {
                }

                @Override
                public void onNext(DownInfo downInfo) {
                    Activity current = (Activity) view.getContext();
                    switch (downInfo.status) {
                        case START:
                            TopProgressDialog.getInstance(current).onLoading("下载中");
                            downInfo.progress.observeOn(AndroidSchedulers.mainThread()).subscribe(new Observer<Progress>() {
                                @Override
                                public void onSubscribe(Disposable d) {
                                }

                                @Override
                                public void onNext(Progress progress) {
                                    TopProgressDialog.getInstance(current).onProgress(progress.progress);
                                }

                                @Override
                                public void onError(Throwable e) {
                                }

                                @Override
                                public void onComplete() {
                                }
                            });
                            break;
                        case FINISH:
                            TopProgressDialog.getInstance(current).onLoadSuccess("下载成功", false);
                            installNewApk((Activity) view.getContext(), downInfo.localFilePath);
                            break;
                        case ERROR:
                            onError(downInfo.exception);
                            break;
                    }
                }

                @Override
                public void onError(Throwable e) {
                    Activity current = (Activity) view.getContext();
                    TopProgressDialog.getInstance(current).onLoadFailure("下载失败 请重试", false);
                }

                @Override
                public void onComplete() {
                }
            });
            return true;
        }
        return false;
    }


    public static boolean shouldOverrideUrlLoading(com.tencent.smtt.sdk.WebView view, String url) {
        if (!TextUtils.isEmpty(url) && url.startsWith("http") && url.endsWith("apk")) {
            Util.askPermission((Activity) view.getContext(),
                    new String[]{
                            Manifest.permission.READ_EXTERNAL_STORAGE,
                            Manifest.permission.WRITE_EXTERNAL_STORAGE},
                    "").flatMapObservable(new Function<Boolean, ObservableSource<DownInfo>>() {
                @Override
                public ObservableSource<DownInfo> apply(Boolean aBoolean) throws Exception {
                    return HttpManager.fileDownLoad(url, -1);
                }
            }).observeOn(AndroidSchedulers.mainThread()).subscribe(new Observer<DownInfo>() {
                @Override
                public void onSubscribe(Disposable d) {
                }

                @Override
                public void onNext(DownInfo downInfo) {
                    Activity current = (Activity) view.getContext();
                    switch (downInfo.status) {
                        case START:
                            TopProgressDialog.getInstance(current).onLoading("下载中");
                            downInfo.progress.observeOn(AndroidSchedulers.mainThread()).subscribe(new Observer<Progress>() {
                                @Override
                                public void onSubscribe(Disposable d) {
                                }

                                @Override
                                public void onNext(Progress progress) {
                                    TopProgressDialog.getInstance(current).onProgress(progress.progress);
                                }

                                @Override
                                public void onError(Throwable e) {
                                }

                                @Override
                                public void onComplete() {
                                }
                            });
                            break;
                        case FINISH:
                            TopProgressDialog.getInstance(current).onLoadSuccess("下载成功", false);
                            installNewApk((Activity) view.getContext(), downInfo.localFilePath);
                            break;
                        case ERROR:
                            onError(downInfo.exception);
                            break;
                    }
                }

                @Override
                public void onError(Throwable e) {
                    Activity current = (Activity) view.getContext();
                    TopProgressDialog.getInstance(current).onLoadFailure("下载失败 请重试", false);
                }

                @Override
                public void onComplete() {
                }
            });
            return true;
        }
        return false;
    }


    /**
     * 安装apk
     */
    public static void installNewApk(Activity activity, File file) {
        /**判断安装包是否存在*/
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
            if (file.exists()) {
                Intent intent = new Intent(Intent.ACTION_VIEW);
                String applicationId = HcmobileApp.getApplication().getPackageName();
                Uri apkUri = FileProvider.getUriForFile(HcmobileApp.getApplication(), applicationId + ".provider", file);
                intent.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION);
                intent.setDataAndType(apkUri, "application/vnd.android.package-archive");
                activity.startActivity(intent);
            }
        } else {
            if (file.exists()) {
                Intent intent = new Intent(Intent.ACTION_VIEW);
                intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                /**设置安装路径和后缀.apk*/
                intent.setDataAndType(Uri.fromFile(file), "application/vnd.android.package-archive");
                activity.startActivity(intent);
            }
        }
    }


    // todo 根据后缀获取文件类型
    public static String getMimeType(String path) {
        String[] fileNameFromUrl = FileUtil.getFileNameFromUrl(path);
        String extension = fileNameFromUrl[1];
        extension = extension.toLowerCase(Locale.getDefault());
        if (extension.equals("3ga")) {
            return "audio/3gpp";
        }
        String type = MimeTypeMap.getSingleton().getMimeTypeFromExtension(extension);
        if (!TextUtils.isEmpty(type)) {
            return type;
        }
        switch (extension) {
            case "json":
                return "application/json";
            case "xml":
                return "text/xml";
            case "html":
                return "text/html";
            case "zip":
                return "application/zip";
            case "pdf":
                return "application/pdf";
            case "doc":
                return "application/msword";// (.doc)
            case "docx":
                return "application/vnd.openxmlformats-officedocument.wordprocessingml.document"; //(.docx)
            case "xls":
                return "application/vnd.ms-excel"; //(.xls)
            case "xlsx":
                return "application/vnd.openxmlformats-officedocument.spreadsheetml.sheet";// (.xlsx)
            case "ppt":
                return "application/vnd.ms-powerpoint";// (.ppt)
            case "pptx":
                return "application/vnd.openxmlformats-officedocument.presentationml.presentation";// (.pptx)
            case "odt":
                return "application/vnd.oasis.opendocument.text";// (.odt)
            case "css":
                return "text/css";
            case "txt":
                return "text/plain";
            case "png":
                return "image/png";
            case "jpeg":
                return "image/jpeg";
            case "gif":
                return "image/gif";
            case "hls":
            case "mp4":
            case "flv":
            case "avi":
            case "3gp":
            case "3gpp":
            case "webm":
            case "ts":
            case "ogv":
            case "m3u8":
            case "asf":
            case "wmv":
            case "rmvb":
            case "rm":
            case "f4v":
            case "dat":
            case "mov":
            case "mpg":
            case "mkv":
            case "mpeg":
            case "mpeg1":
            case "mpeg2":
            case "mpeg3":
            case "xvid":
            case "dvd":
            case "vob":
            case "divx":
                return "audio/mpeg";
            default:
                return extension;
        }
    }


    // todo 根据后缀获取文件类型
    public static String getMimeType(Uri uri, Context context) {
        String mimeType = null;
        if ("content".equals(uri.getScheme())) {
            mimeType = context.getContentResolver().getType(uri);
        } else {
            mimeType = getMimeType(uri.getPath());
        }
        return mimeType;
    }




    public static String getApplicationMd5(Context context) {
        try {
            PackageManager pm = context.getPackageManager();
            Signature sig = pm.getPackageInfo(context.getPackageName(), PackageManager.GET_SIGNATURES).signatures[0];
            String md5Fingerprint = doFingerprint(sig.toByteArray(), "MD5");
            Log.d("sign:", md5Fingerprint);
            return md5Fingerprint;
        } catch (Exception e) {
            e.printStackTrace();
        }
        return null;
    }

    private static String doFingerprint(byte[] certificateBytes, String algorithm) throws Exception {
        MessageDigest md = MessageDigest.getInstance(algorithm);
        md.update(certificateBytes);
        byte[] digest = md.digest();

        String toRet = "";
        for (int i = 0; i < digest.length; i++) {
//            if (i != 0) {
//                toRet += ":";
//            }
            int b = digest[i] & 0xff;
            String hex = Integer.toHexString(b);
            if (hex.length() == 1) {
                toRet += "0";
            }
            toRet += hex;
        }
        return toRet;
    }

}




















































