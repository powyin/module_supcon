package com.supconit.hcmobile.plugins.debug.project;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.net.Uri;
import android.text.TextUtils;
import android.util.Base64;
import android.util.Log;
import android.webkit.ValueCallback;
import android.webkit.WebView;

import com.google.gson.Gson;
import com.koushikdutta.async.http.Headers;
import com.koushikdutta.async.http.NameValuePair;
import com.koushikdutta.async.http.body.MultipartFormDataBody;
import com.koushikdutta.async.http.server.AsyncHttpServerRequest;
import com.koushikdutta.async.http.server.AsyncHttpServerResponse;
import com.supconit.hcmobile.HcmobileApp;
import com.supconit.hcmobile.MainActivity;
import com.supconit.hcmobile.MainActivityNoSingle;
import com.supconit.hcmobile.center.dialog.TopProgressDialog;
import com.supconit.hcmobile.model.ConsoleMs;
import com.supconit.hcmobile.net.DownInfo;
import com.supconit.hcmobile.net.HttpManager;
import com.supconit.hcmobile.net.Progress;
import com.supconit.hcmobile.plugins.debug.ServerObserver;
import com.supconit.hcmobile.util.FileUtil;
import com.supconit.hcmobile.util.JsonUtil;
import com.supconit.hcmobile.util.NetUtil;
import com.supconit.hcmobile.util.Util;

import org.apache.tools.ant.Project;
import org.apache.tools.ant.taskdefs.Zip;
import org.apache.tools.ant.types.FileSet;
import org.apache.tools.zip.ZipEntry;
import org.apache.tools.zip.ZipFile;
import org.json.JSONArray;
import org.json.JSONObject;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.UUID;

import io.reactivex.Observer;
import io.reactivex.SingleObserver;
import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.disposables.Disposable;

import static android.content.Context.MODE_PRIVATE;

public class WorkSpace implements Cloneable {
//    private static String[] shareId = new String[]{
//            "debugId1",
//            "debugId2",
//            "debugId3",
//            "debugId4",
//            "debugId5",
//            "debugId6",
//    };

    private static WorkSpace debugSpace;
    private static WorkSpace shareSpace;
    private static HashMap<String, WorkSpace> miniPros;
    private static HashSet<String> keySet;
    private static int le;
    private static int pageIndex = 0;

    private static SharedPreferences structInfo() {
        return HcmobileApp.getApplication().getSharedPreferences("hcmobile_work_space_info", Context.MODE_PRIVATE);
    }

    private static void ensureInit() {
        if (debugSpace == null) {
            String path = FileUtil.getOfficeFilePath(HcmobileApp.getApplication(), "workSpace_debug", "", true).getAbsolutePath();

            if (structInfo().getBoolean("noDebugInit", true)) {
                structInfo().edit().putBoolean("noDebugInit", false).apply();
                copy("<html>\n" +
                                "<head>\n" +
                                "    <script charset=\"UTF-8\" src=\"supconit://hcmobile.js\"></script>\n" +
                                "    <script>\n" +
                                "            //????????????\n" +
                                "            document.addEventListener(\"deviceready\", onDeviceReady, false);\n" +
                                "\n" +
                                "            function onDeviceReady() {\n" +
                                "\n" +
                                "                //????????????????????????????????????\n" +
                                "                //navigator\n" +
                                "\n" +
                                "            }\n" +
                                "    </script>\n" +
                                "</head>\n" +
                                "<body>\n" +
                                "       <h1> ?????????????????????????????????</h1>\n" +
                                "</body>\n" +
                                "</html>\n",
                        new File(Util.pathAppend(path, "example", "example.html")));
                copy("console.log(\"??????????????????????????? ?????????????????????????????????: navigator.xx.function()\")\n" +
                                "navigator.notification.alert(\"???????????????\", function (info) {\n" +
                                "    console.log(\"success\")\n" +
                                "}, \"??????title\", \"???????????????????????????\");",
                        new File(Util.pathAppend(path, "example", "example.js")));
            }

            debugSpace = new WorkSpace(path);
        }

        if (shareSpace == null) {
            String path = FileUtil.getOfficeFilePath(HcmobileApp.getApplication(), "workSpace_share", "", true).getAbsolutePath();
            shareSpace = new WorkSpace(path);
        }

        if (miniPros == null) {
            miniPros = new HashMap<>();
            Set<String> key_mini_pro = structInfo().getStringSet("key_work_space_mini", new HashSet<>());
            for (String key : key_mini_pro) {
                WorkSpace space = new WorkSpace(key);
                miniPros.put(key, space);
            }
        }

        if (keySet == null) {
            keySet = new HashSet<>();
            Map<String, ?> all = structInfo().getAll();
            if (all != null) {
                all = new HashMap<>(all);
                for (Map.Entry<String, ?> entry : all.entrySet()) {
                    String key = entry.getKey();
                    String value = entry.getValue().toString();
                    if (TextUtils.isDigitsOnly(key)) {
                        if (System.currentTimeMillis() - Long.parseLong(key) < 1000 * 60 * 60 * 24 * 10) {
                            keySet.add(value);
                        } else {
                            structInfo().edit().remove(key).apply();
                        }
                    }
                }
            }
        }
    }

    static WorkSpace getSpaceById(String id) {
        if (TextUtils.isEmpty(id)) {
            return null;
        }
        ensureInit();
        switch (id) {
            case "debug":
            case "debugSpace":
                return debugSpace;
            case "share":
            case "shareSpace":
                return shareSpace;
            default:
                return miniPros.get(id);
        }
    }


    //todo  ????????????
    public static void post_isTokenVaildity(AsyncHttpServerRequest asyncHttpServerRequest, AsyncHttpServerResponse asyncHttpServerResponse, String postContent) {
        ensureInit();
        String token = asyncHttpServerRequest.getHeaders().getMultiMap().getString("token");
        JSONObject jsonObject = new JSONObject();
        try {
            if (keySet.contains(token)) {
                jsonObject.put("code", "0");
            } else {
                jsonObject.put("code", "1");
            }
        } catch (Exception ignored) {
        }

        asyncHttpServerResponse.send(jsonObject.toString());
    }


    //todo  ????????????
    public static void post_access(AsyncHttpServerRequest asyncHttpServerRequest, AsyncHttpServerResponse asyncHttpServerResponse, String postContent) {
        ensureInit();

        String passWord = JsonUtil.getJsonString(postContent, "passWord");
        String key = structInfo().getString("password", "123456");

        if (key.equals(passWord)) {
            String uid = UUID.randomUUID().toString();
            structInfo().edit().putString(System.currentTimeMillis() + "", uid).apply();
            keySet.add(uid);
            asyncHttpServerResponse.send(uid);
        } else {
            asyncHttpServerResponse.code(300);
            asyncHttpServerResponse.send("????????????");
        }
    }

    //todo  ????????????
    public static void post_resetPassWord(AsyncHttpServerRequest asyncHttpServerRequest, AsyncHttpServerResponse asyncHttpServerResponse, String postContent) {
        ensureInit();

        // String passWord = JsonUtil.getJsonString(postContent, "passWord");
        String passWord_New = JsonUtil.getJsonString(postContent, "passWord_New");
        // String key = structInfo().getString("password", "123456");

        // if ("123456".equals(key) || key.equals(passWord)) {
        if (TextUtils.isEmpty(passWord_New)) {
            asyncHttpServerResponse.code(300);
            asyncHttpServerResponse.send("??????????????????");
        } else {
            structInfo().edit().putString("password", passWord_New).apply();
            String uid = UUID.randomUUID().toString();
            structInfo().edit().putString(System.currentTimeMillis() + "", uid).apply();
            keySet.add(uid);
            asyncHttpServerResponse.send(uid);
        }
//        } else {
//            asyncHttpServerResponse.code(300);
//            asyncHttpServerResponse.send("????????????");
//
//        }
    }


    public static void resetPassWord(String passWord_New) {
        structInfo().edit().putString("password", passWord_New).apply();
    }


    // todo ??????????????????
    private static boolean noAccess(AsyncHttpServerRequest asyncHttpServerRequest, AsyncHttpServerResponse asyncHttpServerResponse, String postContent) {
        Headers headers = asyncHttpServerRequest.getHeaders();

        String user = headers.get("User-Agent");
        if (TextUtils.isEmpty(user)) {
            user = headers.get("user-agent");
        }

        if (user != null && !user.contains("android")) {
            String token = headers.getMultiMap().getString("token");
            if (TextUtils.isEmpty(token) || !keySet.contains(token)) {
                asyncHttpServerResponse.code(302);
                asyncHttpServerResponse.send("?????????");
                return true;
            }
        }
        return false;
    }

    //todo  ???????????????
    public static void post_getMiniList(AsyncHttpServerRequest asyncHttpServerRequest, AsyncHttpServerResponse asyncHttpServerResponse, String postContent) {
        ensureInit();
        if (noAccess(asyncHttpServerRequest, asyncHttpServerResponse, postContent)) {
            return;
        }

        ArrayList<WorkSpace> arrayList = new ArrayList<>();
        if (miniPros.size() != 0) {
            arrayList.addAll(miniPros.values());
        }

        MiniRoot item = new MiniRoot();
        item.children = arrayList;
        item.name = "?????????" + "??????";
        item.type = 1;
        item.isFile = false;
        String ip = "http://" + NetUtil.getIP() + ":" + ServerObserver.serverPort;
        item.icon = ip + "/icon_file_mark/app_list.png";

        ArrayList<MiniRoot> ret = new ArrayList<>();
        ret.add(item);

        Collections.sort(arrayList, new Comparator<WorkSpace>() {
            @Override
            public int compare(WorkSpace o1, WorkSpace o2) {
                return (int) (o1.createTime - o2.createTime);
            }
        });

        asyncHttpServerResponse.send(new Gson().toJson(ret));
    }

