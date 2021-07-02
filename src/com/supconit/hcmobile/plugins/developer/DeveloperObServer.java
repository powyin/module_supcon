package com.supconit.hcmobile.plugins.developer;

import android.content.res.AssetManager;
import android.text.TextUtils;

import com.supconit.develop.JSApplicationCreate;
import com.supconit.hcmobile.HcmobileApp;
import com.supconit.hcmobile.appplugin.ApplicationObserver;
import com.supconit.hcmobile.util.FileUtil;
import com.supconit.hcmobile.util.JsonUtil;
import com.supconit.hcmobile.util.Util;

import java.io.IOException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class DeveloperObServer extends ApplicationObserver {
    static boolean isInitApp = false;
    static Map<String, String> jsNativeMap = new HashMap<>();

    @Override
    public void onCreate() {
        super.onCreate();
        AssetManager assets = HcmobileApp.getApplication().getAssets();
        String sdkId = Util.getCordovaConfigTag("sdkId", "value");
        if (TextUtils.isEmpty(sdkId)) {
            try {
                String[] list = assets.list("");
                assert list != null;
                for (String item : list) {
                    if (item.startsWith("plugins_")) {
                        try {
                            String json = FileUtil.readAssetFileAsString(HcmobileApp.getApplication(), item);
                            if (TextUtils.isEmpty(json)) {
                                json = FileUtil.readAssetFileAsString(HcmobileApp.getApplication(), "/" + item);
                            }
                            String name = JsonUtil.getJsonString(json, "name");
                            if (!TextUtils.isEmpty(name)) {
                                jsNativeMap.put(name, json);
                            }
                        } catch (Exception e) {
                            e.printStackTrace();
                        }
                    }
                }
            } catch (IOException e) {
                e.printStackTrace();
            }
        }

        List<String> configTagList = Util.getCordovaConfigTagList("sdkitem", "name");
        for (String tag : configTagList) {
            String json = FileUtil.readAssetFileAsString(HcmobileApp.getApplication(), tag);
            if (TextUtils.isEmpty(json)) {
                json = FileUtil.readAssetFileAsString(HcmobileApp.getApplication(), "/" + tag);
            }
            String name = JsonUtil.getJsonString(json, "name");
            if (!TextUtils.isEmpty(name)) {
                jsNativeMap.put(name, json);
            }
        }


        for (Map.Entry<String, String> next : jsNativeMap.entrySet()) {
            String value = next.getValue();
            value = JsonUtil.getJsonString(value, "android_entry_application_launch");
            if (!TextUtils.isEmpty(value)) {
                try {
                    Class cla = Class.forName(value);
                    Object o = cla.newInstance();
                    ((JSApplicationCreate) o).onApplicationOnCreate(getApplicationContext());
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        }


    }
}
