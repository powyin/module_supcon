package com.supconit.hcmobile.plugins.developer;

import android.app.Application;
import android.content.res.AssetManager;
import android.text.TextUtils;

import com.supconit.develop.Response;
import com.supconit.develop.JSBridge;
import com.supconit.hcmobile.HcmobileApp;
import com.supconit.hcmobile.util.FileUtil;
import com.supconit.hcmobile.util.JsonUtil;
import com.supconit.hcmobile.util.Util;

import org.apache.cordova.CallbackContext;
import org.apache.cordova.CordovaInterface;
import org.apache.cordova.CordovaPlugin;
import org.apache.cordova.CordovaWebView;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class developer extends CordovaPlugin {

    private HashMap<String, JSBridge> jsNativeMapInstance = new HashMap<String, JSBridge>();


    @Override
    public void initialize(CordovaInterface cordova, CordovaWebView webView) {

        for (Map.Entry<String, String> next : DeveloperObServer.jsNativeMap.entrySet()) {
            String key = next.getKey();
            String value = next.getValue();
            value = JsonUtil.getJsonString(value, "android_entry");
            try {
                Class cla = Class.forName(value);
                Object o = cla.newInstance();
                jsNativeMapInstance.put(key, (JSBridge) o);
            } catch (Exception e) {
                e.printStackTrace();
            }
        }

        for (Map.Entry<String, String> next : DeveloperObServer.jsNativeMap.entrySet()) {
            System.out.println("----------------------------def plugin---" + next.getKey() + "    " + next.getValue());
        }

        if (!DeveloperObServer.isInitApp) {
            DeveloperObServer.isInitApp = true;
            for (HashMap.Entry<String, JSBridge> entry : jsNativeMapInstance.entrySet()) {
                entry.getValue().onApplicationCreate((Application) HcmobileApp.getApplication());
            }
        }

    }

    @Override
    public boolean execute(String action, JSONArray args, CallbackContext callbackContext) throws JSONException {
        System.out.println("----------------------------def plugin---:" + action);
        if ("hcMobile_getAssertInfo".equals(action)) {
            JSONArray jsonArray = new JSONArray();
            for (String value : DeveloperObServer.jsNativeMap.values()) {
                try {
                    JSONObject jsonObject = new JSONObject(value);
                    jsonArray.put(jsonObject);
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
            callbackContext.success(jsonArray);
            return true;
        }

        String ar0 = args.getString(0);
        String ar1 = args.getString(1);

        JSONArray array = new JSONArray();
        for (int i = 2; i < args.length(); i++) {
            array.put(args.get(i));
        }

        for (HashMap.Entry<String, JSBridge> entry : jsNativeMapInstance.entrySet()) {
            String key = entry.getKey();
            if (key.equals(ar0)) {
                entry.getValue().execute(ar1, array, new Response(callbackContext));
                return true;
            }
        }

        callbackContext.error("无法找到对应name: " + ar0 + " 的实体类");
        return false;
    }


    private boolean isInitOnCreate;

    @Override
    public void onStart() {
        super.onStart();
        if (!isInitOnCreate) {
            for (HashMap.Entry<String, JSBridge> entry : jsNativeMapInstance.entrySet()) {
                entry.getValue().onActivityCreate(cordova.getActivity());
            }
            isInitOnCreate = true;
        }
    }


    @Override
    public void onPause(boolean multitasking) {
        super.onPause(multitasking);
        for (HashMap.Entry<String, JSBridge> entry : jsNativeMapInstance.entrySet()) {
            entry.getValue().onActivityPase(cordova.getActivity());
        }
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        for (HashMap.Entry<String, JSBridge> entry : jsNativeMapInstance.entrySet()) {
            entry.getValue().onActivityDestory(cordova.getActivity());
        }
    }
}




