    // todo ???????????????
    //{
    //    "space_id":"id111111111"
    //}
    public static void post_mini_delete(AsyncHttpServerRequest asyncHttpServerRequest, AsyncHttpServerResponse asyncHttpServerResponse, String postContent) {
        ensureInit();
        if (noAccess(asyncHttpServerRequest, asyncHttpServerResponse, postContent)) {
            return;
        }

        String id = JsonUtil.getJsonString(postContent, "space_id");
        WorkSpace space_id = getSpaceById(id);
        if (space_id == null || space_id == debugSpace || space_id == shareSpace) {
            asyncHttpServerResponse.send("{\"code\":0}");
            return;
        }

        assert id != null;
        WorkSpace remove = miniPros.remove(id);
        if (remove != null) {
            HashSet<String> set = new HashSet<String>(miniPros.keySet());
            structInfo().edit().putStringSet("key_work_space_mini", set).apply();
            remove.sharedPreferences.edit().clear().apply();
            FileUtil.deleteFile(new File(id));
            asyncHttpServerResponse.send("{\"code\":0}");
        } else {
            asyncHttpServerResponse.code(300);
            asyncHttpServerResponse.send("?????????????????????");
        }

    }

    // todo ???????????????
    //{
    //    "indexPage":"?????????????????????????????? ??????????????????",
    //    "type":"??????????????? String??????  ??????????????????",
    //    "name":"???????????????"
    //}
    public static void post_mini_create(AsyncHttpServerRequest asyncHttpServerRequest, AsyncHttpServerResponse asyncHttpServerResponse, String postContent) {
        ensureInit();
        if (noAccess(asyncHttpServerRequest, asyncHttpServerResponse, postContent)) {
            return;
        }

        String indexPage = JsonUtil.getJsonString(postContent, "indexPage");
        String type = JsonUtil.getJsonString(postContent, "type");
        String name = JsonUtil.getJsonString(postContent, "name");
        String isFullScreen = JsonUtil.getJsonString(postContent, "isFullScreen");
        if (TextUtils.isEmpty(isFullScreen)) {
            isFullScreen = "0";
        }

        if (TextUtils.isEmpty(indexPage) || TextUtils.isEmpty(type)) {
            asyncHttpServerResponse.code(300);
            asyncHttpServerResponse.send("??????????????????");
            return;
        }

        String id = UUID.randomUUID().toString();
        File mini = FileUtil.getOfficeFilePath(HcmobileApp.getApplication(), "workSpace_mini", "", true);
        mini = new File(mini, id);
        FileUtil.ensureFileExist(mini, true);
        id = mini.getAbsolutePath();


        WorkSpace space = new WorkSpace(id);
        space.sharedPreferences.edit().putString("indexPage", indexPage).putString("type", type).putString("name", name).putString("isFullScreen", isFullScreen).putLong("createTime", System.currentTimeMillis()).apply();
        miniPros.put(id, space);
        space.resetMiniConfig();

        HashSet<String> set = new HashSet<String>(miniPros.keySet());
        SharedPreferences.Editor info = structInfo().edit();
        info.putStringSet("key_work_space_mini", set).apply();


        asyncHttpServerResponse.send(id);
    }

    // todo ???????????????
    //{
    //    "indexPage":"?????????????????????????????? ??????????????????",
    //    "type":"??????????????? String??????  ??????????????????",
    //    "name":"???????????????"
    //}
    public static void post_mini_createById(AsyncHttpServerRequest asyncHttpServerRequest, AsyncHttpServerResponse asyncHttpServerResponse, String postContent) {
        ensureInit();
        if (noAccess(asyncHttpServerRequest, asyncHttpServerResponse, postContent)) {
            return;
        }

        final String id = JsonUtil.getJsonString(postContent, "id");
        final String token = JsonUtil.getJsonString(postContent, "token");
        // final String ip = JsonUtil.getJsonString(postContent, "ip");
        if (TextUtils.isEmpty(id) || TextUtils.isEmpty(token)) {
            asyncHttpServerResponse.code(300);
            asyncHttpServerResponse.send("??????id ?????? token");
            return;
        }

        for (String next : miniPros.keySet()) {
            if (next.endsWith("/" + id)) {
                asyncHttpServerResponse.code(300);
                asyncHttpServerResponse.send("?????????????????????");
                return;
            }
        }


        String appId = Util.getCordovaConfigTag("appID", "value");
        if (!TextUtils.isEmpty(appId) && appId.startsWith("_")) {
            appId = appId.substring("_".length());
        }

        String address = Util.getCordovaConfigTag("hc_mobile_server_address", "value");
        String url = Util.pathAppend(address, "applet/cloudApp/detail");
//        if (!TextUtils.isEmpty(ip)) {
//            url = Util.pathAppend(ip, "applet/detail");
//        }


        Map<String, String> getH = new HashMap<>();
        getH.put("token", token);
        Map<String, String> getP = new HashMap<>();
        getP.put("uniqueCode", id);
        getP.put("appId", appId);
        HttpManager.getHb(url, getH, getP, String.class).subscribe(new SingleObserver<String>() {
            @Override
            public void onSubscribe(Disposable disposable) {
            }

            @Override
            public void onSuccess(String js) {
                String index = JsonUtil.getJsonString(js, "result", "indexUrl");
                String zip = JsonUtil.getJsonString(js, "result", "offlineUrl");
                if (index == null || "".equals(index)) {
                    asyncHttpServerResponse.code(300);
                    asyncHttpServerResponse.send("id ??????");
                    return;
                }
                File mini = new File(FileUtil.getOfficeFilePath(HcmobileApp.getApplication(), "workSpace_mini", "", true), id);
                if (index.startsWith("http")) {
                    FileUtil.ensureFileExist(mini, true);
                    WorkSpace space = new WorkSpace(mini.getAbsolutePath());
                    space.sharedPreferences.edit().
                            putString("originJson", js).
                            putString("indexPage", index).
                            putString("type", "??????").
                            putString("name", JsonUtil.getJsonString(js, "result", "name")).
                            putString("localIconRes", JsonUtil.getJsonString(js, "result", "logoUrl")).
                            putString("onLineId", id).
                            putLong("createTime", System.currentTimeMillis()).
                            apply();
                    miniPros.put(mini.getAbsolutePath(), space);
                    space.resetMiniConfig();

                    HashSet<String> set = new HashSet<String>(miniPros.keySet());
                    SharedPreferences.Editor info = structInfo().edit();
                    info.putStringSet("key_work_space_mini", set).apply();
                    asyncHttpServerResponse.send(mini.getAbsolutePath());
                } else {
                    if (TextUtils.isEmpty(zip)) {
                        asyncHttpServerResponse.code(300);
                        asyncHttpServerResponse.send("???????????????????????????");
                        return;
                    }
                    HttpManager.fileDownLoad(zip, 2).
                            subscribe(new Observer<DownInfo>() {
                                @Override
                                public void onSubscribe(Disposable d) {
                                }

                                @Override
                                public void onNext(DownInfo downInfo) {
                                    switch (downInfo.status) {
                                        case FINISH:
                                            FileUtil.ensureFileExist(mini, true);
                                            unzip(downInfo.localFilePath, mini);
                                            WorkSpace space = new WorkSpace(mini.getAbsolutePath());
                                            space.sharedPreferences.edit().
                                                    putString("originJson", js).
                                                    putString("indexPage", index).
                                                    putString("type", "??????").
                                                    putString("name", JsonUtil.getJsonString(js, "result", "name")).
                                                    putString("onLineId", id).
                                                    putString("localIconRes", JsonUtil.getJsonString(js, "result", "logoUrl")).
                                                    putLong("createTime", System.currentTimeMillis()).
                                                    apply();
                                            space.resetFileDescribe();
                                            space.resetMiniConfig();
                                            miniPros.put(mini.getAbsolutePath(), space);
                                            HashSet<String> set = new HashSet<String>(miniPros.keySet());
                                            SharedPreferences.Editor info = structInfo().edit();
                                            info.putStringSet("key_work_space_mini", set).apply();
                                            asyncHttpServerResponse.send(mini.getAbsolutePath());
                                            break;
                                        case ERROR:
                                            onError(downInfo.exception);
                                            break;
                                    }
                                }

                                @Override
                                public void onError(Throwable e) {
                                    asyncHttpServerResponse.code(300);
                                    asyncHttpServerResponse.send("????????????: " + (e == null ? "null" : e.getMessage()));
                                }

                                @Override
                                public void onComplete() {
                                }
                            });
                }
            }

            @Override
            public void onError(Throwable throwable) {
                asyncHttpServerResponse.code(300);
                asyncHttpServerResponse.send("????????????: " + throwable.getMessage());
            }
        });
    }

