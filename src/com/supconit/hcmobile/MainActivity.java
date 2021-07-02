package com.supconit.hcmobile;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.Log;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.webkit.ConsoleMessage;
import android.webkit.ValueCallback;
import android.webkit.WebView;
import android.widget.FrameLayout;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.gyf.immersionbar.ImmersionBar;
import com.supconit.hcmobile.model.ConsoleMs;
import com.supconit.hcmobile.net.HttpManager;
import com.supconit.hcmobile.util.FileUtil;
import com.supconit.hcmobile.util.JsonUtil;
import com.supconit.hcmobile.util.Util;
import com.supconit.hcmobile.widget.HCMProgressBar;

import org.apache.cordova.Const;
import org.apache.cordova.CordovaActivity;
import org.apache.cordova.CordovaWebView;
import org.apache.cordova.CordovaWebViewEngine;
import org.apache.cordova.CordovaWebViewImpl;
import org.apache.cordova.engine.SystemWebChromeClient;
import org.apache.cordova.engine.SystemWebView;
import org.apache.cordova.engine.SystemWebViewEngine;
import org.apache.cordova.exEngine.ExSystemWebView;

import io.reactivex.SingleObserver;
import io.reactivex.disposables.Disposable;
import io.reactivex.subjects.PublishSubject;

import com.supconit.inner_hcmobile.R;

import java.lang.reflect.Field;
import java.util.Collections;
import java.util.Iterator;
import java.util.Vector;

public class MainActivity extends CordovaActivity {

    private static final String HIDE_PROGRESS = "HCMOBILE_HIDE_PROGRESS";
    private static int NAVIGATION_BAR_HEIGHT = -1;
    private static int index = 0;

    public final Vector<ConsoleMs> consoleMessageList = new Vector<>();
    public final PublishSubject<ConsoleMs> consoleMessagePublishSubject = PublishSubject.create();

    private HCMProgressBar progressBar;
    public ExSystemWebView mSystemWebView;

