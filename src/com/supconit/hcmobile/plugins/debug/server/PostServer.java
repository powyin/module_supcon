package com.supconit.hcmobile.plugins.debug.server;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.net.Uri;
import android.text.TextUtils;
import android.view.View;

import com.google.gson.Gson;
import com.koushikdutta.async.http.body.AsyncHttpRequestBody;
import com.koushikdutta.async.http.body.MultipartFormDataBody;
import com.koushikdutta.async.http.server.AsyncHttpServerRequest;
import com.koushikdutta.async.http.server.AsyncHttpServerResponse;
import com.koushikdutta.async.http.server.HttpServerRequestCallback;
import com.supconit.hcmobile.HcmobileApp;
import com.supconit.hcmobile.MainActivity;
import com.supconit.hcmobile.plugins.debug.project.WorkSpace;
import com.supconit.inner_hcmobile.R;
import com.supconit.hcmobile.plugins.debug.data.DataCenter;
import com.supconit.hcmobile.plugins.debug.data.FileDes;
import com.supconit.hcmobile.util.FileUtil;

import java.io.File;
import java.util.Iterator;
import java.util.List;

public class PostServer implements HttpServerRequestCallback {

    public static int inc;

    @Override
    public void onRequest(AsyncHttpServerRequest asyncHttpServerRequest, AsyncHttpServerResponse asyncHttpServerResponse) {

        asyncHttpServerResponse.getHeaders().add("Access-Control-Allow-Origin", "*");
        asyncHttpServerResponse.getHeaders().add("Access-Control-Allow-Methods", "POST,GET,OPTIONS,DELETE");
        asyncHttpServerResponse.getHeaders().add("Access-Control-Max-Age", "3600");
        asyncHttpServerResponse.getHeaders().add("Access-Control-Allow-Headers",
                "*, userName, space_id, spaceid, parentid, filename, Origin, X-Requested-With, Content-Type, Accept, " +
                        "WG-App-Version, WG-Device-Id, WG-Network-Type, WG-Vendor, WG-OS-Type, WG-OS-Version, WG-Device-Model, WG-CPU, WG-Sid, WG-App-Id, WG-Token");
        asyncHttpServerResponse.getHeaders().add("Access-Control-Allow-Credentials", "true");


        try {
            String path = asyncHttpServerRequest.getPath();
            final MainActivity currentShowingCordovaWebView = getCurrentShowingCordovaWebView();
            // ????????????????????????UI
            forceShowDebugView(getCurrentShowingCordovaWebView());

            String bodyString = null;
            String dPath;
            MultipartFormDataBody multipartFormDataBody;
            try {
                Object o = asyncHttpServerRequest.getBody().get();
                bodyString = (o instanceof String) ? Uri.decode((String) o) : null;
            } catch (Exception e) {
                e.printStackTrace();
            }

            String bodyJson = null;
            try {
                AsyncHttpRequestBody body = asyncHttpServerRequest.getBody();
                bodyJson = asyncHttpServerRequest.getBody().get().toString();
            } catch (Exception ignore) {
            }

            switch (path) {
                // todo ????????????????????????cordovaActivity?????????????????????html??????
                case "/reload_html_content":
                case "/proxy/reload_html_content":
                    if (bodyString == null || bodyString.length() == 0) {
                        asyncHttpServerResponse.code(300);
                        asyncHttpServerResponse.send("????????????body???????????? HTML");
                        return;
                    }
                    currentShowingCordovaWebView.loadUrlContent(bodyString);
                    asyncHttpServerResponse.code(200);
                    asyncHttpServerResponse.send("ok");
                    break;
                // todo ????????????cordovaActivity????????????jsCode
                case "/run_js_code":
                case "/proxy/run_js_code":
                    if (bodyString == null || bodyString.length() == 0) {
                        asyncHttpServerResponse.code(300);
                        asyncHttpServerResponse.send("????????????body????????????js?????????");
                        return;
                    }
                    final String jsCode = bodyString;
                    if (currentShowingCordovaWebView != null) {
                        HcmobileApp.getHandle().post(new Runnable() {
                            @Override
                            public void run() {
                                File cacheZip = FileUtil.getRandomFilePath(HcmobileApp.getApplication(), "code_run_js" + (inc++), "js", false);
                                WorkSpace.copy(jsCode, cacheZip);
                                String address = cacheZip.getAbsolutePath();
                                if (address.startsWith("/")) {
                                    address = "file://" + address;
                                } else {
                                    address = "file:///" + address;
                                }
                                String js = "var src = document.createElement('script');\n" +
                                        "    src.type = 'text/javascript';\n" +
                                        "    src.async = true;\n" +
                                        "    src.charset = 'utf-8';\n" +
                                        "    src.src = '" + address + "';\n" +
                                        "    document.head.appendChild(src);";
                                currentShowingCordovaWebView.loadUrl("javascript: " + js);
                            }
                        });
                    }
                    asyncHttpServerResponse.code(200);
                    asyncHttpServerResponse.send("ok");
                    break;
                // todo ????????????
                case "/getFileDesc":
                case "/proxy/getFileDesc":
                    dPath = asyncHttpServerRequest.getHeaders().getMultiMap().getString("path");
                    if (TextUtils.isEmpty(dPath)) {
                        asyncHttpServerResponse.code(300);
                        asyncHttpServerResponse.send("missing par path ????????? header ????????????????????? path");
                        return;
                    }
                    FileDes des = DataCenter.getInstance().getPath(dPath);
                    if (des == null) {
                        asyncHttpServerResponse.code(300);
                        asyncHttpServerResponse.send("not found such path file");
                    } else {
                        Gson gson = new Gson();
                        asyncHttpServerResponse.code(200);
                        asyncHttpServerResponse.send(gson.toJson(des));
                    }
                    break;
                // todo ????????????
                case "//uploadFile":
                case "/proxy/uploadFile":
                    multipartFormDataBody = asyncHttpServerRequest.getBody();
                    dPath = asyncHttpServerRequest.getHeaders().getMultiMap().getString("path");
                    String toPath = dPath;
                    if (TextUtils.isEmpty(dPath)) {
                        asyncHttpServerResponse.code(300);
                        asyncHttpServerResponse.send("??????header?????? toPath ??????????????????");
                        asyncHttpServerResponse.end();
                        return;
                    }
                    multipartFormDataBody.onFileDownLoadBack = new MultipartFormDataBody.OnFileDownLoadBack() {
                        @Override
                        public void onFile(File file) {
                            if (file == null || !file.exists() || file.length() == 0) {
                                asyncHttpServerResponse.code(300);
                                asyncHttpServerResponse.send("????????????");
                                asyncHttpServerResponse.end();
                            } else {
                                DataCenter.getInstance().writeFile(toPath, file);
                                asyncHttpServerResponse.code(200);
                                asyncHttpServerResponse.send("????????????: toPath=" + toPath + "  :store=" + file.getAbsolutePath());
                                asyncHttpServerResponse.end();
                            }
                        }
                    };
                    break;
                // todo ???????????????
                case "/createDir":
                case "/proxy/createDir":
                    dPath = asyncHttpServerRequest.getHeaders().getMultiMap().getString("path");
                    if (TextUtils.isEmpty(dPath)) {
                        asyncHttpServerResponse.code(300);
                        asyncHttpServerResponse.send("??????header?????? toPath ??????????????????");
                        asyncHttpServerResponse.end();
                        return;
                    }

                    File temFile = DataCenter.getInstance().getFile(dPath);
                    FileUtil.ensureFileExist(temFile, true);
                    DataCenter.getInstance().resetRootConfig();
                    asyncHttpServerResponse.code(200);
                    asyncHttpServerResponse.send("????????????: toPath=" + temFile.getAbsolutePath());
                    asyncHttpServerResponse.end();

                    break;
                // todo ????????????
                case "/downloadFile":
                case "/proxy/downloadFile":
                    dPath = asyncHttpServerRequest.getHeaders().getMultiMap().getString("path");
                    if (TextUtils.isEmpty(dPath)) {
                        asyncHttpServerResponse.code(300);
                        asyncHttpServerResponse.send("missing par path ????????? header ????????????????????? path");
                        return;
                    }
                    File file = DataCenter.getInstance().getFile(dPath);
                    if (file.exists()) {
                        asyncHttpServerResponse.sendFile(file);
                    } else {
                        asyncHttpServerResponse.code(300);
                        asyncHttpServerResponse.send("cannot find file in path :" + file.getAbsolutePath());
                    }
                    break;
                // case "/deleteFile":
                case "/proxy/deleteFile":
                    dPath = asyncHttpServerRequest.getHeaders().getMultiMap().getString("path");
                    if (TextUtils.isEmpty(dPath)) {
                        asyncHttpServerResponse.code(300);
                        asyncHttpServerResponse.send("missing par path ????????? header ????????????????????? path");
                        return;
                    }
                    DataCenter.getInstance().deleteFile(dPath);
                    asyncHttpServerResponse.code(200);
                    asyncHttpServerResponse.send("ok");
                    break;
                // todo ??????zip??? ?????????????????????????????????
                case "/uploadZip":
                case "/proxy/uploadZip":
                    multipartFormDataBody = asyncHttpServerRequest.getBody();
                    multipartFormDataBody.onFileDownLoadBack = new MultipartFormDataBody.OnFileDownLoadBack() {
                        @Override
                        public void onFile(File file) {
                            if (file == null || !file.exists() || file.length() == 0) {
                                asyncHttpServerResponse.code(300);
                                asyncHttpServerResponse.send("????????????");
                                asyncHttpServerResponse.end();
                            } else {
                                DataCenter.getInstance().uZipFileToRootDirectory(file);
                                asyncHttpServerResponse.code(200);
                                asyncHttpServerResponse.send("???????????? store=" + file.getAbsolutePath());
                                asyncHttpServerResponse.end();
                            }
                        }
                    };
                    break;
                // todo ????????????
                case "/clearAllFile":
                case "/proxy/clearAllFile":
                    DataCenter.getInstance().clear();
                    asyncHttpServerResponse.code(200);
                    asyncHttpServerResponse.send("success");
                    asyncHttpServerResponse.end();
                    break;
                // todo ???????????????
                case "/resetMainPageUrl":
                case "/proxy/resetMainPageUrl":
                    dPath = asyncHttpServerRequest.getHeaders().getMultiMap().getString("path");
                    if (!TextUtils.isEmpty(dPath)) {
                        SharedPreferences sp = HcmobileApp.getApplication().getSharedPreferences("supconit_hcmobile_android_for_platform", Context.MODE_PRIVATE);
                        dPath = "file://" + DataCenter.getInstance().getFile(dPath).getAbsolutePath();
                        sp.edit().putString("launchUrl", dPath).apply();
                        asyncHttpServerResponse.code(200);
                        asyncHttpServerResponse.send(" ok " + path);
                    } else {
                        asyncHttpServerResponse.code(300);
                        asyncHttpServerResponse.send("head ?????? ccc path ??????: " + path);
                    }
                    break;
                case "/getMainPage":
                case "/proxy/getMainPage":
                    SharedPreferences sp = HcmobileApp.getApplication().getSharedPreferences("supconit_hcmobile_android_for_platform", Context.MODE_PRIVATE);
                    String launchUrl = sp.getString("launchUrl", "");
                    asyncHttpServerResponse.code(200);
                    asyncHttpServerResponse.send(launchUrl);
                    asyncHttpServerResponse.end();
                    break;
                // todo ??????app
                case "/restart":
                case "/proxy/restart":
                    Intent i = HcmobileApp.getApplication().getPackageManager().getLaunchIntentForPackage(HcmobileApp.getApplication().getPackageName());
                    try {
                        assert i != null;
                        i.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                        List<Activity> activityList = HcmobileApp.getActivityList();
                        for (Activity activity : activityList) {
                            activity.finish();
                        }
                        HcmobileApp.getApplication().startActivity(i);
                        asyncHttpServerResponse.code(200);
                        asyncHttpServerResponse.send(" ok " + path);
                    } catch (Exception e) {
                        e.printStackTrace();
                        asyncHttpServerResponse.code(300);
                        asyncHttpServerResponse.send("??????????????????: ");
                    }
                    break;


                case "/isTokenVaildity":
                    WorkSpace.post_isTokenVaildity(asyncHttpServerRequest, asyncHttpServerResponse, bodyJson);
                    break;
                case "/access":
                    WorkSpace.post_access(asyncHttpServerRequest, asyncHttpServerResponse, bodyJson);
                    break;
                case "/resetPassWord":
                    WorkSpace.post_resetPassWord(asyncHttpServerRequest, asyncHttpServerResponse, bodyJson);
                    break;


                case "/getMiniList":
                    WorkSpace.post_getMiniList(asyncHttpServerRequest, asyncHttpServerResponse, bodyJson);
                    break;
                case "/mini_delete":
                    WorkSpace.post_mini_delete(asyncHttpServerRequest, asyncHttpServerResponse, bodyJson);
                    break;
                case "/mini_create":
                    WorkSpace.post_mini_create(asyncHttpServerRequest, asyncHttpServerResponse, bodyJson);
                    break;
                case "/mini_createById":
                    WorkSpace.post_mini_createById(asyncHttpServerRequest, asyncHttpServerResponse, bodyJson);
                    break;
                case "/mini_get_info":
                    WorkSpace.post_mini_get_info(asyncHttpServerRequest, asyncHttpServerResponse, bodyJson);
                    break;
                case "/mini_rename":
                    WorkSpace.post_mini_rename(asyncHttpServerRequest, asyncHttpServerResponse, bodyJson);
                    break;
                case "/mini_editIndexPage":
                    WorkSpace.post_mini_editIndexPage(asyncHttpServerRequest, asyncHttpServerResponse, bodyJson);
                    break;
                case "/mini_upload_icon":
                    WorkSpace.post_mini_upload_icon(asyncHttpServerRequest, asyncHttpServerResponse, bodyJson);
                    break;
                case "/mini_upload_icon_base64":
                    WorkSpace.post_mini_upload_icon_base64(asyncHttpServerRequest, asyncHttpServerResponse, bodyJson);
                    break;
                case "/mini_upload_OfflineZip":
                    WorkSpace.post_mini_upload_OfflineZip(asyncHttpServerRequest, asyncHttpServerResponse, bodyJson);
                    break;


                case "/getFolders":
                    WorkSpace.post_getFolders(asyncHttpServerRequest, asyncHttpServerResponse, bodyJson);
                    break;
                case "/createFile":
                    WorkSpace.post_createFile(asyncHttpServerRequest, asyncHttpServerResponse, bodyJson);
                    break;
                case "/uploadFile":
                    WorkSpace.post_uploadFile(asyncHttpServerRequest, asyncHttpServerResponse, bodyJson);
                    break;
                case "/uploadDirZip":
                    WorkSpace.post_uploadDirZip(asyncHttpServerRequest, asyncHttpServerResponse, bodyJson);
                    break;
                case "/getFile":
                    WorkSpace.post_getFile(asyncHttpServerRequest, asyncHttpServerResponse, bodyJson);
                    break;
                case "/searchFile":
                    WorkSpace.post_searchFile(asyncHttpServerRequest, asyncHttpServerResponse, bodyJson);
                    break;
                case "/renameFile":
                    WorkSpace.post_renameFile(asyncHttpServerRequest, asyncHttpServerResponse, bodyJson);
                    break;
                case "/deleteFile":
                    WorkSpace.post_deleteFile(asyncHttpServerRequest, asyncHttpServerResponse, bodyJson);
                    break;
                case "/downloadFileOrZip":
                    WorkSpace.post_downloadFileOrZip(asyncHttpServerRequest, asyncHttpServerResponse, bodyJson);
                    break;


                case "/runJsCode":
                    WorkSpace.post_save_and_runJsCode(asyncHttpServerRequest, asyncHttpServerResponse, bodyJson);
                    break;
                case "/runHtmlPage":
                    WorkSpace.post_save_and_runHtmlPage(asyncHttpServerRequest, asyncHttpServerResponse, bodyJson);
                    break;
                case "/getLog":
                    WorkSpace.post_getLog(asyncHttpServerRequest, asyncHttpServerResponse, bodyJson);
                    break;
                case "/runApp":
                    WorkSpace.post_runApp(asyncHttpServerRequest, asyncHttpServerResponse, bodyJson);
                    break;
                case "/getAppId":
                    WorkSpace.post_getAppId(asyncHttpServerRequest, asyncHttpServerResponse, bodyJson);
                    break;
                case "/getHcServerAddress":
                    WorkSpace.post_getHcServerAddress(asyncHttpServerRequest, asyncHttpServerResponse, bodyJson);
                    break;
                case "/getStorageInfo":
                    WorkSpace.post_getStorageInfo(asyncHttpServerRequest, asyncHttpServerResponse, bodyJson);
                    break;


                default:
                    asyncHttpServerResponse.code(300);
                    asyncHttpServerResponse.send("no such method: " + path);
                    break;
            }

        } catch (Exception e) {
            e.printStackTrace();
        }
    }


    /**
     * ???????????????cordova???????????????viewView
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
