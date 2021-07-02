package com.supconit.hcmobile.net;

import android.content.Context;
import android.content.SharedPreferences;
import android.graphics.Bitmap;
import android.net.Uri;
import android.support.annotation.NonNull;
import android.text.TextUtils;
import android.util.Log;
import com.google.gson.Gson;
import com.supconit.hcmobile.HcmobileApp;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.UUID;
import java.util.concurrent.TimeUnit;
import io.reactivex.Observable;
import io.reactivex.ObservableEmitter;
import io.reactivex.ObservableOnSubscribe;
import io.reactivex.Single;
import io.reactivex.SingleEmitter;
import io.reactivex.SingleOnSubscribe;
import io.reactivex.functions.Function;
import io.reactivex.schedulers.Schedulers;
import io.reactivex.subjects.PublishSubject;
import io.reactivex.subjects.Subject;
import okhttp3.CacheControl;
import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.MediaType;
import okhttp3.MultipartBody;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;
import okhttp3.ResponseBody;


/**
 * Created by powyin on 2018/7/1.
 */

public final class HttpManager {
    private static Boolean hasClearCacheFile = false;
    private final static Object cacheFileLock = new Object();

    private static OkHttpClient mJsonClient = new OkHttpClient.Builder()
            .connectTimeout(5, TimeUnit.SECONDS)
            .addInterceptor(new InterceptorLog(InterceptorLog.Level.BODY))
            .build();

    private static OkHttpClient mFileClient = new OkHttpClient.Builder()
            .connectTimeout(10, TimeUnit.SECONDS)
            .readTimeout(10, TimeUnit.SECONDS)
            .writeTimeout(10, TimeUnit.SECONDS)
            .build();



    /**
     * 单文件下载 存储为file
     */
    public static Observable<DownInfo> fileDownLoad(String url, File saveTo) {
        if (TextUtils.isEmpty(url)) {
            return Observable.empty();
        }
        if (saveTo == null) {
            saveTo = PrivateUtil.getRandomFilePath(HcmobileApp.getApplication(), null, null, false);
        }
        return fileDownLoad(Collections.singletonMap(url, saveTo));
    }


    /**
     * 单文件下载 存储为file 自动进行缓存 cacheTime单位为小时 最长为10天
     * 同一个url 同时进行多份下载时：最后完成的下载绝对磁盘保留
     */
    public static Observable<DownInfo> fileDownLoad(String url, long cacheTime_hour) {
        if (TextUtils.isEmpty(url)) {
            return Observable.empty();
        }
        if (cacheTime_hour < 0) {
            cacheTime_hour = 10 * 24;
        }

        final SharedPreferences record = HcmobileApp.getApplication().getSharedPreferences("HttpManager_save_cache_file_recorad", Context.MODE_PRIVATE);

        // todo 清除过期文件 否者缓存文件将会越来越多无法删除
        if (!hasClearCacheFile) {
            hasClearCacheFile = true;
            Map<String, ?> all = record.getAll();
            List<String> list = all == null ? Collections.emptyList() : new ArrayList<>(all.keySet());
            synchronized (cacheFileLock) {
                for (String key : list) {
                    if (key.startsWith("saveTime_")) {
                        long time = record.getLong(key, 0);
                        if (System.currentTimeMillis() - time >= 10 * 24 * 60 * 60 * 1000) {
                            String filePath = record.getString(key.substring("saveTime_".length()), null);
                            if (filePath != null) {
                                PrivateUtil.deleteFile(new File(filePath));
                            }
                            record.edit().remove(key).remove(key.substring("saveTime_".length())).apply();
                        }
                    }
                }
            }
        }

        String savePath = record.getString(url, null);
        long saveTime = record.getLong("saveTime_" + url, 0);
        final File saveFile = !TextUtils.isEmpty(savePath) ? new File(savePath) : null;

        if (saveFile != null && saveFile.exists() && saveFile.length() > 0 && ((saveTime + cacheTime_hour * 60 * 60 * 1000 - System.currentTimeMillis()) > 0)) {
            return Observable.create(new ObservableOnSubscribe<DownInfo>() {
                @Override
                public void subscribe(ObservableEmitter<DownInfo> emitter) throws Exception {
                    DownInfo info = new DownInfo();
                    info.status = NetState.START;
                    info.localFilePath = saveFile;
                    info.remoteUrl = url;
                    emitter.onNext(info);
                    info = info.clone();
                    assert info != null;
                    info.status = NetState.FINISH;
                    emitter.onNext(info);
                    emitter.onComplete();
                }
            });
        } else {
            File randomFilePath = PrivateUtil.getRandomFilePath(HcmobileApp.getApplication(), null, PrivateUtil.getFileNameFromUrl(url)[1], false);
            return fileDownLoad(Collections.singletonMap(url, randomFilePath)).map(new Function<DownInfo, DownInfo>() {
                @Override
                public DownInfo apply(DownInfo downInfo) throws Exception {
                    if (downInfo.status == NetState.FINISH) {
                        synchronized (cacheFileLock) {
                            // todo 遗留文件都不敢清除  可能被使用 缓存一波 等下次程序启动的时候再自动清除;
                            String filePath = record.getString(url, null);
                            if (!TextUtils.isEmpty(filePath)) {
                                String temUrl = url + UUID.randomUUID().toString();
                                record.edit()
                                        .putString(temUrl, filePath)
                                        .putLong("saveTime_" + temUrl, 0)
                                        .apply();
                            }
                            record.edit()
                                    .putString(url, downInfo.localFilePath.getAbsolutePath())
                                    .putLong("saveTime_" + url, System.currentTimeMillis())
                                    .apply();
                        }
                    }
                    return downInfo;
                }
            });
        }
    }

