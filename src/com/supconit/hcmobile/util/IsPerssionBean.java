package com.supconit.hcmobile.util;

import java.io.Serializable;

public class IsPerssionBean implements Serializable {

    /**
     * code : 100
     * resultDes : null
     * result : true
     */

    private int code;
    private Object resultDes;
    private boolean result;

    public int getCode() {
        return code;
    }

    public void setCode(int code) {
        this.code = code;
    }

    public Object getResultDes() {
        return resultDes;
    }

    public void setResultDes(Object resultDes) {
        this.resultDes = resultDes;
    }

    public boolean isResult() {
        return result;
    }

    public void setResult(boolean result) {
        this.result = result;
    }
}
