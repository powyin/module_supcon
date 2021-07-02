package com.supconit.hcmobile;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.app.Application;
import android.content.Context;
import android.content.SharedPreferences;
import android.content.res.XmlResourceParser;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.os.Build;


import android.text.TextUtils;
import android.util.Log;

import com.supconit.hcmobile.appplugin.ApplicationObserver;
import com.supconit.hcmobile.util.AesEncryptUtils;
import com.tencent.smtt.sdk.QbSdk;


import org.apache.cordova.Const;
import org.xmlpull.v1.XmlPullParser;

import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.lang.ref.WeakReference;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashSet;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.io.Serializable;

import static android.content.Context.MODE_PRIVATE;


public class HcmobileApp implements Application.ActivityLifecycleCallbacks, Serializable {
    /**
     * case 0: init
     * case 1: ok success
     * case 2: error
     */
    public static int mTbsCode = 0;
    @SuppressLint("StaticFieldLeak")
    private static Application mContext;
    private static final Handler handler = new Handler(Looper.getMainLooper());
    private static volatile Handler extHandler;
    private static ExecutorService threadPoolExecutor;
    private static LinkedList<WeakReference<Activity>> weakHashMapSet = new LinkedList<>();
    static List<TaskLaunch> mTasks = new ArrayList<>();

    public static Context getApplication() {
        return mContext;
    }

    // todo 主线程handle
    public static Handler getHandle() {
        return handler;
    }