    // todo ?????????????????????
    //{
    //    "space_id":"?????????id",
    //}
    public static void post_mini_get_info(AsyncHttpServerRequest asyncHttpServerRequest, AsyncHttpServerResponse asyncHttpServerResponse, String postContent) {
        ensureInit();
        if (noAccess(asyncHttpServerRequest, asyncHttpServerResponse, postContent)) {
            return;
        }

        String space_id = JsonUtil.getJsonString(postContent, "space_id");
        WorkSpace space = getSpaceById(space_id);
        if (space == null) {
            asyncHttpServerResponse.code(300);
            asyncHttpServerResponse.send("?????????????????????");
            return;
        }

        String ret = new Gson().toJson(space);
        asyncHttpServerResponse.send(ret);
    }

    // todo
    //{
    //    "space_id":"?????????id",
    //    "newIndexPage":"???????????????"
    //}
    public static void post_mini_editIndexPage(AsyncHttpServerRequest asyncHttpServerRequest, AsyncHttpServerResponse asyncHttpServerResponse, String postContent) {
        ensureInit();
        if (noAccess(asyncHttpServerRequest, asyncHttpServerResponse, postContent)) {
            return;
        }

        String newName = JsonUtil.getJsonString(postContent, "newIndexPage");
        WorkSpace space = getSpaceById(JsonUtil.getJsonString(postContent, "space_id"));
        if (TextUtils.isEmpty(newName)) {
            asyncHttpServerResponse.code(300);
            asyncHttpServerResponse.send("???????????????");
            return;
        }
        if (space == null) {
            asyncHttpServerResponse.code(300);
            asyncHttpServerResponse.send("?????????????????????");
            return;
        }


        space.sharedPreferences.edit().putString("indexPage", newName).apply();
        space.resetMiniConfig();

        asyncHttpServerResponse.send(new Gson().toJson(space));
    }

    // todo
    //{
    //    "space_id":"?????????id",
    //    "newName":"???????????????"
    //}
    public static void post_mini_rename(AsyncHttpServerRequest asyncHttpServerRequest, AsyncHttpServerResponse asyncHttpServerResponse, String postContent) {
        ensureInit();
        if (noAccess(asyncHttpServerRequest, asyncHttpServerResponse, postContent)) {
            return;
        }

        String newName = JsonUtil.getJsonString(postContent, "newName");
        String isFullScreen = JsonUtil.getJsonString(postContent, "isFullScreen");
        WorkSpace space = getSpaceById(JsonUtil.getJsonString(postContent, "space_id"));

        if (space == null) {
            asyncHttpServerResponse.code(300);
            asyncHttpServerResponse.send("???????????????????????????");
            return;
        }
        if (TextUtils.isEmpty(newName)) {
            asyncHttpServerResponse.code(300);
            asyncHttpServerResponse.send("??????????????????");
            return;
        }

        space.sharedPreferences.edit().putString("name", newName).apply();
        if (!TextUtils.isEmpty(isFullScreen)) {
            space.sharedPreferences.edit().putString("isFullScreen", isFullScreen).apply();
        }
        space.resetMiniConfig();
        asyncHttpServerResponse.send(new Gson().toJson(space));
    }

    // todo ??????????????? ??????icon
    //???
    //     "space_id":"id111111111"
    //???
    public static void post_mini_upload_icon(AsyncHttpServerRequest asyncHttpServerRequest, AsyncHttpServerResponse asyncHttpServerResponse, String postContent) {
        ensureInit();
        if (noAccess(asyncHttpServerRequest, asyncHttpServerResponse, postContent)) {
            return;
        }

        String space_id = asyncHttpServerRequest.getHeaders().getMultiMap().getString("space_id");
        if (!TextUtils.isEmpty(space_id)) {
            space_id = Uri.decode(space_id);
        }
        WorkSpace space = getSpaceById(space_id);
        if (space == null) {
            asyncHttpServerResponse.code(300);
            asyncHttpServerResponse.send("?????????????????????");
            return;
        }

        try {
            MultipartFormDataBody multipartFormDataBody = asyncHttpServerRequest.getBody();
            multipartFormDataBody.onFileDownLoadBack = new MultipartFormDataBody.OnFileDownLoadBack() {
                @Override
                public void onFile(File file) {
                    if (file == null || !file.exists() || file.length() == 0) {
                        asyncHttpServerResponse.code(300);
                        asyncHttpServerResponse.send("????????????");
                        asyncHttpServerResponse.end();
                    } else {
                        String iconFile = space.rootDir.endsWith("/") ? space.rootDir.substring(0, space.rootDir.length() - 1) : space.rootDir;
                        iconFile += ((pageIndex++) % 100) + "_icon.jpg";
                        FileUtil.copy(file, new File(iconFile));
                        FileUtil.deleteFile(file);

                        space.sharedPreferences.edit().putString("localIconRes", iconFile).apply();
                        space.resetMiniConfig();

                        asyncHttpServerResponse.code(200);
                        asyncHttpServerResponse.send(new Gson().toJson(space));
                        asyncHttpServerResponse.end();
                    }
                }
            };
        } catch (Exception e) {
            asyncHttpServerResponse.code(300);
            asyncHttpServerResponse.send("????????????:" + e.getMessage());
        }
    }

    // todo ??????????????? ??????icon
    //???
    //     "space_id":"id111111111"
    //???
    public static void post_mini_upload_icon_base64(AsyncHttpServerRequest asyncHttpServerRequest, AsyncHttpServerResponse asyncHttpServerResponse, String postContent) {
        ensureInit();
        if (noAccess(asyncHttpServerRequest, asyncHttpServerResponse, postContent)) {
            return;
        }

        String space_id = asyncHttpServerRequest.getHeaders().getMultiMap().getString("space_id");
        if (!TextUtils.isEmpty(space_id)) {
            space_id = Uri.decode(space_id);
        }
        WorkSpace space = getSpaceById(space_id);
        if (space == null) {
            asyncHttpServerResponse.code(300);
            asyncHttpServerResponse.send("?????????????????????");
            return;
        }

        try {
            byte[] decode = android.util.Base64.decode(postContent, Base64.DEFAULT);
            String iconFile = space.rootDir.endsWith("/") ? space.rootDir.substring(0, space.rootDir.length() - 1) : space.rootDir;
            iconFile += ((pageIndex++) % 100) + "_icon.jpg";
            File target = new File(iconFile);
            FileUtil.ensureFileExist(target, false);
            BufferedInputStream bis = new BufferedInputStream(new ByteArrayInputStream(decode));
            BufferedOutputStream bos = new BufferedOutputStream(new FileOutputStream(target));
            int len = 0;
            byte[] b = new byte[1024 * 64];
            while ((len = bis.read(b)) != -1) {
                bos.write(b, 0, len);
            }
            bos.flush();
            bis.close();
            bos.close();

            space.sharedPreferences.edit().putString("localIconRes", iconFile).apply();
            space.resetMiniConfig();

            asyncHttpServerResponse.code(200);
            asyncHttpServerResponse.send(new Gson().toJson(space));
            asyncHttpServerResponse.end();
        } catch (Exception e) {
            e.printStackTrace();
            asyncHttpServerResponse.code(300);
            asyncHttpServerResponse.send("????????????:" + e.getMessage());
        }

    }


    // todo ??????????????? ??????????????????
    //???
    //     "space_id":"id111111111"
    //???
    public static void post_mini_upload_OfflineZip(AsyncHttpServerRequest asyncHttpServerRequest, AsyncHttpServerResponse asyncHttpServerResponse, String postContent) {
        ensureInit();
        if (noAccess(asyncHttpServerRequest, asyncHttpServerResponse, postContent)) {
            return;
        }

        String space_id = asyncHttpServerRequest.getHeaders().getMultiMap().getString("space_id");
        if (!TextUtils.isEmpty(space_id)) {
            space_id = Uri.decode(space_id);
        }
        WorkSpace space = getSpaceById(space_id);
        if (space == null) {
            asyncHttpServerResponse.code(300);
            asyncHttpServerResponse.send("?????????????????????");
            return;
        }

        try {
            MultipartFormDataBody multipartFormDataBody = asyncHttpServerRequest.getBody();
            multipartFormDataBody.onFileDownLoadBack = new MultipartFormDataBody.OnFileDownLoadBack() {
                @Override
                public void onFile(File file) {
                    if (file == null || !file.exists() || file.length() == 0) {
                        asyncHttpServerResponse.code(300);
                        asyncHttpServerResponse.send("????????????");
                        asyncHttpServerResponse.end();
                    } else {
                        unzip(file, new File(space.rootDir));
                        FileUtil.deleteFile(file);
                        space.resetFileDescribe();
                        space.resetMiniConfig();
                        asyncHttpServerResponse.send("ok");
                    }
                }
            };
        } catch (Exception e) {
            e.printStackTrace();
            asyncHttpServerResponse.code(300);
            asyncHttpServerResponse.send("????????????:" + e.getMessage());
        }
    }


