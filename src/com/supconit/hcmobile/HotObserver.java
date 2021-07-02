package com.supconit.hcmobile;

import android.content.Context;
import android.content.SharedPreferences;
import android.text.TextUtils;
import android.util.Log;

import com.supconit.hcmobile.appplugin.ApplicationObserver;
import com.supconit.hcmobile.util.FileUtil;
import com.supconit.hcmobile.util.JsonUtil;
import com.supconit.hcmobile.util.Util;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;


public class HotObserver extends ApplicationObserver {

    private static final String SHARE_NAME_FOR_PLATFORM = "supconit_hcmobile_android_for_platform";

    @Override
    public void onCreate() {
        String cordovaConfigTag = Util.getCordovaConfigTag("content", "src");
        if (TextUtils.isEmpty(cordovaConfigTag)) {
            return;
        }


        SharedPreferences sharedPreferences = HcmobileApp.getApplication().getSharedPreferences(SHARE_NAME_FOR_PLATFORM, Context.MODE_PRIVATE);
        String olderContent = sharedPreferences.getString("launchUrl_config_content", null);

        if (TextUtils.isEmpty(olderContent)) {
            SimpleDateFormat dateFormatter = new SimpleDateFormat("yyyyMMddhhmmss", Locale.getDefault());
            String formatDate = dateFormatter.format(new Date());
            resetContent(formatDate, cordovaConfigTag);
            return;
        }

        if (!cordovaConfigTag.equals(olderContent)) {
            SimpleDateFormat dateFormatter = new SimpleDateFormat("yyyyMMddhhmmss", Locale.getDefault());
            String formatDate = dateFormatter.format(new Date());
            resetContent(formatDate, cordovaConfigTag);
            return;
        }


        String json = FileUtil.readAssetFileAsString(HcmobileApp.getApplication(), "www/offline/hcmobile.json");
        if (TextUtils.isEmpty(json)) {
            json = FileUtil.readAssetFileAsString(HcmobileApp.getApplication(), "/www/offline/hcmobile.json");
        }
        String release = JsonUtil.getJsonString(json, "release");
        if (TextUtils.isEmpty(release)) {
            return;
        }

        String older = sharedPreferences.getString("launchUrl_older_verson", null);
        if (TextUtils.isEmpty(older)) {
            older = "0";
        }
        try {
            release = release.replace(".", "");
            release = release.replace("-", "");
            long cu = Long.parseLong(release);
            long ol = Long.parseLong(older);
            if (ol >= cu) {
                return;
            }
        } catch (Exception e) {
            e.printStackTrace();
            return;
        }

        resetContent(release, cordovaConfigTag);
    }


    private void resetContent(String time, String nowContent) {
        Log.e("powyin", "content has be reset to null");
        SharedPreferences sharedPreferences = HcmobileApp.getApplication().getSharedPreferences(SHARE_NAME_FOR_PLATFORM, Context.MODE_PRIVATE);
        sharedPreferences.edit()
                .putString("launchUrl_older_verson", time)
                .putString("launchUrl", null)
                .putString("launchUrl_config_content", nowContent)
                .apply();
    }

}
