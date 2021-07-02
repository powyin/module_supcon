package com.supconit.hcmobile.net;

import java.io.File;

import io.reactivex.subjects.PublishSubject;
import okhttp3.Headers;

public final class DownInfo implements Cloneable {

    // 下载进度监听
    public PublishSubject<Progress> progress = PublishSubject.create();

    // 用于扩展携带其他参数
    public Object what;

    // 下载完成情况  START必回调一次 请在回调时注册进度监听; ERROR | FINISH 回调其中的一种;
    public NetState status;
    // 下载异常信息
    public Exception exception;

    // 下载文件的 http地址
    public String remoteUrl;
    // 下载文件的本地文件路径
    public File localFilePath;
    // 下载文件的 header
    public Headers responseHeader;

    @Override
    public DownInfo clone() {
        try {
            return (DownInfo) super.clone();
        } catch (Exception ignored) {
        }
        return null;
    }
}