    // todo ???????????? ????????????
    public static void post_getFolders(AsyncHttpServerRequest asyncHttpServerRequest, AsyncHttpServerResponse asyncHttpServerResponse, String postContent) {
        ensureInit();
        if (noAccess(asyncHttpServerRequest, asyncHttpServerResponse, postContent)) {
            return;
        }

        String id = JsonUtil.getJsonString(postContent, "spaceId");
        if (TextUtils.isEmpty(id)) {
            asyncHttpServerResponse.code(300);
            asyncHttpServerResponse.send("?????????????????????");
            return;
        }
        WorkSpace space = getSpaceById(id);

        if (space == null) {
            asyncHttpServerResponse.code(300);
            asyncHttpServerResponse.send("?????????????????????");
            return;
        }

        List<FileItem> ret = space.mRoot != null ? space.mRoot.children : null;
        if (ret == null) {
            ret = new ArrayList<>();
        }

        if (space == debugSpace) {
            String path = Util.pathAppend(debugSpace.rootDir, "example");
            FileItem fileItem = debugSpace.fileDesHashMap.get(path);
            if (fileItem != null) {
                if (debugSpace.children.remove(fileItem)) {
                    debugSpace.children.add(0, fileItem);
                }
            }

            FileItem item = new FileItem();
            item.children = ret;
            item.folderId = "/";
            item.name = "??????";
            item.type = 1;
            item.isFile = false;
            String ip = "http://" + NetUtil.getIP() + ":" + ServerObserver.serverPort;
            item.icon = ip + "/icon_file_mark/folder.png";
            ret = new ArrayList<>();
            ret.add(item);
        }

        asyncHttpServerResponse.send(new Gson().toJson(ret));
    }

    // todo ????????????
    // {
    //    "spaceId":"?????????id",
    //    "parentId":"?????????id",
    //    "folderName":"?????????????????????????????????",
    //    "isFile":"???????????????  ???????????????????????????????????????fileContent ??????????????? ??????????????????",
    //    "fileContent":"????????????"
    //}
    public static void post_createFile(AsyncHttpServerRequest asyncHttpServerRequest, AsyncHttpServerResponse asyncHttpServerResponse, String postContent) {
        ensureInit();
        if (noAccess(asyncHttpServerRequest, asyncHttpServerResponse, postContent)) {
            return;
        }

        String spaceId = JsonUtil.getJsonString(postContent, "spaceId");
        String parentId = JsonUtil.getJsonString(postContent, "parentId");
        String folderName = JsonUtil.getJsonString(postContent, "folderName");

        WorkSpace spaceById = getSpaceById(spaceId);
        if (spaceById == null) {
            asyncHttpServerResponse.code(300);
            asyncHttpServerResponse.send("?????????????????????");
            return;
        }
        if (TextUtils.isEmpty(folderName)) {
            asyncHttpServerResponse.code(300);
            asyncHttpServerResponse.send("?????????????????????");
            return;
        }
        if (TextUtils.isEmpty(parentId)) {
            asyncHttpServerResponse.code(300);
            asyncHttpServerResponse.send("???????????????????????????");
            return;
        }

        if ("/".equals(parentId)) {
            parentId = spaceById.rootDir;
        }
        String targetFile = Util.pathAppend(parentId, folderName);
        File tar = new File(targetFile);
        if (tar.exists()) {
            String tip;
            if (tar.isFile()) {
                tip = "??????:" + tar.getName() + "????????????";
            } else {
                tip = "?????????:" + tar.getName() + "????????????";
            }
            asyncHttpServerResponse.code(300);
            asyncHttpServerResponse.send(tip);
            return;
        }

        if ("true".equals(JsonUtil.getJsonString(postContent, "isFile"))) {
            String json = JsonUtil.getJsonString(postContent, "fileContent");

            FileUtil.ensureFileExist(tar, false);
            if (!TextUtils.isEmpty(json)) {
                copy(json, tar);
            }
        } else {
            FileUtil.ensureFileExist(new File(targetFile), true);
        }
        spaceById.resetFileDescribe();

        FileItem fileItem = spaceById.fileDesHashMap.get(targetFile);
        if (fileItem != null) {
            asyncHttpServerResponse.send(new Gson().toJson(fileItem));
        } else {
            String ip = "http://" + NetUtil.getIP() + ":" + ServerObserver.serverPort;
            asyncHttpServerResponse.send(new Gson().toJson(spaceById.ensureFileDescribe(new File(targetFile), ip, spaceId)));
        }

    }


    // todo ????????????
    //???
    //    "spaceId":"?????????id",
    //    "parentId":"?????????id ?????????????????? ?????????/",
    //    "fileName":"????????????",
    //???
    public static void post_uploadFile(AsyncHttpServerRequest asyncHttpServerRequest, AsyncHttpServerResponse asyncHttpServerResponse, String postContent) {
        ensureInit();
        if (noAccess(asyncHttpServerRequest, asyncHttpServerResponse, postContent)) {
            return;
        }


        String spaceId = asyncHttpServerRequest.getHeaders().getMultiMap().getString("spaceid");
        if (!TextUtils.isEmpty(spaceId)) {
            spaceId = Uri.decode(spaceId);
        }
        String parentId = asyncHttpServerRequest.getHeaders().getMultiMap().getString("parentid");
        if (!TextUtils.isEmpty(parentId)) {
            parentId = Uri.decode(parentId);
        }
        String fileName = asyncHttpServerRequest.getHeaders().getMultiMap().getString("filename");
        if (!TextUtils.isEmpty(fileName)) {
            fileName = Uri.decode(fileName);
        }


        Headers headers = asyncHttpServerRequest.getHeaders();
        String length = headers.get("content-length");
        try {
            int size = Integer.parseInt(length);
            if (size > 10 * 1024 * 1024) {
                asyncHttpServerResponse.code(300);
                asyncHttpServerResponse.send("????????????, ????????????10mb");
                asyncHttpServerResponse.end();
                asyncHttpServerRequest.close();
                return;
            }
        } catch (Exception e) {
            e.printStackTrace();
        }

        WorkSpace spaceById = getSpaceById(spaceId);
        if (spaceById == null) {
            asyncHttpServerResponse.code(300);
            asyncHttpServerResponse.send("?????????????????????");
            return;
        }
        if (TextUtils.isEmpty(parentId)) {
            asyncHttpServerResponse.code(300);
            asyncHttpServerResponse.send("???????????????????????????");
            return;
        }
        if (TextUtils.isEmpty(fileName)) {
            asyncHttpServerResponse.code(300);
            asyncHttpServerResponse.send("??????????????????");
            return;
        }

        if ("/".equals(parentId)) {
            parentId = spaceById.rootDir;
        }
        String targetFile = Util.pathAppend(parentId, fileName);

        final String space = spaceId;
        try {
            MultipartFormDataBody multipartFormDataBody = asyncHttpServerRequest.getBody();
            multipartFormDataBody.onFileDownLoadBack = new MultipartFormDataBody.OnFileDownLoadBack() {
                @Override
                public void onFile(File file) {
                    if (file == null || !file.exists() || file.length() == 0) {
                        FileUtil.ensureFileExist(new File(targetFile), true);
                        spaceById.resetFileDescribe();
                        asyncHttpServerResponse.send("??????????????? ????????????");
                        asyncHttpServerResponse.end();
                    } else {
                        FileUtil.copy(file, new File(targetFile));
                        FileUtil.deleteFile(file);
                        spaceById.resetFileDescribe();

                        FileItem fileItem = spaceById.fileDesHashMap.get(targetFile);
                        if (fileItem != null) {
                            asyncHttpServerResponse.send(new Gson().toJson(fileItem));
                        } else {
                            String ip = "http://" + NetUtil.getIP() + ":" + ServerObserver.serverPort;
                            asyncHttpServerResponse.send(new Gson().toJson(spaceById.ensureFileDescribe(new File(targetFile), ip, space)));
                        }
                    }
                }
            };
        } catch (Exception e) {
            asyncHttpServerResponse.code(300);
            asyncHttpServerResponse.send("????????????:" + e.getMessage());
        }
    }

    // todo ????????????
    //???
    //    "spaceId":"?????????id",
    //    "parentId":"?????????id ?????????????????? ?????????/",
    //    "fileName":"????????????",
    //???
    public static void post_uploadDirZip(AsyncHttpServerRequest asyncHttpServerRequest, AsyncHttpServerResponse asyncHttpServerResponse, String postContent) {
        ensureInit();
        if (noAccess(asyncHttpServerRequest, asyncHttpServerResponse, postContent)) {
            return;
        }

        String spaceId = asyncHttpServerRequest.getHeaders().getMultiMap().getString("spaceid");
        if (!TextUtils.isEmpty(spaceId)) {
            spaceId = Uri.decode(spaceId);
        }
        String parentId = asyncHttpServerRequest.getHeaders().getMultiMap().getString("parentid");
        if (!TextUtils.isEmpty(parentId)) {
            parentId = Uri.decode(parentId);
        }


        WorkSpace spaceById = getSpaceById(spaceId);
        if (spaceById == null) {
            asyncHttpServerResponse.code(300);
            asyncHttpServerResponse.send("?????????????????????");
            return;
        }
        if (TextUtils.isEmpty(parentId)) {
            asyncHttpServerResponse.code(300);
            asyncHttpServerResponse.send("???????????????????????????");
            return;
        }
        if ("/".equals(parentId)) {
            parentId = spaceById.rootDir;
        }

        final String targetFile = parentId;
        final String space = spaceId;
        try {
            MultipartFormDataBody multipartFormDataBody = asyncHttpServerRequest.getBody();
            multipartFormDataBody.onFileDownLoadBack = new MultipartFormDataBody.OnFileDownLoadBack() {
                @Override
                public void onFile(File file) {
                    if (file == null || !file.exists() || file.length() == 0) {
                        FileUtil.ensureFileExist(new File(targetFile), true);
                        spaceById.resetFileDescribe();
                        asyncHttpServerResponse.send("??????????????? ????????????");
                        asyncHttpServerResponse.end();
                    } else {
                        unzip(file, new File(targetFile));
                        FileUtil.deleteFile(file);
                        spaceById.resetFileDescribe();

                        FileItem fileItem = spaceById.fileDesHashMap.get(targetFile);
                        if (fileItem != null) {
                            asyncHttpServerResponse.send(new Gson().toJson(fileItem));
                        } else {
                            String ip = "http://" + NetUtil.getIP() + ":" + ServerObserver.serverPort;
                            asyncHttpServerResponse.send(new Gson().toJson(spaceById.ensureFileDescribe(new File(targetFile), ip, space)));
                        }
                    }
                }
            };
        } catch (Exception e) {
            asyncHttpServerResponse.code(300);
            asyncHttpServerResponse.send("????????????:" + e.getMessage());
        }

    }


