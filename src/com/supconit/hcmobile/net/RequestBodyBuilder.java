package com.supconit.hcmobile.net;

import android.graphics.Bitmap;

import com.google.gson.Gson;

import org.json.JSONArray;
import org.json.JSONObject;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.IOException;
import java.util.Map;

import io.reactivex.subjects.Subject;
import okhttp3.FormBody;
import okhttp3.MediaType;
import okhttp3.MultipartBody;
import okhttp3.RequestBody;
import okio.Buffer;
import okio.BufferedSink;
import okio.ForwardingSink;
import okio.Okio;
import okio.Sink;

/**
 * 用于构建转换requestBody
 */
public class RequestBodyBuilder {


    /**
     * 构建 Content-Type: application/x-www-form-urlencoded 样式请求
     */
    static RequestBody build_x_www_form_urlencoded(Map<String, String> para) {
        if (para == null) {
            return RequestBody.create(MediaType.parse("application/x-www-form-urlencoded"), "");
        }
        FormBody.Builder formBody = new FormBody.Builder();
        for (Map.Entry<String, String> entry : para.entrySet()) {
            formBody.add(entry.getKey(), entry.getValue());
        }
        return formBody.build();
    }


    /**
     * 构建 Content-Type: application/json 样式请求
     */
    static RequestBody build_json(Object jsonObj) {
        if (jsonObj == null) {
            return null;
        }
        if (jsonObj instanceof JSONObject || jsonObj instanceof JSONArray) {
            return RequestBody.create(MediaType.parse("application/json"), jsonObj.toString());
        }
        try {
            Gson gson = new Gson();
            String json = gson.toJson(jsonObj);
            return RequestBody.create(MediaType.parse("application/json"), json);
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }


    /**
     * 构建 Content-Type: multipart/form-data 样式请求
     */
    static RequestBody build_multipart_form_data(Map<String, Object> objectMap) {
        if (objectMap == null) {
            return RequestBody.create(MediaType.parse("multipart/form-data"), "");
        }
        MultipartBody.Builder builder = new MultipartBody.Builder();
        for (Map.Entry<String, Object> entry : objectMap.entrySet()) {
            String key = entry.getKey();
            Object value = entry.getValue();
            if (value == null || key == null) {
                continue;
            }
            if (value instanceof CharSequence) {
                builder.addFormDataPart(key, value.toString());
            } else if (value instanceof File) {
                builder.addFormDataPart(key, key, build_file((File) value));
            } else if (value instanceof Bitmap) {
                builder.addFormDataPart(key, key, build_bitmap((Bitmap) value));
            } else {
                throw new RuntimeException("unsupport data format to convert Body" + value.getClass());
            }
        }
        return builder.build();
    }

    /**
     * 转换bitmap to requestBody
     */
    static RequestBody build_bitmap(Bitmap bitmap) {
        ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
        bitmap.compress(Bitmap.CompressFormat.PNG, 60, outputStream);
        byte[] bytes = outputStream.toByteArray();
        try {
            outputStream.close();
        } catch (Exception e) {
            e.printStackTrace();
        }
        MediaType mediaType = MediaType.parse("image/png");
        return RequestBody.create(mediaType, bytes);
    }

    /**
     * 转换file to requestBody
     */
    static RequestBody build_file(File file) {
        String[] fileNameFromUrl = PrivateUtil.getFileNameFromUrl(file.getAbsolutePath());
        String urlSuffixName = fileNameFromUrl[1];
        if (urlSuffixName != null && urlSuffixName.length() > 0) {
            switch (urlSuffixName) {
                case "json":
                    return RequestBody.create(MediaType.parse("application/json"), file);
                case "xml":
                    return RequestBody.create(MediaType.parse("text/xml"), file);
                case "html":
                    return RequestBody.create(MediaType.parse("text/html"), file);
                case "zip":
                    return RequestBody.create(MediaType.parse("application/zip"), file);
                case "pdf":
                    return RequestBody.create(MediaType.parse("application/pdf"), file);
                case "doc":
                    return RequestBody.create(MediaType.parse("application/msword"), file);// (.doc)
                case "docx":
                    return RequestBody.create(MediaType.parse("application/vnd.openxmlformats-officedocument.wordprocessingml.document"), file); //(.docx)
                case "xls":
                    return RequestBody.create(MediaType.parse("application/vnd.ms-excel"), file); //(.xls)
                case "xlsx":
                    return RequestBody.create(MediaType.parse("application/vnd.openxmlformats-officedocument.spreadsheetml.sheet"), file);// (.xlsx)
                case "ppt":
                    return RequestBody.create(MediaType.parse("application/vnd.ms-powerpoint"), file);// (.ppt)
                case "pptx":
                    return RequestBody.create(MediaType.parse("application/vnd.openxmlformats-officedocument.presentationml.presentation"), file);// (.pptx)
                case "odt":
                    return RequestBody.create(MediaType.parse("application/vnd.oasis.opendocument.text"), file);// (.odt)
                case "css":
                    return RequestBody.create(MediaType.parse("text/css"), file);
                case "txt":
                    return RequestBody.create(MediaType.parse("text/plain"), file);
                case "png":
                    return RequestBody.create(MediaType.parse("image/png"), file);
                case "jpeg":
                    return RequestBody.create(MediaType.parse("image/jpeg"), file);
                case "gif":
                    return RequestBody.create(MediaType.parse("image/gif"), file);
                case "hls":
                case "mp4":
                case "flv":
                case "avi":
                case "3gp":
                case "3gpp":
                case "webm":
                case "ts":
                case "ogv":
                case "m3u8":
                case "asf":
                case "wmv":
                case "rmvb":
                case "rm":
                case "f4v":
                case "dat":
                case "mov":
                case "mpg":
                case "mkv":
                case "mpeg":
                case "mpeg1":
                case "mpeg2":
                case "mpeg3":
                case "xvid":
                case "dvd":
                case "vob":
                case "divx":
                    return RequestBody.create(MediaType.parse("audio/mpeg"), file);
            }
            return RequestBody.create(MediaType.parse("image/jpeg"), file);
        } else {
            return RequestBody.create(MediaType.parse("image/jpeg"), file);
        }
    }


    /**
     * 包裹requestBody;  计算它的读取流进度 代理上传进度监听
     */
    static RequestBody buildProgress(final RequestBody requestBody, final Subject<Progress> publish) {
        return new RequestBody() {
            private Progress mProgress = new Progress();

            @Override
            public MediaType contentType() {
                return requestBody.contentType();
            }

            @Override
            public long contentLength() {
                try {
                    return requestBody.contentLength();
                } catch (IOException e) {
                    e.printStackTrace();
                }
                return -1;
            }

            @Override
            public void writeTo(BufferedSink sink) throws IOException {
                CountingSink mCountingSink = new CountingSink(sink);
                BufferedSink bufferedSink = Okio.buffer(mCountingSink);
                requestBody.writeTo(bufferedSink);
                bufferedSink.flush();
            }

            final class CountingSink extends ForwardingSink {
                private long bytesWritten = 0;
                private int currentProgress = 0;

                private CountingSink(Sink delegate) {
                    super(delegate);
                }

                @Override
                public void write(Buffer source, long byteCount) throws IOException {
                    long per = Math.max(1024, contentLength() / 20);
                    while (byteCount > 0) {
                        long readCount = Math.min(per, byteCount);
                        super.write(source, readCount);
                        bytesWritten += readCount;
                        int progress = (int) (100F * bytesWritten / contentLength() / 5);

                        if (currentProgress != progress) {
                            currentProgress = progress;
                            mProgress.progress = 5 * currentProgress;
                            publish.onNext(mProgress);
                        }
                        byteCount -= readCount;
                    }
                }
            }
        };
    }


}

