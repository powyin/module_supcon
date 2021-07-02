package org.apache.cordova;

import android.util.Log;

import org.json.JSONArray;
import org.json.JSONException;

public class MissPlugin extends CordovaPlugin {
    public MissPlugin(String missClassName) {
        this.mMissClassName = missClassName;
    }
    private String mMissClassName;
    @Override
    public boolean execute(String action, JSONArray args, CallbackContext callbackContext) {
        callbackContext.error("missClass for newInstance this is empty  " + mMissClassName);
        Log.d("powyin", "missClass for newInstance this is empty  " + mMissClassName);
        return false;
    }
}