    // todo ????????????
    // {
    //    "spaceId":"?????????id",
    //    "folderId":"??????id"
    //}
    public static void post_getFile(AsyncHttpServerRequest asyncHttpServerRequest, AsyncHttpServerResponse asyncHttpServerResponse, String postContent) {
        ensureInit();
        if (noAccess(asyncHttpServerRequest, asyncHttpServerResponse, postContent)) {
            return;
        }

        String spaceId = JsonUtil.getJsonString(postContent, "spaceId");
        String folderId = JsonUtil.getJsonString(postContent, "folderId");

        WorkSpace spaceById = getSpaceById(spaceId);
        if (spaceById == null || TextUtils.isEmpty(folderId)) {
            asyncHttpServerResponse.code(300);
            asyncHttpServerResponse.send("???????????????????????????");
            return;
        }

        File file = new File(folderId);

        if (!file.exists()) {
            asyncHttpServerResponse.code(300);
            asyncHttpServerResponse.send("???????????????");
            return;
        }
        if (file.isDirectory()) {
            asyncHttpServerResponse.code(300);
            asyncHttpServerResponse.send("???????????????");
            return;
        }

        asyncHttpServerResponse.sendFile(file);
    }

    //todo ??????
    //{
    //    "spaceId":"?????????id",
    //    "folderId":"??????????????????id  ?????????????????? ?????????/",
    //    "searchWord":"???????????????"
    //}
    public static void post_searchFile(AsyncHttpServerRequest asyncHttpServerRequest, AsyncHttpServerResponse asyncHttpServerResponse, String postContent) {
        ensureInit();
        if (noAccess(asyncHttpServerRequest, asyncHttpServerResponse, postContent)) {
            return;
        }

        String spaceId = JsonUtil.getJsonString(postContent, "spaceId");
        String folderId = JsonUtil.getJsonString(postContent, "folderId");
        String searchWord = JsonUtil.getJsonString(postContent, "searchWord");

        WorkSpace spaceById = getSpaceById(spaceId);
        if (spaceById == null || TextUtils.isEmpty(folderId)) {
            asyncHttpServerResponse.code(300);
            asyncHttpServerResponse.send("??????????????????");
            return;
        }


        FileItem fileItem = spaceById.fileDesHashMap.get(folderId);
        if (fileItem == null) {
            asyncHttpServerResponse.code(300);
            asyncHttpServerResponse.send("?????????????????????");
            return;
        }

        ArrayList<FileItem> ret = new ArrayList<>();
        searchFileItem(fileItem, ret, searchWord);
        asyncHttpServerResponse.send(new Gson().toJson(ret));
    }


    // todo ??????????????????????????????
    //{
    //    "spaceId":"?????????id",
    //    "folderId":"??????id",
    //    "newName":"??????????????????",
    //}
    public static void post_renameFile(AsyncHttpServerRequest asyncHttpServerRequest, AsyncHttpServerResponse asyncHttpServerResponse, String postContent) {
        ensureInit();
        if (noAccess(asyncHttpServerRequest, asyncHttpServerResponse, postContent)) {
            return;
        }

        String spaceId = JsonUtil.getJsonString(postContent, "spaceId");
        String folderId = JsonUtil.getJsonString(postContent, "folderId");
        String newName = JsonUtil.getJsonString(postContent, "newName");
        WorkSpace spaceById = getSpaceById(spaceId);
        if (spaceById == null || TextUtils.isEmpty(folderId)) {
            asyncHttpServerResponse.code(300);
            asyncHttpServerResponse.send("??????????????????");
            return;
        }


        File tar = new File(folderId);
        if (!tar.exists()) {
            asyncHttpServerResponse.code(300);
            asyncHttpServerResponse.send("???????????????");
            return;
        }

        String newN = tar.getParent();
        newN = Util.pathAppend(newN, newName);
        try {
            if (new File(newN).exists()) {
                asyncHttpServerResponse.code(300);
                asyncHttpServerResponse.send("??????:" + newName + "????????????");
                return;
            }
            if (!tar.renameTo(new File(newN))) {
                String tip = "??????????????????";
                if (new File(newN).isFile()) {
                    tip = "??????:" + newName + "????????????";
                } else {
                    tip = "?????????:" + newName + "????????????";
                }
                asyncHttpServerResponse.code(300);
                asyncHttpServerResponse.send(tip);
                return;
            }
        } catch (Exception e) {
            e.printStackTrace();
            asyncHttpServerResponse.code(300);
            asyncHttpServerResponse.send("????????????" + e.getMessage());
            return;
        }
        spaceById.resetFileDescribe();


        FileItem fileItem = spaceById.fileDesHashMap.get(newN);
        if (fileItem != null) {
            asyncHttpServerResponse.send(new Gson().toJson(fileItem));
        } else {
            String ip = "http://" + NetUtil.getIP() + ":" + ServerObserver.serverPort;
            asyncHttpServerResponse.send(new Gson().toJson(spaceById.ensureFileDescribe(new File(newN), ip, spaceId)));
        }

    }

    // todo ???????????????????????????
    // {
    //    "spaceId":"?????????id",
    //    "folderId":"??????id",
    //}
    public static void post_deleteFile(AsyncHttpServerRequest asyncHttpServerRequest, AsyncHttpServerResponse asyncHttpServerResponse, String postContent) {
        ensureInit();
        if (noAccess(asyncHttpServerRequest, asyncHttpServerResponse, postContent)) {
            return;
        }

        String spaceId = JsonUtil.getJsonString(postContent, "spaceId");
        String folderId = JsonUtil.getJsonString(postContent, "folderId");
        WorkSpace spaceById = getSpaceById(spaceId);
        if (spaceById == null || TextUtils.isEmpty(folderId)) {
            asyncHttpServerResponse.code(300);
            asyncHttpServerResponse.send("??????????????????" + spaceId + "   " + folderId);
            return;
        }

        File tar = new File(folderId);
        if (!tar.exists()) {
            asyncHttpServerResponse.code(300);
            asyncHttpServerResponse.send("???????????????");
            return;
        }
        FileUtil.deleteFile(tar);

        spaceById.resetFileDescribe();
        List<FileItem> ret = spaceById.mRoot != null ? spaceById.mRoot.children : null;
        if (ret == null) {
            ret = new ArrayList<>();
        }
        asyncHttpServerResponse.send(new Gson().toJson(ret));
    }

    // todo ???????????????????????????
    // {
    //    "spaceId":"?????????id",
    //    "folderId":"??????id",
    //}
    public static void post_downloadFileOrZip(AsyncHttpServerRequest asyncHttpServerRequest, AsyncHttpServerResponse asyncHttpServerResponse, String postContent) {
        ensureInit();
//        if (noAccess(asyncHttpServerRequest, asyncHttpServerResponse, postContent)) {
//            return;
//        }

        String spaceId = JsonUtil.getJsonString(postContent, "spaceId");
        String folderId = JsonUtil.getJsonString(postContent, "folderId");

        if (TextUtils.isEmpty(spaceId)) {
            spaceId = asyncHttpServerRequest.getQuery().getString("spaceId");
            if (!TextUtils.isEmpty(spaceId)) {
                spaceId = Uri.decode(spaceId);
            }
        }

        if (TextUtils.isEmpty(folderId)) {
            folderId = asyncHttpServerRequest.getQuery().getString("folderId");
            if (!TextUtils.isEmpty(folderId)) {
                folderId = Uri.decode(folderId);
            }
        }

        if ("/".equals(folderId)) {
            WorkSpace spaceById = WorkSpace.getSpaceById(spaceId);
            if (spaceById != null) {
                folderId = spaceById.rootDir;
            }
        }
        if ("/".equals(folderId) || TextUtils.isEmpty(folderId)) {
            asyncHttpServerResponse.code(300);
            asyncHttpServerResponse.send("????????????????????????");
            asyncHttpServerResponse.end();
            return;
        }

        File file = new File(folderId);
        if (!file.exists()) {
            asyncHttpServerResponse.code(300);
            asyncHttpServerResponse.send("????????????");
            asyncHttpServerResponse.end();
        }

        String filename = file.isFile() ? file.getName() : file.getName() + ".zip";
        asyncHttpServerResponse.getHeaders().add("Content-Disposition", "attachment;filename=" + filename);
        asyncHttpServerResponse.getHeaders().add("Content-Transfer-Encoding", "binary");

        if (file.isFile()) {
            asyncHttpServerResponse.sendFile(file);
        } else {
            String cacheZip = debugSpace.rootDir.endsWith("/") ? debugSpace.rootDir.substring(0, debugSpace.rootDir.length() - 1) : debugSpace.rootDir;
            cacheZip += "_cache" + ((index++) % 5) + ".zip";
            try {
                zip(new File(cacheZip), file);
            } catch (Exception e) {
                e.printStackTrace();
                asyncHttpServerResponse.code(300);
                asyncHttpServerResponse.send("????????????");
                asyncHttpServerResponse.end();
            }
            asyncHttpServerResponse.sendFile(new File(cacheZip));
        }
    }

