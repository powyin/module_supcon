package com.supconit.develop;

import android.app.Activity;
import android.app.Application;

import org.json.JSONArray;

public interface JSBridge {

    // todo use JSApplicationCreate
    // app启动运行方法 可以替换成 JSApplicationCreate 同等实现
    void onApplicationCreate(Application application);

    // js通道
    void execute(String action, JSONArray args, Response callbackContext);

    void onActivityCreate(Activity activity);

    void onActivityPase(Activity activity);

    void onActivityDestory(Activity activity);


}
