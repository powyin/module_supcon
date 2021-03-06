package com.supconit.hcmobile.plugins.cent;


import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.text.TextUtils;
import com.gyf.immersionbar.ImmersionBar;
import com.supconit.hcmobile.HcmobileApp;
import com.supconit.hcmobile.MainActivity;
import com.supconit.hcmobile.util.IsPerssionUtil;
import com.supconit.hcmobile.util.Util;

import org.apache.cordova.CallbackContext;
import org.apache.cordova.Const;
import org.apache.cordova.CordovaActivity;
import org.apache.cordova.CordovaInterface;
import org.apache.cordova.CordovaPlugin;
import org.apache.cordova.CordovaWebView;
import org.apache.cordova.PluginResult;
import org.json.JSONArray;
import org.json.JSONException;

public class Center extends CordovaPlugin {


    @Override
    public void initialize(CordovaInterface cordova, CordovaWebView webView) {
        super.initialize(cordova, webView);
        if (HcmobileApp.getApplication().getPackageName().equals("com.hcapp.test")){
            IsPerssionUtil.requestResult();
        }else {
            SharedPreferences sharedPreferences=HcmobileApp.getApplication().getSharedPreferences("powyin_app_data_is_perssion", Context.MODE_PRIVATE);
            SharedPreferences.Editor editor=sharedPreferences.edit();
            editor.putBoolean("isPerssion",true);
            editor.commit();
        }
    }

    /**
     * Executes the request and returns PluginResult.
     *
     * @param action          The action to execute.
     * @param args            JSONArry of arguments for the plugin.
     * @param callbackContext The callback context from which we were invoked.
     * @return A PluginResult object with a status and message.
     */
    public boolean execute(String action, JSONArray args, CallbackContext callbackContext) throws JSONException {
        try {
            switch (action) {
                case "reset":
                    reset();
                    callbackContext.success("success");
                    break;
                case "reload":
                    reload();
                    callbackContext.success("success");
                    break;
                case "setColor":
                    if (!ImmersionBar.isSupportStatusBarDarkFont()) {
                        callbackContext.error("??????????????????????????????????????????");
                        return true;
                    }
                    if ("true".equals(Util.getCordovaConfigTag("layout_immersion", "value"))){
                        HcmobileApp.getHandle().post(new Runnable() {
                            @Override
                            public void run() {
                                if (args.optString(0).equals("white")){
                                    //Android??????????????????
                                    ((MainActivity)cordova.getActivity()).setYs(false);
                                }else if (args.optString(0).equals("black")){
                                    //Android??????????????????
                                    ((MainActivity)cordova.getActivity()).setYs(true);
                                }else {
                                    return;
                                }
                            }
                        });
                    }else {
                        callbackContext.error("?????????????????????????????????????????????????????????");
                    }
                    break;
                case "getStatusBarHeight":
                    int statusBarHeight = ImmersionBar.getStatusBarHeight(cordova.getActivity());
                    if ("true".equals(Util.getCordovaConfigTag("layout_immersion", "value"))) {
                        callbackContext.success(statusBarHeight);
                    } else {
                        callbackContext.success(0);
                    }
                    break;
            }
        } catch (Exception e) {
            callbackContext.sendPluginResult(new PluginResult(PluginResult.Status.JSON_EXCEPTION));
            return true;
        }
        return true;
    }


    /**
     * ????????????
     */
    private void reset() {
        cordova.getActivity().runOnUiThread(new Runnable() {
            @Override
            public void run() {
                try {
                    Intent intent = new Intent(cordova.getActivity(), ReloadUrlActivity.class);
                    cordova.getActivity().startActivity(intent);
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        });
    }

    /**
     * ????????????
     */
    public void reload() {
        cordova.getActivity().runOnUiThread(new Runnable() {
            @Override
            public void run() {
                try {
                    String failingUrl = ((CordovaActivity) cordova.getActivity()).getFailingUrl();
                    String currentUrl = webView.getUrl();
                    // ??????????????????????????????
                    if (Const.errorUrls.contains(currentUrl)) {
                        if (failingUrl != null && failingUrl.length() > 0) {
                            webView.loadUrl(failingUrl);
                        }
                    } else {
                        if (!TextUtils.isEmpty(currentUrl)) {
                            webView.loadUrl(currentUrl);
                        }
                    }
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        });

    }


}