    private static int index;
    private static int cacheJsindex;

    // todo ???js
    //{
    //    "spaceId":"?????????id",
    //    "folderId":"??????id",
    //    "change":{
    //            "???????????????1id":"??????1id?????????",
    //            "???????????????12id":"??????2?????????"
    //    }
    //}
    public static void post_save_and_runJsCode(AsyncHttpServerRequest asyncHttpServerRequest, AsyncHttpServerResponse asyncHttpServerResponse, String postContent) {
        ensureInit();
        if (noAccess(asyncHttpServerRequest, asyncHttpServerResponse, postContent)) {
            return;
        }

        WorkSpace spaceId = getSpaceById(JsonUtil.getJsonString(postContent, "spaceId"));
        String folderId = JsonUtil.getJsonString(postContent, "folderId");
        String change = JsonUtil.getJsonString(postContent, "change");

        if (spaceId == null) {
            asyncHttpServerResponse.code(300);
            asyncHttpServerResponse.send("??????????????????");
            return;
        }

        if (!TextUtils.isEmpty(change)) {
            try {
                JSONObject array = new JSONObject(change);
                Iterator<String> keys = array.keys();
                while (keys.hasNext()) {
                    String next = keys.next();
                    copy(array.getString(next), new File(next));
                    System.out.print("::::" + array.getString(next));
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
            spaceId.resetFileDescribe();
        }

        if (!TextUtils.isEmpty(folderId)) {
            MainActivity current = getCurrentRunJsEvn();
            if (current == null) {
                asyncHttpServerResponse.code(300);
                asyncHttpServerResponse.send("?????????????????????activity");
                return;
            }

            HcmobileApp.getHandle().post(new Runnable() {
                @Override
                public void run() {
                    String address = folderId;
                    String url = current.mSystemWebView.getUrl();


                    String js;
                    if (url.startsWith("https")) {
                        js = FileUtil.readLocalFileAsString(folderId);
                        ((WebView) current.mSystemWebView).evaluateJavascript(js, new ValueCallback<String>() {
                            @Override
                            public void onReceiveValue(String value) {
                                //
                            }
                        });
                    } else {

                        String cacheJs = debugSpace.rootDir.endsWith("/") ? debugSpace.rootDir.substring(0, debugSpace.rootDir.length() - 1) : debugSpace.rootDir;
                        cacheJs = new File(cacheJs).getParent();
                        cacheJs = Util.pathAppend(cacheJs, "cachejs_" + (cacheJsindex++) % 150 + ".js");
                        FileUtil.copy(new File(folderId), new File(cacheJs));
                        address = cacheJs;


                        if (url.startsWith("file")) {
                            if (address.startsWith("/")) {
                                address = "file://" + address;
                            } else {
                                address = "file:///" + address;
                            }

                        } else if (url.startsWith("http:")) {
                            String ip = "http://" + NetUtil.getIP() + ":" + ServerObserver.serverPort;
                            address = Util.pathAppend(ip, "get_file_local", folderId);
                        }
                        js = "var src = document.createElement('script');\n" +
                                "    src.type = 'text/javascript';\n" +
                                "    src.async = true;\n" +
                                "    src.charset = 'utf-8';\n" +
                                "    src.src = '" + address + "';\n" +
                                "    document.head.appendChild(src);";

                        current.loadUrl("javascript: " + js);

                    }


                }
            });
        }

        JSONObject jsonObject = new JSONObject();
        try {
            jsonObject.put("code", 0);
            if (TextUtils.isEmpty(change)) {
                jsonObject.put("hasFileChange", false);
            } else {
                jsonObject.put("hasFileChange", true);
            }
            if (TextUtils.isEmpty(folderId)) {
                jsonObject.put("hasRunJs", false);
            } else {
                jsonObject.put("hasRunJs", true);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }

        asyncHttpServerResponse.send(jsonObject);
    }

    // todo ???html??????
    //{
    //    "spaceId":"?????????id",
    //    "folderId":"??????id",
    //    "change":{
    //            "???????????????1id":"??????1id?????????",
    //            "???????????????12id":"??????2?????????"
    //    }
    //}
    public static void post_save_and_runHtmlPage(AsyncHttpServerRequest asyncHttpServerRequest, AsyncHttpServerResponse asyncHttpServerResponse, String postContent) {
        ensureInit();
        if (noAccess(asyncHttpServerRequest, asyncHttpServerResponse, postContent)) {
            return;
        }

        WorkSpace spaceId = getSpaceById(JsonUtil.getJsonString(postContent, "spaceId"));
        String folderId = JsonUtil.getJsonString(postContent, "folderId");
        String change = JsonUtil.getJsonString(postContent, "change");

        if (spaceId == null) {
            asyncHttpServerResponse.code(300);
            asyncHttpServerResponse.send("?????????????????????");
            return;
        }

//        if (TextUtils.isEmpty(change) && TextUtils.isEmpty(folderId)) {
//            asyncHttpServerResponse.code(300);
//            asyncHttpServerResponse.send("???????????????");
//            return;
//        }

        if (!TextUtils.isEmpty(change)) {
            try {
                JSONObject array = new JSONObject(change);
                Iterator<String> keys = array.keys();
                while (keys.hasNext()) {
                    String next = keys.next();
                    copy(array.getString(next), new File(next));
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
            spaceId.resetFileDescribe();
        }

        if (!TextUtils.isEmpty(folderId)) {
            MainActivity current = getCurrentShowMainActivity();
            if (current == null) {
                asyncHttpServerResponse.code(300);
                asyncHttpServerResponse.send("?????????????????? ?????????app??????");
                return;
            }


            HcmobileApp.getHandle().post(new Runnable() {
                @Override
                public void run() {
                    String indexPage = null;
                    if ("/".equals(folderId)) {
                        if (spaceId.indexPage.startsWith("http")) {
                            indexPage = spaceId.indexPage;
//                            intent.putExtra("launchUrl", spaceId.indexPage);
//                            current.loadUrl(spaceId.indexPage);
                        } else {
                            String root = spaceId.rootDir;
                            if (root.startsWith("/")) {
                                root = "file://" + root;
                            } else {
                                root = "file:///" + root;
                            }
                            indexPage = Util.pathAppend(root, spaceId.indexPage);
//                            intent.putExtra("launchUrl", Util.pathAppend(root, spaceId.indexPage));
//                            current.loadUrl(Util.pathAppend(root, spaceId.indexPage));
                        }
                    } else {
                        String root = folderId;
                        if (root.startsWith("/")) {
                            root = "file://" + root;
                        } else {
                            root = "file:///" + root;
                        }
                        indexPage = root;
//                        intent.putExtra("launchUrl", root);
//                        current.loadUrl(root);
                    }


                    try {
                        JSONObject jsonObject = new JSONObject();
                        JSONObject result = new JSONObject();
                        String index = spaceId.spaceId;
                        result.put("id", index);
                        result.put("uniqueCode", index);
                        result.put("logoUrl", spaceId.localIconRes);
                        result.put("name", spaceId.name);
                        result.put("isFullScreen", spaceId.isFullScreen);

                        jsonObject.put("lauchUrl", indexPage);
                        jsonObject.put("result", result);


                        SharedPreferences sharedPreferences = HcmobileApp.getApplication().getSharedPreferences("hc_mobile_keep_mini_list", Context.MODE_PRIVATE);
                        sharedPreferences.edit().putString(index, jsonObject.toString()).apply();


                        // ??????????????????
                        if (spaceId == debugSpace) {
                            Intent intent = new Intent(current, MainActivityNoSingle.class);
                            intent.putExtra("launchUrl", indexPage);
                            current.startActivity(intent);
                        } else {
                            // ???????????????
                            Intent intent = new Intent(current, Class.forName("com.supconit.hcmobile.plugins.local.MiniEnterActivity"));
                            intent.putExtra("uniqueCode", index);
                            intent.putExtra("miniId", index);
                            current.startActivity(intent);
                        }

                        Log.e("powyin", "mini task");
                    } catch (Exception e) {
                        e.printStackTrace();
                        Intent intent = new Intent(current, MainActivityNoSingle.class);
                        intent.putExtra("launchUrl", indexPage);
                        current.startActivity(intent);
                        Log.e("powyin", "skip mini task");
                    }
                }
            });
        }

        JSONObject jsonObject = new JSONObject();
        try {
            jsonObject.put("code", 0);
            if (TextUtils.isEmpty(change)) {
                jsonObject.put("hasFileChange", false);
            } else {
                jsonObject.put("hasFileChange", true);
            }
            if (TextUtils.isEmpty(folderId)) {
                jsonObject.put("hasRunHtml", false);
            } else {
                jsonObject.put("hasRunHtml", true);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }

        asyncHttpServerResponse.send(jsonObject);
    }


    // todo ??????log??????
    public static void post_getLog(AsyncHttpServerRequest asyncHttpServerRequest, AsyncHttpServerResponse asyncHttpServerResponse, String postContent) {
        ensureInit();
//        if (noAccess(asyncHttpServerRequest, asyncHttpServerResponse, postContent)) {
//            return;
//        }
        MainActivity current = getCurrentShowMainActivity();
        if (current == null) {
            asyncHttpServerResponse.code(300);
            asyncHttpServerResponse.send("?????????????????? ?????????app??????");
            return;
        }

        Integer in = JsonUtil.getJsonInt(postContent, "index");
        int index = in == null ? 0 : in;

        JSONArray jsonArray = new JSONArray();
        synchronized (current.consoleMessageList) {
            Iterator<ConsoleMs> iterator = current.consoleMessageList.iterator();
            while (iterator.hasNext()) {
                ConsoleMs next = iterator.next();
                if (next.index <= index) {
                    continue;
                }
                try {
                    JSONObject jsonObject = new JSONObject();
                    jsonObject.put("createTime", next.createTime);
                    jsonObject.put("index", next.index);
                    jsonObject.put("message", (le++) + "  :" + next.message);
                    jsonObject.put("level", next.level);
                    jsonObject.put("line", next.lineNumber);
                    jsonObject.put("sourceId", next.sourceId);
                    jsonArray.put(jsonObject);
                } catch (Exception ignore) {
                }
            }
        }
        asyncHttpServerResponse.send(jsonArray);

    }

    // todo ??????log??????
    public static void post_runApp(AsyncHttpServerRequest asyncHttpServerRequest, AsyncHttpServerResponse asyncHttpServerResponse, String postContent) {
        ensureInit();
        if (noAccess(asyncHttpServerRequest, asyncHttpServerResponse, postContent)) {
            return;
        }

        MainActivity current = getCurrentShowMainActivity();
        if (current == null) {
            asyncHttpServerResponse.code(300);
            asyncHttpServerResponse.send("?????????????????? ?????????app??????");
            return;
        }

        String index = JsonUtil.getJsonString(postContent, "index");
        String zip = JsonUtil.getJsonString(postContent, "offlineZip");

        if (index == null || "".equals(index)) {
            asyncHttpServerResponse.code(300);
            asyncHttpServerResponse.send("???????????? index");
            return;
        }
        if (index.startsWith("http")) {
            Intent intent = new Intent(current, MainActivityNoSingle.class);
            intent.putExtra("launchUrl", index);
            current.startActivity(intent);
            asyncHttpServerResponse.send("{\"code\":\"0\"}");
            return;
        }
        asyncHttpServerResponse.send("{\"code\":\"0\"}");
        HttpManager.fileDownLoad(zip, -1).observeOn(AndroidSchedulers.mainThread()).subscribe(new Observer<DownInfo>() {
            @Override
            public void onSubscribe(Disposable d) {
            }

            @Override
            public void onNext(DownInfo downInfo) {
                switch (downInfo.status) {
                    case START:
                        TopProgressDialog.getInstance(current).onLoading("???????????????");
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
                        TopProgressDialog.getInstance(current).onLoadSuccess("????????????", false);
                        String cacheZip = debugSpace.rootDir.endsWith("/") ? debugSpace.rootDir.substring(0, debugSpace.rootDir.length() - 1) : debugSpace.rootDir;
                        cacheZip += "_cache_work_dir" + ((WorkSpace.index++) % 5);
                        unzip(downInfo.localFilePath, new File(cacheZip));
                        String root = Util.pathAppend(cacheZip, index);
                        if (root.startsWith("/")) {
                            root = "file://" + root;
                        } else {
                            root = "file:///" + root;
                        }
                        Intent intent = new Intent(current, MainActivityNoSingle.class);
                        intent.putExtra("launchUrl", root);
                        current.startActivity(intent);
                        break;
                    case ERROR:
                        onError(downInfo.exception);
                        break;
                }
            }

            @Override
            public void onError(Throwable e) {
                TopProgressDialog.getInstance(current).onLoadFailure("????????????", false);
            }

            @Override
            public void onComplete() {
            }
        });

    }


    // todo ??????appId
    public static void post_getAppId(AsyncHttpServerRequest asyncHttpServerRequest, AsyncHttpServerResponse asyncHttpServerResponse, String postContent) {
        ensureInit();
        if (noAccess(asyncHttpServerRequest, asyncHttpServerResponse, postContent)) {
            return;
        }

        String appId = Util.getCordovaConfigTag("appID", "value");
        if (!TextUtils.isEmpty(appId) && appId.startsWith("_")) {
            appId = appId.substring("_".length());
        }

        if (!TextUtils.isEmpty(appId)) {
            asyncHttpServerResponse.send(appId);
        } else {
            asyncHttpServerResponse.code(300);
            asyncHttpServerResponse.send("???app?????????id");
        }
    }

    // todo ?????????????????????
    public static void post_getHcServerAddress(AsyncHttpServerRequest asyncHttpServerRequest, AsyncHttpServerResponse asyncHttpServerResponse, String postContent) {
        ensureInit();
        if (noAccess(asyncHttpServerRequest, asyncHttpServerResponse, postContent)) {
            return;
        }
        String address = Util.getCordovaConfigTag("hc_mobile_server_address", "value");
        if (!TextUtils.isEmpty(address)) {
            asyncHttpServerResponse.send(address);
        } else {
            asyncHttpServerResponse.code(300);
            asyncHttpServerResponse.send("???app????????????????????????");
        }
    }

    private static final String SHARE_NAME = "supconit_hcmobile_android";
    private static SharedPreferences storage_sharedPreferences = null;

    // todo ?????????????????????
    public static void post_getStorageInfo(AsyncHttpServerRequest asyncHttpServerRequest, AsyncHttpServerResponse asyncHttpServerResponse, String postContent) {
        ensureInit();
//        if (noAccess(asyncHttpServerRequest, asyncHttpServerResponse, postContent)) {
//            return;
//        }

        String key = JsonUtil.getJsonString(postContent, "key");
        if (storage_sharedPreferences == null) {
            storage_sharedPreferences = HcmobileApp.getApplication().getSharedPreferences(SHARE_NAME, MODE_PRIVATE);
        }

        if (TextUtils.isEmpty(key)) {
            asyncHttpServerResponse.code(300);
            asyncHttpServerResponse.send("????????????key");
            return;
        }

        Object result = null;
        Map<String, ?> map = storage_sharedPreferences.getAll();
        for (String k : map.keySet()) {
            Object value = map.get(key);
            if (key.equals(k)) {
                result = value;
                break;
            }
        }

        if (result == null || TextUtils.isEmpty(result.toString())) {
            asyncHttpServerResponse.send("");
        } else {
            asyncHttpServerResponse.send(result.toString());
        }
    }


    /**
     * ???????????????
     *
     * @param zipFilePath     ???????????????   /sdcard/mm.zip
     * @param targetDirectory ?????????????????? /sdcard/cache/tem/
     */
    private static boolean unzip(File zipFilePath, File targetDirectory) {
        ZipFile zipInputStream = null;
        try {
            zipInputStream = new ZipFile(zipFilePath, "GBK");
            ZipEntry zipEntry = null;
            byte[] buffer = new byte[512];
            int readLength = 0;
            Enumeration<ZipEntry> entries = zipInputStream.getEntries();

            while (entries.hasMoreElements()) {
                zipEntry = entries.nextElement();
                File file = new File(targetDirectory.getAbsolutePath() + "/" + zipEntry.getName());
                FileUtil.ensureFileExist(file, zipEntry.isDirectory());
                if (zipEntry.isDirectory()) {
                    continue;
                }
                OutputStream outputStream = new FileOutputStream(file);
                InputStream inputStream = zipInputStream.getInputStream(zipEntry);
                while ((readLength = inputStream.read(buffer, 0, 512)) != -1) {
                    outputStream.write(buffer, 0, readLength);
                }
                try {
                    inputStream.close();
                } catch (Exception e) {
                    e.printStackTrace();
                }
                try {
                    outputStream.close();
                } catch (Exception e) {
                    e.printStackTrace();
                }
                Log.d("powyin", "uncompressed  " + file.getCanonicalPath());
            }
            Log.d("powyin", "unzip success!  ");
            return true;
        } catch (Exception e) {
            java.util.zip.ZipFile zipFile = null;
            try {
                zipFile = new java.util.zip.ZipFile(zipFilePath);
                java.util.zip.ZipEntry zipEntry = null;
                byte[] buffer = new byte[512];
                int readLength = 0;
                Enumeration<? extends java.util.zip.ZipEntry> entries = zipFile.entries();
                while (entries.hasMoreElements()) {
                    zipEntry = entries.nextElement();
                    File file = new File(targetDirectory.getAbsolutePath() + "/" + zipEntry.getName());
                    FileUtil.ensureFileExist(file, zipEntry.isDirectory());
                    if (zipEntry.isDirectory()) {
                        continue;
                    }
                    OutputStream outputStream = new FileOutputStream(file);
                    InputStream inputStream = zipFile.getInputStream(zipEntry);
                    while ((readLength = inputStream.read(buffer, 0, 512)) != -1) {
                        outputStream.write(buffer, 0, readLength);
                    }
                    try {
                        inputStream.close();
                    } catch (Exception a) {
                        a.printStackTrace();
                    }
                    try {
                        outputStream.close();
                    } catch (Exception c) {
                        c.printStackTrace();
                    }
                    Log.d("powyin", "uncompressed  " + file.getCanonicalPath());
                }
                Log.d("powyin", "unzip success!  ");
                return true;
            } catch (Exception ig) {
                ig.printStackTrace();
                System.out.println("unzip fail!");
                Log.d("powyin", "unzip fail!  " + zipFilePath);
            } finally {
                if (zipFile != null) {
                    try {
                        zipFile.close();
                    } catch (Exception d) {
                        d.printStackTrace();
                    }
                }
            }
            e.printStackTrace();
            System.out.println("unzip fail! GBK");
            Log.d("powyin", "unzip fail! GBK  " + zipFilePath);
            return false;
        } finally {
            if (zipInputStream != null) {
                try {
                    zipInputStream.close();
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        }
    }


    /**
     * ????????????
     */
    private static void zip(File zipFile, File sourceDir) {
        if (sourceDir == null || !sourceDir.exists()) {
            throw new RuntimeException(sourceDir + "????????????");
        }
        Project prj = new Project();
        Zip zip = new Zip();
        zip.setProject(prj);
        zip.setDestFile(zipFile);
        FileSet fileSet = new FileSet();
        fileSet.setProject(prj);
        fileSet.setDir(sourceDir);
        //fileSet.setIncludes("**/*.java"); //?????????????????????????????? eg:zip.setIncludes("*.java");
        //fileSet.setExcludes(...); //??????????????????????????????
        zip.addFileset(fileSet);
        zip.execute();
    }


    private static void searchFileItem(FileItem fileItem, ArrayList<FileItem> ret, String searchWord) {
        if (fileItem.children != null) {
            for (FileItem item : fileItem.children) {
                if (item.name != null && item.name.contains(searchWord)) {
                    ret.add(item);
                }
                searchFileItem(item, ret, searchWord);
            }
        }
    }

    /**
     * ???????????????MainActivity
     */
    private static MainActivity getCurrentRunJsEvn() {
        Iterator<Activity> iterator = HcmobileApp.getActivityList().iterator();
        MainActivity mainActivity = null;
        while (iterator.hasNext()) {
            Activity next = iterator.next();
            if (next instanceof MainActivity) {
                mainActivity = (MainActivity) next;
                if (mainActivity.frontShow) {
                    return mainActivity;
                }
            }
        }

        iterator = HcmobileApp.getActivityList().iterator();
        while (iterator.hasNext()) {
            Activity next = iterator.next();
            if (next instanceof MainActivity) {
                mainActivity = (MainActivity) next;
                return mainActivity;
            }
        }
        return null;
    }

    /**
     * ???????????????MainActivity
     */
    private static MainActivity getCurrentShowMainActivity() {
        Iterator<Activity> iterator = HcmobileApp.getActivityList().iterator();
        MainActivity mainActivity = null;
        while (iterator.hasNext()) {
            Activity next = iterator.next();
            if (next.getClass() == MainActivity.class) {
                mainActivity = (MainActivity) next;
                return mainActivity;
            }
        }

        iterator = HcmobileApp.getActivityList().iterator();
        while (iterator.hasNext()) {
            Activity next = iterator.next();
            if (next instanceof MainActivity) {
                mainActivity = (MainActivity) next;
                return mainActivity;
            }
        }
        return null;
    }

    public static void copy(String src, File target) {
        FileUtil.ensureFileExist(target, false);
        try {
            BufferedInputStream bis = new BufferedInputStream(new ByteArrayInputStream(src.getBytes()));
            BufferedOutputStream bos = new BufferedOutputStream(new FileOutputStream(target));
            int len = 0;
            byte[] b = new byte[1024 * 64];
            while ((len = bis.read(b)) != -1) {
                bos.write(b, 0, len);
            }
            bos.flush();
            bis.close();
            bos.close();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }


    public static void addMiniByJsonConfig(String json, File cacheFile) {
        ensureInit();

        String id = JsonUtil.getJsonString(json, "result", "uniqueCode");
        String type = JsonUtil.getJsonString(json, "result", "type");
        String indexUrl = JsonUtil.getJsonString(json, "result", "indexUrl");
        String logoUrl = JsonUtil.getJsonString(json, "result", "logoUrl");
        String remoteUrl = JsonUtil.getJsonString(json, "result", "remoteUrl");
        if (TextUtils.isEmpty(id)) {
            return;
        }


        File mini = FileUtil.getOfficeFilePath(HcmobileApp.getApplication(), "workSpace_mini", "", true);
        mini = new File(mini, id);
        if (cacheFile != null) {
            FileUtil.copyDirOrFile(cacheFile, mini);
        }


        WorkSpace space = new WorkSpace(mini.getAbsolutePath());
        space.sharedPreferences.edit().putString("origin_config_json", json).apply();
        space.resetMiniConfig();

        //FileUtil.copy();
    }


    private transient HashMap<String, FileItem> fileDesHashMap = new HashMap<>();
    private transient FileItem mRoot;
    private transient String rootDir;
    transient SharedPreferences sharedPreferences;
    private transient String localIconRes;
    private transient long createTime;

    private String icon;
    private String indexPage;
    private String type;
    private String name;
    private String iconUserUpload;
    private String onLineId;

    private List<FileItem> children;
    private String spaceId;
    private String folderId;
    private String isFullScreen;


    private WorkSpace(String rootPath) {
        this.rootDir = rootPath;
        this.sharedPreferences = HcmobileApp.getApplication().getSharedPreferences("info_mini_" + rootPath.hashCode(), Context.MODE_MULTI_PROCESS);
        sharedPreferences.edit().putString("test", "test").apply();
        resetFileDescribe();
        resetMiniConfig();
    }

    // ???????????????????????????
    private void resetFileDescribe() {
        fileDesHashMap.clear();
        FileUtil.ensureFileExist(new File(rootDir), true);
        String ip = "http://" + NetUtil.getIP() + ":" + ServerObserver.serverPort;
        mRoot = ensureFileDescribe(new File(rootDir), ip, rootDir);
        fileDesHashMap.put("/", mRoot);


        spaceId = rootDir;
        folderId = rootDir;
        children = mRoot.children;

    }

    // ??????????????????????????????
    private void resetMiniConfig() {
        String ip = "http://" + NetUtil.getIP() + ":" + ServerObserver.serverPort;

        localIconRes = sharedPreferences.getString("localIconRes", null);
        indexPage = sharedPreferences.getString("indexPage", null);
        type = sharedPreferences.getString("type", null);
        name = sharedPreferences.getString("name", null);
        createTime = sharedPreferences.getLong("createTime", 0);
        onLineId = sharedPreferences.getString("onLineId", null);
        isFullScreen = sharedPreferences.getString("isFullScreen", null);


        if ("??????".equals(type)) {
            icon = ip + "/icon_file_mark/offline_app.png";
        } else {
            icon = ip + "/icon_file_mark/online_app.png";
        }

        if (!TextUtils.isEmpty(localIconRes)) {
            if (localIconRes.startsWith("http")) {
                iconUserUpload = localIconRes;
            } else {
                iconUserUpload = Util.pathAppend(ip, "get_file_local", localIconRes);
            }
        } else {
            iconUserUpload = Util.pathAppend(ip, "/icon_file_mark/applet.png");
        }

//        }

    }

    // todo ??????????????????
    private FileItem ensureFileDescribe(File file, String ip, String spaceId) {
        if (file == null || !file.exists()) {
            return null;
        }

        FileItem fileItem = new FileItem();
        fileItem.name = file.getName();
        fileItem.folderId = file.getAbsolutePath();
        fileItem.spaceId = spaceId;
        fileDesHashMap.put(fileItem.folderId, fileItem);
        fileItem.isFile = file.isFile();
        fileItem.createTime = file.lastModified();
        fileItem.type = 0;

        if (fileItem.folderId != null && fileItem.folderId.endsWith("workSpace_debug/example/example.html")) {
            fileItem.type = 1;
        }
        if (fileItem.folderId != null && fileItem.folderId.endsWith("workSpace_debug/example/example.js")) {
            fileItem.type = 1;
        }
        if (fileItem.folderId != null && fileItem.folderId.endsWith("workSpace_debug/example")) {
            fileItem.type = 1;
        }

        if (fileItem.isFile) {
            fileItem.lenght = file.length() / 1024f;
            fileItem.children = null;
            String name = fileItem.name == null ? "" : fileItem.name;
            name = name.substring(name.lastIndexOf('.') + 1);
            name = name + ".png";
            fileItem.icon = ip + "/icon_file_mark/" + name;
        } else {
            fileItem.icon = ip + "/icon_file_mark/folder.png";
            fileItem.children = new ArrayList<>();
            File[] files = file.listFiles();
            if (files != null) {
                for (File tem : files) {
                    fileItem.children.add(ensureFileDescribe(tem, ip, spaceId));
                }
            }
        }
        return fileItem;
    }


}
