package com.supconit.hcmobile.plugins.file;
import android.util.SparseArray;

import org.apache.cordova.CallbackContext;

import java.io.Serializable;

/**
 * Holds pending runtime permission requests
 */
class PendingRequests implements Serializable {
    private int currentReqId = 0;
    private SparseArray<Request> requests = new SparseArray<Request>();

    /**
     * Creates a request and adds it to the array of pending requests. Each created request gets a
     * unique result code for use with requestPermission()
     * @param rawArgs           The raw arguments passed to the plugin
     * @param action            The action this request corresponds to (get file, etc.)
     * @param callbackContext   The CallbackContext for this plugin call
     * @return                  The request code that can be used to retrieve the Request object
     */
    public synchronized int createRequest(String rawArgs, int action, CallbackContext callbackContext)  {
        Request req = new Request(rawArgs, action, callbackContext);
        requests.put(req.requestCode, req);
        return req.requestCode;
    }

    /**
     * Gets the request corresponding to this request code and removes it from the pending requests
     * @param requestCode   The request code for the desired request
     * @return              The request corresponding to the given request code or null if such a
     *                      request is not found
     */
    public synchronized Request getAndRemove(int requestCode) {
        Request result = requests.get(requestCode);
        requests.remove(requestCode);
        return result;
    }

    /**
     * Holds the options and CallbackContext for a call made to the plugin.
     */
    public class Request {

        // Unique int used to identify this request in any Android permission callback
        private int requestCode;

        // Action to be performed after permission request result
        private int action;

        // Raw arguments passed to plugin
        private String rawArgs;

        // The callback context for this plugin request
        private CallbackContext callbackContext;

        private Request(String rawArgs, int action, CallbackContext callbackContext) {
            this.rawArgs = rawArgs;
            this.action = action;
            this.callbackContext = callbackContext;
            this.requestCode = currentReqId ++;
        }

        public int getAction() {
            return this.action;
        }

        public String getRawArgs() {
            return rawArgs;
        }

        public CallbackContext getCallbackContext() {
            return callbackContext;
        }
    }
}