    /**
     * 多文件下载 存储为file
     */
    public static Observable<DownInfo> fileDownLoad(final Map<String, File> RemoteUrlAndLocalFilePair) {
        if (RemoteUrlAndLocalFilePair == null || RemoteUrlAndLocalFilePair.isEmpty()) {
            return Observable.empty();
        }
        final HashMap<String, DownInfo> setDownInfo = new HashMap<String, DownInfo>();
        final Set<Progress> progressSet = new HashSet<>();
        for (Map.Entry<String, File> entry : RemoteUrlAndLocalFilePair.entrySet()) {
            String key = entry.getKey();
            File value = entry.getValue();
            value = value == null ? PrivateUtil.getRandomFilePath(HcmobileApp.getApplication(), "", null, false) : value;
            DownInfo info = new DownInfo();
            info.remoteUrl = key;
            info.localFilePath = value;
            info.status = NetState.START;
            setDownInfo.put(info.remoteUrl, info);
        }

        return Observable.create(new ObservableOnSubscribe<DownInfo>() {
            @Override
            public void subscribe(final ObservableEmitter<DownInfo> emitter) throws Exception {

                for (final Map.Entry<String, DownInfo> entry : setDownInfo.entrySet()) {
                    final Progress progress = new Progress();
                    final String url = entry.getKey();
                    final DownInfo downFile = entry.getValue();

                    progressSet.add(progress);
                    emitter.onNext(downFile.clone());

                    Request.Builder builder = new Request.Builder();
                    builder.cacheControl(CacheControl.FORCE_NETWORK);
                    builder.get().url(url);
                    mFileClient.newCall(builder.build()).enqueue(new Callback() {
                        @Override
                        public void onFailure(@NonNull Call call, IOException err) {
                            err.printStackTrace();
                            downFile.status = NetState.ERROR;
                            downFile.exception = err;
                            DownInfo clone = downFile.clone();
                            assert clone != null;
                            emitter.onNext(clone);
                            checkUploadAll();
                        }

                        @Override
                        public void onResponse(@NonNull Call call, @NonNull Response response) throws IOException {
                            Exception ioException = null;
                            ResponseBody body = null;
                            try {
                                downFile.responseHeader = response.headers();
                                body = response.body();
                                InputStream inputStream = body.byteStream();
                                long sunLen = body.contentLength();

                                PrivateUtil.ensureFileExist(downFile.localFilePath, false);
                                FileOutputStream outPut = new FileOutputStream(downFile.localFilePath);
                                byte[] buffer = new byte[1024 * 16 * 16];

                                if (sunLen > 0) {
                                    // todo 可以获取下载总大小
                                    long consume = sunLen;
                                    float oldProgress = 0;
                                    while (consume > 0) {
                                        long temRead = inputStream.read(buffer);
                                        outPut.write(buffer, 0, (int) temRead);

                                        progress.progress += 1f * temRead / sunLen;
                                        if (progress.progress - oldProgress > 0.08f) {
                                            oldProgress = progress.progress;
                                            progress(downFile);
                                        }

                                        if (temRead == -1) break;
                                        consume -= temRead;
                                    }
                                } else {
                                    // todo 未知下载总大小
                                    long read = inputStream.read(buffer);
                                    while (read >= 0) {
                                        if (read > 0) {
                                            outPut.write(buffer, 0, (int) read);
                                        }
                                        read = inputStream.read(buffer);
                                    }
                                }
                                progress.progress = 1;
                                progress(downFile);
                                downFile.progress.onComplete();

                            } catch (Exception exc) {
                                ioException = exc;
                                ioException.printStackTrace();
                            } finally {
                                if (ioException == null) {
                                    downFile.status = NetState.FINISH;
                                } else {
                                    downFile.status = NetState.ERROR;
                                    downFile.exception = ioException;
                                    ioException.printStackTrace();
                                }
                                DownInfo clone = downFile.clone();
                                assert clone != null;
                                emitter.onNext(clone);

                                if (body != null) {
                                    try {
                                        body.close();
                                    } catch (Exception e) {
                                        e.printStackTrace();
                                    }
                                }
                                checkUploadAll();
                            }
                        }

                        // todo 计算下载进度
                        private void progress(DownInfo downFile) {
                            float count = 1f * setDownInfo.size();
                            float pro = 0;
                            for (Progress info : progressSet) {
                                pro += info.progress / count;
                            }
                            progress.progressTootle = pro;
                            Progress clone = progress.clone();
                            assert clone != null;
                            downFile.progress.onNext(clone);
                        }

                        // todo 检查下载是否全部结束
                        private void checkUploadAll() {
                            boolean over = true;
                            for (DownInfo info : setDownInfo.values()) {
                                over = over && info.status != NetState.START;
                            }
                            if (over) {
                                emitter.onComplete();
                            }
                        }

                    });
                }
            }
        });
    }