    // todo 非主线程handle
    public synchronized static Handler getExtHandle() {
        if (extHandler == null) {
            synchronized (handler) {
                try {
                    Thread thread = new Thread() {
                        @Override
                        public void run() {
                            synchronized (handler) {
                                if (extHandler != null) {
                                    handler.notifyAll();
                                    return;
                                }
                                Looper.prepare();
                                extHandler = new Handler(Looper.myLooper());
                                handler.notifyAll();
                            }
                            Looper.loop();
                        }
                    };
                    thread.setPriority(10);
                    thread.start();
                    handler.wait();
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        }
        return extHandler;
    }

    // todo 线程池
    public static ExecutorService getThreadPool() {
        if (threadPoolExecutor == null) {
            threadPoolExecutor = Executors.newCachedThreadPool();
        }
        return threadPoolExecutor;
    }

    // todo 获取task中最上层的activity (继承之BaseActivity)
    public static Activity getActivity() {
        Iterator<WeakReference<Activity>> iterator = weakHashMapSet.iterator();
        while (iterator.hasNext()) {
            WeakReference<Activity> next = iterator.next();
            Activity ins = next.get();
            if (ins != null) {
                return ins;
            } else {
                iterator.remove();
            }
        }
        return null;
    }


    // todo 获取task中activity队列 后启动的页面排序靠前
    public static List<Activity> getActivityList() {
        Iterator<WeakReference<Activity>> iterator = weakHashMapSet.iterator();
        ArrayList<Activity> activities = new ArrayList<>();
        while (iterator.hasNext()) {
            WeakReference<Activity> next = iterator.next();
            Activity ins = next.get();
            if (ins != null) {
                activities.add(ins);
            } else {
                iterator.remove();
            }
        }
        return activities;
    }


    // todo 用于EnterActivity安排启动activity顺序 权重值越小启动越早
    public static void scheduleLaunchTask(String activityClass, int weight, boolean single) {
        TaskLaunch taskLaunch = new TaskLaunch();
        taskLaunch.targetClass = activityClass;
        taskLaunch.weight = weight;
        taskLaunch.single = single;
        mTasks.add(taskLaunch);
        Collections.sort(mTasks);
    }

    // todo 重置启动页面 在线
    public static void resetLaunchUrlFromOnline(String url) {
        if (url == null) {
            return;
        }
        if (!url.startsWith("http")) {
            url = "http://" + url;
        }
        SharedPreferences sp = mContext.getSharedPreferences("supconit_hcmobile_android_for_platform", MODE_PRIVATE);
        sp.edit().putString("launchUrl", url).apply();
    }

    // todo 重置启动页面 文件系统
    public static void resetLaunchUrlFromFile(String filePath) {
        if (filePath == null) {
            return;
        }
        if (!filePath.startsWith("file")) {
            filePath = filePath.startsWith("/") ? "file://" + filePath : "file:///" + filePath;
        }
        SharedPreferences sp = mContext.getSharedPreferences("supconit_hcmobile_android_for_platform", MODE_PRIVATE);
        sp.edit().putString("launchUrl", filePath).apply();
    }

    // todo 重置启动页面 assert目录
    public static void resetLaunchUrlFromAssert(String assertPath) {
        if (assertPath == null) {
            return;
        }
        if (!assertPath.startsWith("file:///android_asset")) {
            assertPath = assertPath.startsWith("/") ? "file:///android_asset" + assertPath : "file:///android_asset/" + assertPath;
        }
        SharedPreferences sp = mContext.getSharedPreferences("supconit_hcmobile_android_for_platform", MODE_PRIVATE);
        sp.edit().putString("launchUrl", assertPath).apply();
    }

    @Override
    public void onActivityCreated(Activity activity, Bundle savedInstanceState) {
        ActivityCreated(activity, savedInstanceState);
    }

    @Override
    public void onActivityDestroyed(Activity activity) {
        ActivityDestroyed(activity);
    }

    public static void ActivityCreated(Activity activity, Bundle savedInstanceState) {
        Iterator<WeakReference<Activity>> iterator = weakHashMapSet.iterator();
        while (iterator.hasNext()) {
            WeakReference<Activity> next = iterator.next();
            Activity ins = next.get();
            if (ins == activity || ins == null) {
                iterator.remove();
            }
        }
        weakHashMapSet.addFirst(new WeakReference<Activity>(activity));
    }


    public static void ActivityDestroyed(Activity activity) {
        Iterator<WeakReference<Activity>> iterator = weakHashMapSet.iterator();
        while (iterator.hasNext()) {
            WeakReference<Activity> next = iterator.next();
            Activity ins = next.get();
            if (ins == activity || ins == null) {
                iterator.remove();
            }
        }
    }


    public void init(Application application, String key) {
        Const.keySdk = key;
        onCreate(application);
    }

    private void onCreate(Application application) {
        // super.onCreate();
        mContext = application;
        disableAPIDialog();

        new HotObserver().onCreate();
        application.registerActivityLifecycleCallbacks(this);

        QbSdk.initX5Environment(application, new QbSdk.PreInitCallback() {
            @Override
            public void onCoreInitFinished() {
                Log.d("powyin", "tbs : onCoreInitFinished");
            }

            @Override
            public void onViewInitFinished(boolean initResult) {
                Log.d("powyin", "tbs : onViewInitFinished " + initResult);
                if (initResult) {
                    mTbsCode = 1;
                } else {
                    mTbsCode = 2;
                }
            }
        });

        HashSet<String> applicationObservableList = getApplicationObservableList();
        for (String observerClass : applicationObservableList) {
            try {
                Class c = Class.forName(observerClass);
                ApplicationObserver observer = (ApplicationObserver) c.newInstance();
                observer.setContext(mContext);
                observer.onCreate();
            } catch (Exception e) {
                e.printStackTrace();
            }
        }

//        ConfigXmlParser parser = new ConfigXmlParser();
//        parser.parse(this);
//        ObserverManager observerManager = new ObserverManager(this, parser.getPluginEntries());
//        observerManager.onCreate();
    }

    /**
     * 反射 禁止弹窗
     */
    private void disableAPIDialog() {
        if (Build.VERSION.SDK_INT < 28) return;
        try {
            Class clazz = Class.forName("android.app.ActivityThread");
            Method currentActivityThread = clazz.getDeclaredMethod("currentActivityThread");
            currentActivityThread.setAccessible(true);
            Object activityThread = currentActivityThread.invoke(null);
            Field mHiddenApiWarningShown = clazz.getDeclaredField("mHiddenApiWarningShown");
            mHiddenApiWarningShown.setAccessible(true);
            mHiddenApiWarningShown.setBoolean(activityThread, true);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }


    @Override
    public void onActivityStarted(Activity activity) {
    }

    @Override
    public void onActivityResumed(Activity activity) {
    }

    @Override
    public void onActivityPaused(Activity activity) {
    }

    @Override
    public void onActivityStopped(Activity activity) {
    }

    @Override
    public void onActivitySaveInstanceState(Activity activity, Bundle outState) {
    }


    static class TaskLaunch implements Comparable<TaskLaunch> {
        String targetClass;
        int weight;
        boolean single;

        @Override
        public int compareTo(TaskLaunch o) {
            return o.weight - weight;
        }
    }

    private static HashSet<String> getApplicationObservableList() {
        HashSet<String> ret = new HashSet<>();
        Context context = HcmobileApp.getApplication();
        int id = context.getResources().getIdentifier("config", "xml", context.getPackageName());
        XmlResourceParser xml = context.getResources().getXml(id);
        int eventType = -1;
        while (eventType != XmlPullParser.END_DOCUMENT) {
            if (eventType == XmlPullParser.START_TAG) {
                String strNode = xml.getName();
                if ("param".equals(strNode)) {
                    String paramType = xml.getAttributeValue(null, "name");
                    if ("application-observer-package".equals(paramType)) {
                        String value = xml.getAttributeValue(null, "value");
                        if (!TextUtils.isEmpty(value)) {
                            ret.add(value);
                        }
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

}





