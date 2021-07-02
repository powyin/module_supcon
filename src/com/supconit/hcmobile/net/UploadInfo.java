package com.supconit.hcmobile.net;

import io.reactivex.subjects.PublishSubject;

/**
 * Created by powyin on 2018/7/6.
 */

public final class UploadInfo implements Cloneable {


    // 上传内容 1:File 2:Bitmap 3:OutputStream
    public Object localFilePath;

    // 上传文件的状态
    public NetState status;

    // 上传文件的远程地址
    public String fileName;

    // 上传进度监听
    public PublishSubject<Progress> progress = PublishSubject.create();

    @Override
    public UploadInfo clone() {
        try {
            return (UploadInfo) super.clone();
        } catch (Exception ignored) {
        }
        return null;
    }
}
