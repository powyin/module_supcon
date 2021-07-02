package com.supconit.hcmobile.net;

/**
 * 下载上传进度
 */
public final class Progress implements Cloneable {

    // 单文件的进度 (下载或者上传)
    public float progress;
    // 多文件时 其值是多个文件的总进度 (下载或者上传)
    public float progressTootle;

    @Override
    public Progress clone() {
        try {
            return (Progress) super.clone();
        } catch (Exception ignored) {
        }
        return null;

    }
}
