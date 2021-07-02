package org.apache.cordova.exEngine;

import android.view.View;
import android.view.ViewGroup;

import org.apache.cordova.CordovaWebView;

public interface ExSystemWebView {

    void loadUrl(String js);

    String getUrl();
    String getTitle();

    CordovaWebView getCordovaWebView();

    ViewGroup getParentGroup();

}
