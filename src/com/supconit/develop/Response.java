package com.supconit.develop;

import org.apache.cordova.CallbackContext;
import org.apache.cordova.PluginResult;
import org.json.JSONArray;
import org.json.JSONObject;

public final class Response {
    private final CallbackContext callbackContext;
    private boolean mKeepConnect = false;

    public Response(CallbackContext callbackContext){
        this.callbackContext = callbackContext;
    }
    public void success(JSONObject message) {
        PluginResult result = new PluginResult(PluginResult.Status.OK, message);
        result.setKeepCallback(mKeepConnect);
        callbackContext.sendPluginResult(result);
    }

    public void success(Boolean b) {
        PluginResult result = new PluginResult(PluginResult.Status.OK, b);
        result.setKeepCallback(mKeepConnect);
        callbackContext.sendPluginResult(result);
    }

    public void success(float f) {
        PluginResult result = new PluginResult(PluginResult.Status.OK, f);
        result.setKeepCallback(mKeepConnect);
        callbackContext.sendPluginResult(result);
    }

    public void success(double d) {
        PluginResult result = new PluginResult(PluginResult.Status.OK, d);
        result.setKeepCallback(mKeepConnect);
        callbackContext.sendPluginResult(result);
    }


    public void success(String message) {
        PluginResult result = new PluginResult(PluginResult.Status.OK, message);

        callbackContext.sendPluginResult(result);
    }

    public void success(JSONArray message) {
        PluginResult result = new PluginResult(PluginResult.Status.OK, message);
        result.setKeepCallback(mKeepConnect);
        callbackContext.sendPluginResult(result);
    }


    public void success(int message) {
        PluginResult result = new PluginResult(PluginResult.Status.OK, message);
        result.setKeepCallback(mKeepConnect);
        callbackContext.sendPluginResult(result);
    }

    public void success() {
        PluginResult result = new PluginResult(PluginResult.Status.OK);
        result.setKeepCallback(mKeepConnect);
        callbackContext.sendPluginResult(result);
    }

    public void error(JSONObject message) {
        PluginResult result = new PluginResult(PluginResult.Status.ERROR, message);
        callbackContext.sendPluginResult(result);
    }

    public void error(String message) {
        PluginResult result = new PluginResult(PluginResult.Status.ERROR, message);
        callbackContext.sendPluginResult(result);
    }

    public void error(int message) {
        PluginResult result = new PluginResult(PluginResult.Status.ERROR, message);
        callbackContext.sendPluginResult(result);
    }

}