    public boolean frontShow;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        getWindow().addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);
        setContentView(R.layout.layout_main);

        super.init();

        // todo 获取启动页面 launchUrl
        SharedPreferences sp = getSharedPreferences("supconit_hcmobile_android_for_platform", MODE_PRIVATE);
        if (sp.getString("launchUrl", null) != null) {
            launchUrl = sp.getString("launchUrl", null);
        } else {
            sp.edit().putString("launchUrl", launchUrl).apply();
        }

        // todo 用于 debug or 多开cordova页面 or 小程序打开
        String intentLaunch = getIntent().getStringExtra("launchUrl");
        if (intentLaunch != null && intentLaunch.length() > 0) {
            launchUrl = intentLaunch;
        }
        loadUrl(launchUrl);

        System.out.println(":::::::::::::::::::::::::::::::::::::::;  MainActivity  onCreate " + this);

        initAppTitle();
        checkoutLimit();
        HcmobileApp.ActivityCreated(this, null);
    }


    public void setYs(boolean color) {
        Log.e("我是当前的页面", getClass().getSimpleName());
        ImmersionBar.with(this).statusBarDarkFont(color).init();
    }

    @Override
    protected void onResume() {
        super.onResume();
        frontShow = true;
    }

    @Override
    protected void onPause() {
        super.onPause();
        frontShow = false;
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        System.out.println(":::::::::::::::::::::::::::::::::::::::;  MainActivity  onDestroy " + this);

        HcmobileApp.ActivityDestroyed(this);
    }


    @Override
    protected CordovaWebView makeWebView() {
        boolean isX5 = true;
        RelativeLayout layout = findViewById(R.id.hc_mobile_main_view_group);

        SystemWebView webView = new SystemWebView(this);
        webView.setBackgroundColor(0xffffffff);
        layout.addView(webView, new RelativeLayout.LayoutParams(-1, -1));
        mSystemWebView = webView;
        SystemWebViewEngine engine = new SystemWebViewEngine(webView);
        webView.setWebChromeClient(new SystemWebChromeClient(engine) {
            @Override
            public void onProgressChanged(WebView view, int newProgress) {
                progressBar.setProgress(newProgress);
            }

            @Override
            public boolean onConsoleMessage(ConsoleMessage consoleMessage) {
                ConsoleMs ms = new ConsoleMs();
                ms.createTime = System.currentTimeMillis();
                ms.message = consoleMessage.message();
                ms.level = consoleMessage.messageLevel().name();
                ms.lineNumber = consoleMessage.lineNumber();
                ms.sourceId = consoleMessage.sourceId();
                ms.index = (++index);
                synchronized (consoleMessageList) {
                    consoleMessageList.add(ms);
                    Iterator<ConsoleMs> iterator = consoleMessageList.iterator();
                    while (iterator.hasNext()) {
                        ConsoleMs next = iterator.next();
                        if (System.currentTimeMillis() - next.createTime > 5 * 60 * 1000) {
                            iterator.remove();
                        }
                    }
                }
                consoleMessagePublishSubject.onNext(ms);
                return super.onConsoleMessage(consoleMessage);
            }

            @Override
            public void onReceivedTitle(WebView view, String title) {
                super.onReceivedTitle(view, title);
                TextView textView = findViewById(R.id.hc_mini_title_uni_uni);
                if (textView != null) {
                    textView.setText(title);
                }

                // todo 判断返回键是否可用
                lab:
                {
                    View viewById = findViewById(R.id.hc_mini_click_left_uni_uni);
                    if (viewById != null) {
                        if (webView.canGoBack()) {
                            String failingUrl = getFailingUrl();
                            CordovaWebViewEngine.WebBackForwardListHC history = appView.getEngine().copyBackForwardList();
                            for (int i = history.getSize() - 2; i >= 0; i--) {
                                CordovaWebViewEngine.WebHistoryItemHC item = history.getItemAtIndex(i);
                                String url = item.getUrl();
                                if (url != null && !url.equals(failingUrl) && !Const.errorUrls.contains(url)) {
                                    viewById.setClickable(true);
                                    viewById.setVisibility(View.VISIBLE);
                                    break lab;
                                }
                            }
                        }

                        viewById.setClickable(false);
                        viewById.setVisibility(View.INVISIBLE);
                    }
                }
            }


        });
        return new CordovaWebViewImpl(engine);

    }

    @Override
    protected void createViews() {
        progressBar = findViewById(R.id.progressBar);
        progressBar.reset();
        progressBar.bringToFront();
        appView.getView().requestFocusFromTouch();

    }

    @Override
    public Object onMessage(String id, Object data) {
        if ("onPageStarted".endsWith(id)) {
            if (data != null) {
                HcmobileApp.getHandle().post(new Runnable() {
                    @Override
                    public void run() {
                        if (data.toString().startsWith("http") && !data.toString().contains(HIDE_PROGRESS)) {
                            progressBar.startLoad();
                        }
                    }
                });
                Log.i("onPageStarted:", data.toString());
            }
        } else if ("onPageFinished".equals(id)) {
            HcmobileApp.getHandle().post(new Runnable() {
                @Override
                public void run() {
                    progressBar.endLoad();
                }
            });
            Log.i("onPageFinished:", data.toString());
        }
        return super.onMessage(id, data);
    }

    private void initAppTitle() {
        if (getClass() != MainActivity.class) {
            return;
        }
        String cordovaConfigTag = Util.getCordovaConfigTag("app_title_show", "value");
        if ("true".equals(cordovaConfigTag) || "1".equals(cordovaConfigTag)) {
            ViewGroup view = (ViewGroup) this.getLayoutInflater().inflate(R.layout.layout_app_title_uni_uni, null, false);
            String toolbarBackColor = Util.getCordovaConfigTag("app_title_bac", "value");
            if (!TextUtils.isEmpty(toolbarBackColor)) {

                if (toolbarBackColor.startsWith("#")) {
                    toolbarBackColor = toolbarBackColor.substring(1);
                }

                if (toolbarBackColor.contains("x")) {
                    toolbarBackColor = toolbarBackColor.substring(toolbarBackColor.indexOf('x') + 1);
                }
                if (toolbarBackColor.endsWith(")")) {
                    toolbarBackColor = toolbarBackColor.substring(0, toolbarBackColor.length() - 1);
                }

                if ((toolbarBackColor.length() == 6) || (toolbarBackColor.length() == 8)) {
                    if (toolbarBackColor.length() == 8) {
                        toolbarBackColor = toolbarBackColor.substring(2);
                    }
                    try {
                        int integer = Integer.parseInt(toolbarBackColor, 16);
                        view.setBackgroundColor(integer | 0xff000000);
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                }
            }

            view.findViewById(R.id.hc_mini_click_left_uni_uni).setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    WebView webView = (WebView) mSystemWebView;
                    if (webView.canGoBack()) {
                        String failingUrl = getFailingUrl();
                        CordovaWebViewEngine.WebBackForwardListHC history = appView.getEngine().copyBackForwardList();
                        for (int i = history.getSize() - 2; i >= 0; i--) {
                            CordovaWebViewEngine.WebHistoryItemHC item = history.getItemAtIndex(i);
                            String url = item.getUrl();
                            if (url != null && !url.equals(failingUrl) && !Const.errorUrls.contains(url)) {
                                int index = i - history.getSize() + 1;
                                appView.getEngine().goBackOrForward(index);
                                return;
                            }
                        }
                    }
                    appView.getPluginManager().postMessage("exit", null);
                }
            });


            if (isImmersionShow()) {
                View viewById = view.findViewById(R.id.hc_mini_back_color_uni_uni);
                FrameLayout.LayoutParams layoutParams = (FrameLayout.LayoutParams) viewById.getLayoutParams();
                layoutParams.topMargin = getNavigationBarHeight();
            }
            view.setId(R.id.hc_app_title_uni);

            LinearLayout linearLayout = (LinearLayout) findViewById(R.id.hc_mobile_main_view_group_content);
            int childCount = linearLayout.getChildCount();
            for (int i = 0; i < childCount; i++) {
                View childAt = linearLayout.getChildAt(i);
                if (childAt.getId() == R.id.hc_app_title_uni) {
                    linearLayout.removeViewAt(i);
                    childCount = linearLayout.getChildCount();
                }
            }

            LinearLayout.LayoutParams layoutParams = new LinearLayout.LayoutParams(-1, (int) -2);
            linearLayout.addView(view, 0, layoutParams);
        }
    }


    // 得到底部透明导航栏高度
    public int getNavigationBarHeight() {
        if (NAVIGATION_BAR_HEIGHT < 0) {
            try {
                @SuppressLint("PrivateApi") Class clazz = Class.forName("com.android.internal.R$dimen");
                Object obj = clazz.newInstance();
                Field field = clazz.getField("status_bar_height");
                int x = Integer.parseInt(field.get(obj).toString());
                NAVIGATION_BAR_HEIGHT = getResources().getDimensionPixelSize(x);
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
        return NAVIGATION_BAR_HEIGHT;
    }


    private void checkoutLimit() {
        SharedPreferences sharedPreferences = MainActivity.this.getPreferences(MODE_PRIVATE);
        if ("true".equals(sharedPreferences.getString("neverExpireFlag", "false"))) {
            return;
        }

        String expireTime = sharedPreferences.getString("expireTime", "");
        if (!TextUtils.isEmpty(expireTime)) {
            try {
                long time = Long.parseLong(expireTime);
                if (time > System.currentTimeMillis()) {
                    return;
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
        }

        String url = Util.getCordovaConfigTag("hc_mobile_server_address", "value");
        String appId = Util.getCordovaConfigTag("appID", "value");
        if (!TextUtils.isEmpty(appId) && appId.startsWith("_")) {
            appId = appId.substring("_".length());
        }

        if (TextUtils.isEmpty(url) || TextUtils.isEmpty(appId)) {
            return;
        }


        url = Util.pathAppend(url, "app/service/duration/get");
        HttpManager.get(url, Collections.singletonMap("uniqueCode", appId), String.class).subscribe(new SingleObserver<String>() {
            @Override
            public void onSubscribe(Disposable d) {
            }

            @Override
            public void onSuccess(String s) {

                String neverExpireFlag = JsonUtil.getJsonString(s, "result", "neverExpireFlag");
                if ("true".equals(neverExpireFlag)) {
                    sharedPreferences.edit().putString("neverExpireFlag", "true").apply();
                    return;
                }

                String expireTime = JsonUtil.getJsonString(s, "result", "expireTime");
                if (!TextUtils.isEmpty(expireTime)) {
                    sharedPreferences.edit().putString("expireTime", expireTime).apply();
                }

                String expireFlag = JsonUtil.getJsonString(s, "result", "expireFlag");
                if ("true".equals(expireFlag)) {
                    HcmobileApp.getHandle().post(new Runnable() {
                        @Override
                        public void run() {
                            showLimit();
                        }
                    });
                }

            }

            @Override
            public void onError(Throwable e) {
            }
        });
    }

    private void showLimit() {
        FrameLayout preViewContent = (FrameLayout) findViewById(android.R.id.content).getRootView();
        for (int i = 0; i < preViewContent.getChildCount(); i++) {
            View childAt = preViewContent.getChildAt(i);
            if (childAt.getId() == R.id.sup_con_limit_use_cover_content) {
                return;
            }
        }

        View inflate = getLayoutInflater().inflate(R.layout.layout_app_limit_use_cover, null, false);
        FrameLayout.LayoutParams layoutParams = new FrameLayout.LayoutParams(-1, -1);
        preViewContent.addView(inflate, layoutParams);
        inflate.setId(R.id.sup_con_limit_use_cover_content);
        inflate.findViewById(R.id.sup_con_limit_use_cover).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                preViewContent.removeView(inflate);
            }
        });
        inflate.findViewById(R.id.sup_con_limit_use_image).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
            }
        });
        inflate.findViewById(R.id.sup_con_limit_use_close).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                preViewContent.removeView(inflate);
            }
        });
        inflate.findViewById(R.id.sup_con_limit_use_url).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                FileUtil.previewFileWithSystemApp(MainActivity.this, "https://www.yd-mobile.cn/#/");
            }
        });
    }


}

















