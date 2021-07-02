package com.supconit.hcmobile.plugins.debug.server;

import android.app.Activity;
import android.content.res.AssetManager;
import android.text.TextUtils;
import android.util.Pair;
import android.view.View;
import android.webkit.ConsoleMessage;

import com.koushikdutta.async.http.body.AsyncHttpRequestBody;
import com.koushikdutta.async.http.server.AsyncHttpServerRequest;
import com.koushikdutta.async.http.server.AsyncHttpServerResponse;
import com.koushikdutta.async.http.server.HttpServerRequestCallback;
import com.supconit.hcmobile.HcmobileApp;
import com.supconit.hcmobile.MainActivity;
import com.supconit.hcmobile.plugins.debug.project.WorkSpace;
import com.supconit.inner_hcmobile.R;
import com.supconit.hcmobile.plugins.debug.data.DataCenter;
import com.supconit.hcmobile.util.Util;

import org.json.JSONArray;

import java.io.File;
import java.io.InputStream;
import java.util.Iterator;

public class GetServer implements HttpServerRequestCallback {
    // todo get 请求直接返回assert中http_server目录文件
    @Override
    public void onRequest(AsyncHttpServerRequest request, AsyncHttpServerResponse response) {

        response.getHeaders().add("Access-Control-Allow-Origin", "*");
        response.getHeaders().add("Access-Control-Allow-Methods", "POST,GET,OPTIONS,DELETE");
        response.getHeaders().add("Access-Control-Max-Age", "3600");
        response.getHeaders().add("Access-Control-Allow-Headers",
                "*, userName, space_id, spaceid, parentid, filename, Origin, X-Requested-With, Content-Type, Accept, " +
                        "WG-App-Version, WG-Device-Id, WG-Network-Type, WG-Vendor, WG-OS-Type, WG-OS-Version, WG-Device-Model, WG-CPU, WG-Sid, WG-App-Id, WG-Token");
        response.getHeaders().add("Access-Control-Allow-Credentials", "true");


        forceShowDebugView(getCurrentShowingCordovaWebView());
        String path = request.getPath();
        if ("/".equals(path) || "".equals(path) || path == null) {
            path = "/index.html";
        }

        if (("/native_os").equals(path)) {
            response.send("android");
            return;
        }

        if ("/native_log".equals(path)) {
            String bodyJson = null;
            try {
                bodyJson = request.getBody().get().toString();
            } catch (Exception ignore) {
            }
            WorkSpace.post_getLog(request,response,bodyJson);
            return;
        }


        if (path.startsWith("/icon_file_mark")) {
            path = path.replace("/icon_file_mark", "file_icon");
            AssetManager assetManager = HcmobileApp.getApplication().getAssets();
            try {
                InputStream pathFile = assetManager.open(path);
                response.sendStream(pathFile, pathFile.available());
            } catch (Exception e) {
                e.printStackTrace();
                try {
                    InputStream pathFile = assetManager.open("file_icon/common.png");
                    response.sendStream(pathFile, pathFile.available());
                } catch (Exception ig) {
                    ig.printStackTrace();
                    response.code(300);
                    response.end();
                }
            }
            return;
        }


        if (path.startsWith("/get_file_local")) {
            path = path.replace("/get_file_local", "");
            File file = new File(path);
            if (!file.exists() || file.isDirectory()) {
                response.code(300);
                response.send("没找到图片");
            } else {
                response.sendFile(file);
            }
            return;
        }

        if (path.startsWith("/downloadFileOrZip")) {
            WorkSpace.post_downloadFileOrZip(request,response,"");
            return;
        }


        path = Util.pathAppend("hc_ide_server", path);
        AssetManager assetManager = HcmobileApp.getApplication().getAssets();
        try {
            InputStream pathFile = assetManager.open(path);
            response.sendStream(pathFile, pathFile.available());
        } catch (Exception e) {
            e.printStackTrace();
            response.code(300);
            response.end();
        }

    }


    /**
     * 获取最上层cordova实例对应的viewView
     */
    private MainActivity getCurrentShowingCordovaWebView() {
        Iterator<Activity> iterator = HcmobileApp.getActivityList().iterator();
        MainActivity mainActivity = null;
        while (iterator.hasNext()) {
            Activity next = iterator.next();
            if (next instanceof MainActivity) {
                mainActivity = (MainActivity) next;
                return mainActivity;
            }
        }
        return null;
    }

    private void forceShowDebugView(MainActivity currentShowingCordovaWebView) {
//        if (currentShowingCordovaWebView == null) return;
//        final View logSettingView = currentShowingCordovaWebView.findViewById(R.id.hc_setting);
//        if (logSettingView.getVisibility() != View.VISIBLE) {
//            HcmobileApp.getHandle().post(new Runnable() {
//                @Override
//                public void run() {
//                    logSettingView.setVisibility(View.VISIBLE);
//                }
//            });
//        }
    }


}
