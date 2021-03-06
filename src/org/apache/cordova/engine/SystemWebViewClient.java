/*
       Licensed to the Apache Software Foundation (ASF) under one
       or more contributor license agreements.  See the NOTICE file
       distributed with this work for additional information
       regarding copyright ownership.  The ASF licenses this file
       to you under the Apache License, Version 2.0 (the
       "License"); you may not use this file except in compliance
       with the License.  You may obtain a copy of the License at

         http://www.apache.org/licenses/LICENSE-2.0

       Unless required by applicable law or agreed to in writing,
       software distributed under the License is distributed on an
       "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
       KIND, either express or implied.  See the License for the
       specific language governing permissions and limitations
       under the License.
*/
package org.apache.cordova.engine;

import android.annotation.TargetApi;
import android.content.Context;
import android.content.SharedPreferences;
import android.content.pm.ApplicationInfo;
import android.content.pm.PackageManager;
import android.content.pm.PackageManager.NameNotFoundException;
import android.graphics.Bitmap;
import android.net.Uri;
import android.net.http.SslError;
import android.os.Build;
import android.support.annotation.Nullable;
import android.text.TextUtils;
import android.util.Log;
import android.view.View;
import android.webkit.ClientCertRequest;
import android.webkit.CookieManager;
import android.webkit.HttpAuthHandler;
import android.webkit.SslErrorHandler;
import android.webkit.ValueCallback;
import android.webkit.WebResourceRequest;
import android.webkit.WebResourceResponse;
import android.webkit.WebView;
import android.webkit.WebViewClient;

import com.supconit.hcmobile.HcmobileApp;
import com.supconit.hcmobile.MainActivity;
import com.supconit.hcmobile.util.FileUtil;
import com.supconit.hcmobile.util.Util;
import com.supconit.inner_hcmobile.R;

import org.apache.cordova.AuthenticationToken;
import org.apache.cordova.Const;
import org.apache.cordova.CordovaClientCertRequest;
import org.apache.cordova.CordovaHttpAuthHandler;
import org.apache.cordova.CordovaResourceApi;
import org.apache.cordova.CordovaWebViewEngine;
import org.apache.cordova.LOG;
import org.apache.cordova.PackageUtil;
import org.apache.cordova.PluginManager;

import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.lang.reflect.Method;
import java.util.HashMap;
import java.util.Hashtable;
import java.util.Map;

import static android.content.Context.MODE_PRIVATE;


/**
 * This class is the WebViewClient that implements callbacks for our web view.
 * The kind of callbacks that happen here are regarding the rendering of the
 * document instead of the chrome surrounding it, such as onPageStarted(),
 * shouldOverrideUrlLoading(), etc. Related to but different than
 * CordovaChromeClient.
 */
public class SystemWebViewClient extends WebViewClient {

    static boolean mForbiddenCache = false;
    static long mCacheTime = 10 * 60 * 60 * 1000;
    static String mHotCodeFileOfflinePath;

    /**
     * js ????????????
     */
    public static void setForbiddenCache(boolean isForbidden, long cacheTime) {
        mForbiddenCache = isForbidden;
        if (cacheTime > 0) {
            mCacheTime = cacheTime;
        }
    }

    /*
    ?????????????????? ?????????????????????
     */
    public static void setHotCodeOfflineFilePath(String path) {
        mHotCodeFileOfflinePath = path;
    }

    private static final String TAG = "SystemWebViewClient";
    private static final String INJECTION_TOKEN = "supconit://";
    private static final String LOCAL = "local://";

    private final SystemWebViewEngine parentEngine;
    private boolean doClearHistory = false;
    private boolean isCurrentlyLoading;

    /**
     * The authorization tokens.
     */
    private Hashtable<String, AuthenticationToken> authenticationTokens = new Hashtable<String, AuthenticationToken>();

    public SystemWebViewClient(SystemWebViewEngine parentEngine) {
        this.parentEngine = parentEngine;
    }

    private String hc_authorize_server_address;