    /**
     * 多文件下载 存储为file 同步方法
     */
    public static List<DownInfo> fileDownLoadAsy(final Map<String, File> RemoteUrlAndLocalFilePair) {

        if (RemoteUrlAndLocalFilePair == null || RemoteUrlAndLocalFilePair.isEmpty()) {
            return null;
        }

        final HashMap<String, DownInfo> setDownInfo = new HashMap<String, DownInfo>();
        for (Map.Entry<String, File> entry : RemoteUrlAndLocalFilePair.entrySet()) {
            String key = entry.getKey();
            File value = entry.getValue();
            value = value == null ? PrivateUtil.getRandomFilePath(null, "", null, false) : value;
            DownInfo info = new DownInfo();
            info.remoteUrl = key;
            info.localFilePath = value;
            info.status = NetState.START;
            setDownInfo.put(info.remoteUrl, info);
        }


        for (final Map.Entry<String, DownInfo> entry : setDownInfo.entrySet()) {
            final String url = entry.getKey();
            final DownInfo downFile = entry.getValue();

            Request.Builder builder = new Request.Builder();
            builder.cacheControl(CacheControl.FORCE_NETWORK);
            builder.get().url(url);

            Exception ioException = null;
            ResponseBody body = null;
            try {
                Response response = mFileClient.newCall(builder.build()).execute();
                downFile.responseHeader = response.headers();
                body = response.body();
                InputStream inputStream = body.byteStream();
                long sunLen = body.contentLength();

                PrivateUtil.ensureFileExist(downFile.localFilePath, false);
                FileOutputStream outPut = new FileOutputStream(downFile.localFilePath);
                byte[] buffer = new byte[1024 * 16 * 8];

                if (sunLen > 0) {
                    // todo 可以获取下载总大小
                    long consume = sunLen;
                    while (consume > 0) {
                        long temRead = inputStream.read(buffer);
                        outPut.write(buffer, 0, (int) temRead);

                        if (temRead == -1) break;
                        consume -= temRead;
                    }
                    downFile.progress.onComplete();
                } else {
                    // todo 未知下载总大小
                    long read = inputStream.read(buffer);
                    while (read >= 0) {
                        if (read > 0) {
                            outPut.write(buffer, 0, (int) read);
                        }
                        read = inputStream.read(buffer);
                    }

                }

            } catch (Exception exc) {
                ioException = exc;
                ioException.printStackTrace();
            } finally {
                if (ioException == null) {
                    downFile.status = NetState.FINISH;
                } else {
                    downFile.status = NetState.ERROR;
                    downFile.exception = ioException;
                }
                if (body != null) {
                    body.close();
                }
            }
        }
        return new ArrayList<>(setDownInfo.values());
    }


