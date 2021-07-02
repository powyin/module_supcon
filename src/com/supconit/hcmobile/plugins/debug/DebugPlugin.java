package com.supconit.hcmobile.plugins.debug;

import android.text.TextUtils;
import android.view.View;

import com.supconit.hcmobile.HcmobileApp;
import com.supconit.hcmobile.MainActivity;
import com.supconit.hcmobile.model.ConsoleMs;
import com.supconit.hcmobile.util.FileUtil;
import com.supconit.hcmobile.util.NetUtil;
import com.supconit.hcmobile.util.Util;
import com.supconit.inner_hcmobile.R;

import org.apache.cordova.CallbackContext;
import org.apache.cordova.CordovaPlugin;
import org.apache.cordova.PluginResult;
import org.json.JSONArray;
import org.json.JSONException;

import java.util.Iterator;

public class DebugPlugin extends CordovaPlugin {
    private int minLe = -1;
    public boolean execute(String action, JSONArray args, CallbackContext callbackContext) throws JSONException {
        try {
            switch (action) {
                case "enableDebugLog":
                    HcmobileApp.getHandle().post(new Runnable() {
                        @Override
                        public void run() {
                            try {
                                String par0 = args != null && args.length() >= 1 ? args.get(0).toString() : null;
                                View viewById = cordova.getActivity().findViewById(R.id.hc_mobile_debug_sin_page_block);
                                if (viewById == null) {
                                    callbackContext.error("not icon for debug");
                                    return;
                                }
                                if ("true".equals(par0)) {
                                    viewById.setVisibility(View.VISIBLE);
                                } else {
                                    viewById.setVisibility(View.GONE);
                                }
                            }catch (Exception e){
                                e.printStackTrace();
                            }
                        }
                    });
                    callbackContext.success();
                    return true;
                case "getlog":
                    MainActivity currentShowingCordovaWebView = (MainActivity) cordova.getActivity();
                    if (currentShowingCordovaWebView == null) {
                        callbackContext.error("请打开app");
                        return true;
                    }
                    JSONArray jsonArray = new JSONArray();
                    synchronized (currentShowingCordovaWebView.consoleMessageList) {
                        Iterator<ConsoleMs> iterator = currentShowingCordovaWebView.consoleMessageList.iterator();
                        while (iterator.hasNext()) {
                            ConsoleMs next = iterator.next();
                            if (next.index <= minLe) {
                                continue;
                            }
                            try {
                                jsonArray.put(next.message);
                            } catch (Exception ignore) {
                            }
                            minLe = Math.max(minLe, next.index);
                        }
                    }
                    callbackContext.success(jsonArray);
                    return true;
                case "getDebuggingAddress":
                    String address = "http://" + NetUtil.getIP() + ":" + ServerObserver.serverPort;
                    callbackContext.success(address);
                    return true;
                case "loadDebugConfig":
                    String debug = Util.getCordovaConfigTag("buildType", "value");
                    if ("release".equals(debug)) {
                        callbackContext.error("this is release build");
                        return false;
                    }

                    HcmobileApp.getHandle().post(new Runnable() {
                        @Override
                        public void run() {
                            try {
                                String url = ((MainActivity) cordova.getActivity()).mSystemWebView.getUrl();
                                String js;
                                if (url.startsWith("https")) {
                                    js = FileUtil.readAssetFileAsString(HcmobileApp.getApplication(), "www/hcmobile/js/debug_icon.js");
                                    if (TextUtils.isEmpty(js)) {
                                        js = FileUtil.readAssetFileAsString(HcmobileApp.getApplication(), "/www/hcmobile/js/debug_icon.js");
                                    }
                                } else {
                                    String address = null;
                                    if (url.startsWith("file")) {
                                        address = "file:///android_asset/www/hcmobile/js/debug_icon.js";
                                    } else if (url.startsWith("http:")) {
                                        String ip = "http://" + NetUtil.getIP() + ":" + ServerObserver.serverPort;
                                        address = Util.pathAppend(ip, "js/debug_icon.js");
                                    }
                                    js = "var src = document.createElement('script');\n" +
                                            "    src.type = 'text/javascript';\n" +
                                            "    src.async = true;\n" +
                                            "    src.charset = 'utf-8';\n" +
                                            "    src.src = '" + address + "';\n" +
                                            "    document.head.appendChild(src);";
                                }
                                ((MainActivity) cordova.getActivity()).loadUrl("javascript: " + js);
                            } catch (Exception e) {
                                e.printStackTrace();
                            }
                        }
                    });
                    break;
            }
        } catch (Exception e) {
            callbackContext.sendPluginResult(new PluginResult(PluginResult.Status.JSON_EXCEPTION));
            return false;
        }
        return false;
    }


}