    @Override
    public boolean shouldOverrideUrlLoading(WebView view, String url) {
        if (hc_authorize_server_address == null) {
            hc_authorize_server_address = Util.getCordovaConfigTag("hc_authorize_server_address", "value");
            if (hc_authorize_server_address == null) {
                hc_authorize_server_address = "noValid";
            }
        }

        if (!TextUtils.isEmpty(hc_authorize_server_address)) {
            while (hc_authorize_server_address.endsWith("/")) {
                hc_authorize_server_address = hc_authorize_server_address.substring(0, hc_authorize_server_address.length() - 1);
            }
            String copyUrl = url;
            while (copyUrl.endsWith("/")) {
                copyUrl = copyUrl.substring(0, copyUrl.length() - 1);
            }
            hc_authorize_server_address = hc_authorize_server_address.replace("https://", "http://");
            copyUrl = copyUrl.replace("https://", "http://");

            if (copyUrl.contains("?")) {
                copyUrl = copyUrl.substring(0, copyUrl.indexOf("?"));
            }
            if (hc_authorize_server_address.contains("?")) {
                hc_authorize_server_address = hc_authorize_server_address.substring(0, copyUrl.indexOf("?"));
            }

            System.out.println("1:::::::>>>>>" + copyUrl);
            System.out.println("2:::::::>>>>>" + hc_authorize_server_address);

            if (hc_authorize_server_address.equalsIgnoreCase(copyUrl)) {
                HcmobileApp.getHandle().post(new Runnable() {
                    @Override
                    public void run() {
                        String SHARE_NAME = "supconit_hcmobile_android";
                        SharedPreferences sharedPreferences = HcmobileApp.getApplication().getSharedPreferences(SHARE_NAME, MODE_PRIVATE);
                        String token = sharedPreferences.getString("token", "");
                        Map<String, String> he = new HashMap<>();
                        he.put("auth_flag", token);
                        view.loadUrl(url + "&cc=cc", he);
                    }
                });
                return true;
            }

        }


        try {
            Method shouldOverrideUrlLoading = Class.forName("com.supconit.hcmobile.util.Util").getMethod("shouldOverrideUrlLoading", WebView.class, String.class);
            if ((Boolean) shouldOverrideUrlLoading.invoke(null, view, url)) {
                return true;
            }
        } catch (Exception e) {
            e.printStackTrace();
        }


        return parentEngine.client.onNavigationAttempt(url);
    }


    /**
     * On received http auth request.
     * The method reacts on all registered authentication tokens. There is one and only one authentication token for any host + realm combination
     */
    @Override
    public void onReceivedHttpAuthRequest(WebView view, HttpAuthHandler handler, String host, String realm) {

        // Get the authentication token (if specified)
        AuthenticationToken token = this.getAuthenticationToken(host, realm);
        if (token != null) {
            handler.proceed(token.getUserName(), token.getPassword());
            return;
        }

        // Check if there is some plugin which can resolve this auth challenge
        PluginManager pluginManager = this.parentEngine.pluginManager;
        if (pluginManager != null && pluginManager.onReceivedHttpAuthRequest(null, new CordovaHttpAuthHandler(handler), host, realm)) {
            parentEngine.client.clearLoadTimeoutTimer();
            return;
        }

        // By default handle 401 like we'd normally do!
        super.onReceivedHttpAuthRequest(view, handler, host, realm);
    }

    /**
     * On received client cert request.
     * The method forwards the request to any running plugins before using the default implementation.
     *
     * @param view
     * @param request
     */

    @TargetApi(21)
    public void onReceivedClientCertRequest(WebView view, ClientCertRequest request) {

        // Check if there is some plugin which can resolve this certificate request
        PluginManager pluginManager = this.parentEngine.pluginManager;
        if (pluginManager != null && pluginManager.onReceivedClientCertRequest(null, new CordovaClientCertRequest(request))) {
            parentEngine.client.clearLoadTimeoutTimer();
            return;
        }

        // By default pass to WebViewClient
        super.onReceivedClientCertRequest(view, request);
    }