    /**
     * http get 请求
     *
     * @param url       http://baidu.com/getInfo;
     * @param getPara   {"limit":"2","pageSize":"3","id":"3242342342342"} 拼接:_url_?limit=2&pageSize=3&id=3242342342342
     * @param dataClass 返回结果转换类型 if (dataClass == null || dataClass == String.class) 直接返回respon.string();
     */
    public static <T> Single<T> get(String url, Map<String, String> getPara, final Class<T> dataClass) {
        return _executeHttp(url, "GET", null, null, getPara, dataClass);
    }
    public static <T> Single<T> getHb(String url, Map<String, String> header, Map<String, String> getPara, final Class<T> dataClass) {
        return _executeHttp(url, "GET", header, null, getPara,dataClass);
    }

    public static <T> Single<T> getHead(String url, Map<String, String> header, final Class<T> dataClass) {
        return _executeHttp(url, "GET", header, null,null , dataClass);
    }
    /**
     * post body类型:application/x-www-form-urlencoded; 组成形式:limit=22&id=211243232
     */
    public static <T> Single<T> post(String url, Map<String, String> x_www_form_urlencoded_para, Class<T> dataClass) {
        RequestBody requestBody = RequestBodyBuilder.build_x_www_form_urlencoded(x_www_form_urlencoded_para);
        return _executeHttp(url, "POST", null, requestBody, null, dataClass);
    }

    /**
     * post body类型:application/x-www-form-urlencoded; 组成形式:limit=22&id=211243232
     */
    public static Single<String> post(String url, Map<String, String> x_www_form_urlencoded_para) {
        RequestBody requestBody = RequestBodyBuilder.build_x_www_form_urlencoded(x_www_form_urlencoded_para);
        return _executeHttp(url, "POST", null, requestBody, null, String.class);
    }

    /**
     * post body类型:application/x-www-form-urlencoded; 组成形式:limit=22&id=211243232
     */
    public static Single<String> post(String url, Map<String, String> headers, Map<String, String> x_www_form_urlencoded_para) {
        RequestBody requestBody = RequestBodyBuilder.build_x_www_form_urlencoded(x_www_form_urlencoded_para);
        return _executeHttp(url, "POST", headers, requestBody, null, String.class);
    }

    /**
     * post body类型:application/json; 组成形式:{"limit":333,"id":"erewrwer"}
     */
    public static <T> Single<T> postJson(String url, Object jsonData, Class<T> dataClass) {
        RequestBody requestBody = RequestBodyBuilder.build_json(jsonData);
        return _executeHttp(url, "POST", null, requestBody, null, dataClass);
    }

    /**
     * @param url       http://baidu.com/getInfo;
     * @param method    get post ...
     * @param header    {"cookie":"dfdfkask","token":"9949340342"}
     * @param body      just body
     * @param getPara   {"limit":"2","pageSize":"3","id":"3242342342342"} 拼接: _url_?limit=2&pageSize=3&id=3242342342342
     * @param dataClass 返回结果转换类型 if (dataClass == null || dataClass == String.class) 直接返回respon.string();
     */
    public static <T> Single<T> _executeHttp(String url, String method, Map<String, String> header, RequestBody body, Map<String, String> getPara, final Class<T> dataClass) {

        final Request.Builder builder = new Request.Builder();
        if (url == null || url.length() == 0) {
            throw new RuntimeException("http execute miss url");
        }

        // todo 拼接url与query参数
        StringBuilder stringBuilder = new StringBuilder();
        stringBuilder.append(url);
        stringBuilder.append("?");
        if (getPara != null) {
            for (Map.Entry<String, String> entry : getPara.entrySet()) {
                stringBuilder.append(Uri.encode(entry.getKey()));
                stringBuilder.append("=");
                stringBuilder.append(Uri.encode(entry.getValue()));
                stringBuilder.append("&");
            }
        }
        stringBuilder.setLength(stringBuilder.length() - 1);
        url = stringBuilder.toString();
        Log.d("powyin_http_url", url);
        builder.url(url);

        // todo 添加http头信息
        if (header != null) {
            for (Map.Entry<String, String> entry : header.entrySet()) {
                builder.addHeader(entry.getKey(), entry.getValue());
            }
        }

        // todo http方法
        method = method != null ? method : "get";
        switch (method) {
            case "POST":
            case "PUT":
            case "PATCH":
            case "PROPPATCH":
            case "REPORT":
                if (body != null) {
                    builder.method(method, body);
                } else {
                    throw new RuntimeException("http execute method  mis" + method + "sing body");
                }
                break;
            case "GET":
            default:
                builder.get();
                break;
        }

        // todo 构建request
        return Single.create(new SingleOnSubscribe<T>() {
            @SuppressWarnings("unchecked")
            @Override
            public void subscribe(SingleEmitter<T> emitter) throws Exception {
                Response execute = mJsonClient.newCall(builder.build()).execute();
                String value = execute.body().string();
                if (dataClass != null && dataClass != String.class) {
                    Gson gson = new Gson();
                    T data = gson.fromJson(value, dataClass);
                    emitter.onSuccess(data);
                } else {
                    emitter.onSuccess((T) value);
                }
            }
        }).subscribeOn(Schedulers.io());
    }


