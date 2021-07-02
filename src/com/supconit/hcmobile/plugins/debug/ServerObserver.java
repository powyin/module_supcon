package com.supconit.hcmobile.plugins.debug;

import android.app.Activity;
import android.app.Application;
import android.os.Bundle;
import android.text.TextUtils;

import com.koushikdutta.async.AndroidAsyncFileUtil;
import com.koushikdutta.async.http.server.AsyncHttpServer;
import com.koushikdutta.async.http.server.AsyncHttpServerRequest;
import com.koushikdutta.async.http.server.AsyncHttpServerResponse;
import com.koushikdutta.async.http.server.AsyncHttpServerRouter;
import com.koushikdutta.async.http.server.HttpServerRequestCallback;
import com.powyin.scroll.adapter.MultipleRecycleAdapter;
import com.supconit.hcmobile.HcmobileApp;
import com.supconit.hcmobile.MainActivity;
import com.supconit.hcmobile.util.Util;
import com.supconit.inner_hcmobile.R;
import com.supconit.hcmobile.appplugin.ApplicationObserver;
import com.supconit.hcmobile.plugins.debug.server.GetServer;
import com.supconit.hcmobile.plugins.debug.server.PostServer;
import com.supconit.hcmobile.util.NetUtil;

import java.util.HashMap;


public class ServerObserver extends ApplicationObserver implements Application.ActivityLifecycleCallbacks {
    public static int serverPort;

    @Override
    public void onCreate() {
        AndroidAsyncFileUtil.init(getApplicationContext());
        getApplicationContext().registerActivityLifecycleCallbacks(this);
        for (int i = 5000; i < 10000; i += 1500) {
            if (!NetUtil.isPortUsing(i)) {
                AsyncHttpServer server = new AsyncHttpServer();

                server.get("\\S*", new GetServer());
                server.post("\\S*", new PostServer());
                server.addAction("OPTIONS", "\\S*", new HttpServerRequestCallback() {
                    @Override
                    public void onRequest(AsyncHttpServerRequest asyncHttpServerRequest, AsyncHttpServerResponse asyncHttpServerResponse) {

                        asyncHttpServerResponse.getHeaders().add("Access-Control-Allow-Origin", "*");
                        asyncHttpServerResponse.getHeaders().add("Access-Control-Allow-Methods", "POST,GET,OPTIONS,DELETE");
                        asyncHttpServerResponse.getHeaders().add("Access-Control-Max-Age", "3600");
                        asyncHttpServerResponse.getHeaders().add("Access-Control-Allow-Headers",
                                "*, userName, space_id, spaceid, parentid, filename, Origin, X-Requested-With, Content-Type, Accept, " +
                                        "WG-App-Version, WG-Device-Id, WG-Network-Type, WG-Vendor, WG-OS-Type, WG-OS-Version, WG-Device-Model, WG-CPU, WG-Sid, WG-App-Id, WG-Token");
                        asyncHttpServerResponse.getHeaders().add("Access-Control-Allow-Credentials", "true");

                        asyncHttpServerResponse.send("");
                    }
                });
                server.listen(i);
                serverPort = i;
                break;
            }
        }

        String debug = Util.getCordovaConfigTag("buildType", "value");
        debug = TextUtils.isEmpty(debug) ? "" : debug;
        String appName = HcmobileApp.getApplication().getResources().getString(R.string.app_name);

        if ("debug".equals(debug.toLowerCase()) && (!"cloud app".equalsIgnoreCase(appName)) && (!"CloudGrid".equalsIgnoreCase(appName)) && (!"Cloud Grid".equalsIgnoreCase(appName))) {
            HcmobileApp.scheduleLaunchTask("com.supconit.hcmobile.plugins.debug.DebugSignActivity", 18, false); // 权重越小 越优先启动
        }
    }

    @Override
    public void onActivityCreated(Activity activity, Bundle savedInstanceState) {
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

//    String cordovaConfigTag = Util.getCordovaConfigTag("debugingSetting", "value");
//        if ("true".equals(cordovaConfigTag)) {
//        // logSettingView.setVisibility(View.VISIBLE);
//    }else {
//        // logSettingView.setVisibility(View.GONE);
//    }

    @Override
    public void onActivityStarted(Activity activity) {
        if (activity instanceof MainActivity && activity.findViewById(R.id.hc_mobile_debug_sin_page_block) == null) {
            new ActivityDebugHolder((MainActivity) activity);
        }
    }

    @Override
    public void onActivityDestroyed(Activity activity) {
    }


}