    /**
     * Notify the host application that a page has started loading.
     * This method is called once for each main frame load so a page with iframes or framesets will call onPageStarted
     * one time for the main frame. This also means that onPageStarted will not be called when the contents of an
     * embedded frame changes, i.e. clicking a link whose target is an iframe.
     *
     * @param view The webview initiating the callback.
     * @param url  The url of the page.
     */
    @Override
    public void onPageStarted(WebView view, String url, Bitmap favicon) {
        super.onPageStarted(view, url, favicon);
        isCurrentlyLoading = true;
        // Flush stale messages & reset plugins.
        parentEngine.bridge.reset();
        parentEngine.client.onPageStarted(url);
    }

    /**
     * Notify the host application that a page has finished loading.
     * This method is called only for main frame. When onPageFinished() is called, the rendering picture may not be updated yet.
     *
     * @param view The webview initiating the callback.
     * @param url  The url of the page.
     */
    @Override
    public void onPageFinished(WebView view, String url) {

        HcmobileApp.getHandle().post(new Runnable() {
            @Override
            public void run() {
                // todo ????????????js??????
                String json = FileUtil.readAssetFileAsString(HcmobileApp.getApplication(), "www/cordova.js");
                if (TextUtils.isEmpty(json)) {
                    json = FileUtil.readAssetFileAsString(HcmobileApp.getApplication(), "/www/cordova.js");
                }
                WebView webView = parentEngine.webView;
                webView.evaluateJavascript(json, new ValueCallback<String>() {
                    @Override
                    public void onReceiveValue(String value) {
                        Log.e("powyin", "auto load");
                    }
                });

                // todo ???????????????????????????
                lab:
                {
                    MainActivity mainActivity = (MainActivity) parentEngine.cordova.getActivity();
                    View viewById = mainActivity.findViewById(R.id.hc_mini_click_left_uni_uni);
                    if (viewById != null) {
                        if (webView.canGoBack()) {
                            String failingUrl = mainActivity.getFailingUrl();
                            CordovaWebViewEngine.WebBackForwardListHC history = parentEngine.copyBackForwardList();
                            for (int i = history.getSize() - 2; i >= 0; i--) {
                                CordovaWebViewEngine.WebHistoryItemHC item = history.getItemAtIndex(i);
                                String urlItem = item.getUrl();
                                if (urlItem != null && !urlItem.equals(failingUrl) && !Const.errorUrls.contains(urlItem)) {
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

        super.onPageFinished(view, url);
        if (mForbiddenCache) {
            view.clearCache(true);
        }
        // Ignore excessive calls, if url is not about:blank (CB-8317).
        if (!isCurrentlyLoading && !url.startsWith("about:")) {
            return;
        }
        isCurrentlyLoading = false;

        if (this.doClearHistory) {
            view.clearHistory();
            this.doClearHistory = false;
        }
        parentEngine.client.onPageFinishedLoading(url);

    }

    /**
     * Report an error to the host application. These errors are unrecoverable (i.e. the main resource is unavailable).
     * The errorCode parameter corresponds to one of the ERROR_* constants.
     *
     * @param view        The WebView that is initiating the callback.
     * @param errorCode   The error code corresponding to an ERROR_* value.
     * @param description A String describing the error.
     * @param failingUrl  The url that failed to load.
     */
    @Override
    public void onReceivedError(WebView view, int errorCode, String description, String failingUrl) {
        // Ignore error due to stopLoading().
        if (!isCurrentlyLoading) {
            return;
        }
        LOG.d(TAG, "CordovaWebViewClient.onReceivedError: Error code=%s Description=%s URL=%s", errorCode, description, failingUrl);

        // If this is a "Protocol Not Supported" error, then revert to the previous
        // page. If there was no previous page, then punt. The application's config
        // is likely incorrect (start page set to sms: or something like that)
        if (errorCode == WebViewClient.ERROR_UNSUPPORTED_SCHEME) {
            parentEngine.client.clearLoadTimeoutTimer();

            if (view.canGoBack()) {
                view.goBack();
                return;
            } else {
                super.onReceivedError(view, errorCode, description, failingUrl);
            }
        }
        parentEngine.client.onReceivedError(errorCode, description, failingUrl);
    }

    /**
     * Notify the host application that an SSL error occurred while loading a resource.
     * The host application must call either handler.cancel() or handler.proceed().
     * Note that the decision may be retained for use in response to future SSL errors.
     * The default behavior is to cancel the load.
     *
     * @param view    The WebView that is initiating the callback.
     * @param handler An SslErrorHandler object that will handle the user's response.
     * @param error   The SSL error object.
     */
    @TargetApi(8)
    @Override
    public void onReceivedSslError(WebView view, SslErrorHandler handler, SslError error) {

        final String packageName = parentEngine.cordova.getActivity().getPackageName();
        final PackageManager pm = parentEngine.cordova.getActivity().getPackageManager();

        ApplicationInfo appInfo;
        try {
            appInfo = pm.getApplicationInfo(packageName, PackageManager.GET_META_DATA);
            if ((appInfo.flags & ApplicationInfo.FLAG_DEBUGGABLE) != 0) {
                // debug = true
                handler.proceed();
                return;
            } else {
                // debug = false
                super.onReceivedSslError(view, handler, error);
            }
        } catch (NameNotFoundException e) {
            // When it doubt, lock it out!
            super.onReceivedSslError(view, handler, error);
        }
    }


    /**
     * Sets the authentication token.
     *
     * @param authenticationToken
     * @param host
     * @param realm
     */
    public void setAuthenticationToken(AuthenticationToken authenticationToken, String host, String realm) {
        if (host == null) {
            host = "";
        }
        if (realm == null) {
            realm = "";
        }
        this.authenticationTokens.put(host.concat(realm), authenticationToken);
    }

    /**
     * Removes the authentication token.
     *
     * @param host
     * @param realm
     * @return the authentication token or null if did not exist
     */
    public AuthenticationToken removeAuthenticationToken(String host, String realm) {
        return this.authenticationTokens.remove(host.concat(realm));
    }

    /**
     * Gets the authentication token.
     * <p>
     * In order it tries:
     * 1- host + realm
     * 2- host
     * 3- realm
     * 4- no host, no realm
     *
     * @param host
     * @param realm
     * @return the authentication token
     */
    public AuthenticationToken getAuthenticationToken(String host, String realm) {
        AuthenticationToken token = null;
        token = this.authenticationTokens.get(host.concat(realm));

        if (token == null) {
            // try with just the host
            token = this.authenticationTokens.get(host);

            // Try the realm
            if (token == null) {
                token = this.authenticationTokens.get(realm);
            }

            // if no host found, just query for default
            if (token == null) {
                token = this.authenticationTokens.get("");
            }
        }

        return token;
    }

    /**
     * Clear all authentication tokens.
     */
    public void clearAuthenticationTokens() {
        this.authenticationTokens.clear();
    }


    @TargetApi(Build.VERSION_CODES.HONEYCOMB)
    @Override
    public WebResourceResponse shouldInterceptRequest(WebView view, String url) {
        super.shouldInterceptRequest(view, url);

        // todo hcmobile:// ????????????
        if (url != null && url.contains(INJECTION_TOKEN)) {
            String assetPath = "www/" + url.substring(url.indexOf(INJECTION_TOKEN) + INJECTION_TOKEN.length(), url.length());
            try {
                return new WebResourceResponse(
                        "text/plain",
                        "UTF-8",
                        view.getContext().getAssets().open(assetPath)
                );
            } catch (IOException e) {
                e.printStackTrace();
                return new WebResourceResponse("text/plain", "UTF-8", null);
            }
        }

        // todo local:// ????????????
        if (url != null && url.contains(LOCAL)) {
            if (mHotCodeFileOfflinePath != null && mHotCodeFileOfflinePath.length() > 0) {

                // todo ???????????????????????????
                try {
                    String cacheFile = PackageUtil.pathAppend(mHotCodeFileOfflinePath, url.substring(url.indexOf(LOCAL) + LOCAL.length(), url.length()));
                    File file = new File(cacheFile);
                    Log.d("powyin", "local to cache File path = " + file.getAbsolutePath());
                    if (file.exists() && file.length() > 0) {
                        String[] fileNameFromUrl = PackageUtil.getFileNameFromUrl(cacheFile);
                        switch (fileNameFromUrl[1]) {
                            case "css":
                            case "cs":
                                return new WebResourceResponse("text/css", "UTF-8", new FileInputStream(file));
                            default:
                                return new WebResourceResponse("text/plain", "UTF-8", new FileInputStream(file));
                        }
                    } else {
                        Log.e("powyin", "local:// missing file orig " + url);
                        Log.e("powyin", "local:// missing file need " + file);
                    }
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }

            // todo assetPath ???????????????
            String assetPath = PackageUtil.pathAppend("www/offline/", url.substring(url.indexOf(LOCAL) + LOCAL.length(), url.length()));
            try {
                Log.d("powyin", "local to assert File path = " + assetPath);
                String[] fileNameFromUrl = PackageUtil.getFileNameFromUrl(assetPath);
                switch (fileNameFromUrl[1]) {
                    case "css":
                    case "cs":
                        return new WebResourceResponse("text/css", "UTF-8", view.getContext().getAssets().open(assetPath));
                    default:
                        return new WebResourceResponse("text/plain", "UTF-8", view.getContext().getAssets().open(assetPath));
                }
            } catch (IOException e) {
                e.printStackTrace();
                return new WebResourceResponse("text/plain", "UTF-8", null);
            }
        }


        try {
            // Check the against the whitelist and lock out access to the WebView directory
            // Changing this will cause problems for your application
//            if (!parentEngine.pluginManager.shouldAllowRequest(url)) {
//                LOG.w(TAG, "URL blocked by whitelist: " + url);
//                // Results in a 404.
//                return new WebResourceResponse("text/plain", "UTF-8", null);
//            }

            CordovaResourceApi resourceApi = parentEngine.resourceApi;
            Uri origUri = Uri.parse(url);
            // Allow plugins to intercept WebView requests.
            Uri remappedUri = resourceApi.remapUri(origUri);

            if (!origUri.equals(remappedUri) || needsSpecialsInAssetUrlFix(origUri) || needsKitKatContentUrlFix(origUri)) {
                CordovaResourceApi.OpenForReadResult result = resourceApi.openForRead(remappedUri, true);
                return new WebResourceResponse(result.mimeType, "UTF-8", result.inputStream);
            }
            // If we don't need to special-case the request, let the browser load it.

//            try {
//                return ResourceCache.getWebResource(view, url);
//            } catch (Exception e) {
//                e.printStackTrace();
//                return null;
//            }
            return null;

        } catch (IOException e) {
            if (!(e instanceof FileNotFoundException)) {
                LOG.e(TAG, "Error occurred while loading a file (returning a 404).", e);
            }
            // Results in a 404.
            return new WebResourceResponse("text/plain", "UTF-8", null);
        }
    }

    private static boolean needsKitKatContentUrlFix(Uri uri) {
        return android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.KITKAT && "content".equals(uri.getScheme());
    }

    private static boolean needsSpecialsInAssetUrlFix(Uri uri) {
        if (CordovaResourceApi.getUriType(uri) != CordovaResourceApi.URI_TYPE_ASSET) {
            return false;
        }
        if (uri.getQuery() != null || uri.getFragment() != null) {
            return true;
        }

        if (!uri.toString().contains("%")) {
            return false;
        }

        switch (android.os.Build.VERSION.SDK_INT) {
            case android.os.Build.VERSION_CODES.ICE_CREAM_SANDWICH:
            case android.os.Build.VERSION_CODES.ICE_CREAM_SANDWICH_MR1:
                return true;
        }
        return false;
    }
}
