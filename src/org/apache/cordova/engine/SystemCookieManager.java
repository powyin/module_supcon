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
import android.os.Build;
import android.text.TextUtils;
import android.webkit.CookieManager;
import android.webkit.CookieSyncManager;
import android.webkit.WebView;

import com.supconit.hcmobile.HcmobileApp;

import org.apache.cordova.ICordovaCookieManager;

import java.util.Iterator;
import java.util.Map;

class SystemCookieManager implements ICordovaCookieManager {

    protected final WebView webView;
    private final CookieManager cookieManager;

    //Added because lint can't see the conditional RIGHT ABOVE this
    @TargetApi(Build.VERSION_CODES.LOLLIPOP)
    public SystemCookieManager(WebView webview) {
        webView = webview;
        cookieManager = CookieManager.getInstance();
        cookieManager.setAcceptCookie(true);
        CookieManager.setAcceptFileSchemeCookies(true);

//        SharedPreferences sharedPreferences = HcmobileApp.getApplication().getSharedPreferences("share_for_cookie_chache", Context.MODE_PRIVATE);
//        Map<String, ?> all = sharedPreferences.getAll();
//        if (all != null && all.size() > 0) {
//            Iterator<? extends Map.Entry<String, ?>> iterator = all.entrySet().iterator();
//            while (iterator.hasNext()) {
//                Map.Entry<String, ?> next = iterator.next();
//                String key = next.getKey();
//                Object value = next.getValue();
//                if (value != null) {
//                    cookieManager.setCookie(key, value.toString());
//                }
//            }
//        }
        CookieSyncManager.getInstance().sync();


        //REALLY? Nobody has seen this UNTIL NOW?
//        cookieManager.setAcceptFileSchemeCookies(true);

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            cookieManager.setAcceptThirdPartyCookies(webView, true);
        }
    }

    public void setCookiesEnabled(boolean accept) {
        cookieManager.setAcceptCookie(accept);
    }

    public void setCookie(final String url, final String value) {
        cookieManager.setCookie(url, value);
    }

    public String getCookie(final String url) {
        return cookieManager.getCookie(url);
    }

    public void clearCookies() {
        cookieManager.removeAllCookie();
    }

    public void flush() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            cookieManager.flush();
        }
    }
};
