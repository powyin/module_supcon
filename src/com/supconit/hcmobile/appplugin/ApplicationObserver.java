package com.supconit.hcmobile.appplugin;

import android.app.Application;
import android.content.Context;

/**
 * Created by yanfei on 2018-4-9.
 */

public abstract class ApplicationObserver {
    private Application applicationContext;
    public void setContext(Application context) {
        applicationContext = context;
    }
    protected Application getApplicationContext() {
        return applicationContext;
    }
    public void onCreate() {}
}