    // -----------------------------------------------------------------------------------------------------------------------------------------------------//


    /**
     * 上传 使用multiple/format_data
     */
    public static Observable<UploadInfo> upload(String uri, Map<String, String> paras, Map<String, Object> files) {

        if (mFileClient == null) {
            mFileClient = new OkHttpClient.Builder()
                    .connectTimeout(10, TimeUnit.SECONDS)
                    .build();
        }

        final HashSet<UploadInfo> setInfo = new HashSet<>();
        for (HashMap.Entry<String, Object> entry : files.entrySet()) {
            UploadInfo info = new UploadInfo();
            info.fileName = entry.getKey();
            info.localFilePath = entry.getValue();
            info.status = NetState.START;
            setInfo.add(info);
            if (info.localFilePath == null) return null;
        }
        final Subject<UploadInfo> subject = PublishSubject.create();

        for (final UploadInfo info : setInfo) {
            MultipartBody.Builder multipartBody = new MultipartBody.Builder();
            if (paras != null && paras.size() > 0) {
                for (Map.Entry<String, String> entry : paras.entrySet()) {
                    multipartBody.addFormDataPart(entry.getKey(), entry.getValue());
                }
            }


            Object object = info.localFilePath;
            if (object instanceof Bitmap) {
                RequestBody body = RequestBodyBuilder.build_bitmap((Bitmap) object);
                multipartBody.addFormDataPart("file", info.fileName, body);
            } else if (object instanceof File) {
                multipartBody.addFormDataPart("file", info.fileName, RequestBody.create(MediaType.parse("image/png"), (File) object));
            } else {
                throw new RuntimeException("upload data type not support : " + info.getClass());
            }


            multipartBody.setType(MultipartBody.FORM);

//            ProgressDelegate delegate = new ProgressDelegate(info, multipartBody.build(), subject);
            RequestBody delegate = RequestBodyBuilder.buildProgress(multipartBody.build(), info.progress);
            Request.Builder builder = new Request.Builder();
            Request request = builder.url(uri).post(delegate).build();

            mJsonClient.newCall(request).enqueue(new Callback() {
                @Override
                public void onFailure(Call call, IOException e) {
                    info.status = NetState.ERROR;
                    subject.onNext(info);
                }

                @SuppressWarnings("unchecked")
                @Override
                public void onResponse(Call call, Response response) throws IOException {
                    try {
                        ResponseBody body = response.body();
//                        if (body != null) {
//                            info.json = body.string();
//                        }
                        info.status = NetState.FINISH;
                        subject.onNext(info);
                    } catch (Exception e) {
                        e.printStackTrace();
                    } finally {
                        checkUploadAll();
                    }
                }

                private void checkUploadAll() {
                    boolean over = true;
                    for (UploadInfo info : setInfo) {
                        over = over && info.status != NetState.START;
                    }
                    if (over) {
                        subject.onComplete();
                    }
                }
            });
        }

        return subject;
    }
    //单文件上传
    public static String uploadCs(String url, String filePath) throws IOException {
        OkHttpClient client = new OkHttpClient();
        RequestBody requestBody = new MultipartBody.Builder()
                .setType(MultipartBody.FORM)
                .addFormDataPart("file", new File(filePath).getName(),
                        RequestBody.create(MediaType.parse("multipart/form-data"), new File(filePath)))
                .build();

        Request request = new Request.Builder()
                .header("Authorization", "Client-ID " + UUID.randomUUID())
                .url(url)
                .post(requestBody)
                .build();
        Response response=client.newCall(request).execute();
        return response.body().string();
    }

}

