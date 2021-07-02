package com.supconit.hcmobile;

import android.app.Application;

public class Ap extends Application {

    @Override
    public void onCreate() {
        super.onCreate();
        new HcmobileApp().init(this, null);
    }
}
