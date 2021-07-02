package com.supconit.hcmobile.plugins.file;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Matrix;
import android.media.ThumbnailUtils;
import android.net.Uri;
import android.os.Environment;
import android.text.TextUtils;
import android.util.DisplayMetrics;
import android.util.Log;
import android.webkit.MimeTypeMap;

import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.Locale;


/**
 * 图片处理类
 * @author lilinlin
 * date 2016/06/14
 */
public class BitmapUtil {
    public static final int ACTION_IMAGE_CAPTURE = 0x000001;
    public static final int ACTION_ALBUM= 0x0000002;
    public static final int IMAGE_DELETE = 0X0000003;
    public static final int ALBUM_OPEN = 0x000004;
    public static final int IMAGE_SELECTED = 0X0007;
    public static final int IMAGE_SELECTED_BACK = 0X0008;

    /**
     * 图片url字符串路径转换为bitmap
     * @param urlStr 图片url字符串
     * @return 对应图片
     */
    public static Bitmap netPath2Bitmap(String urlStr){
        URL url = null;
        Bitmap bitmap = null;
        try {
            url = new URL(urlStr);
        } catch (MalformedURLException e) {
            return null;
        }

        bitmap = url2Bitmap(url);
        return bitmap;
    }

    /**
     * URL转换为bitmap
     * @param url 图片URL
     * @return
     */
    public static Bitmap url2Bitmap(URL url){
        Bitmap bitmap = null;
        InputStream inputStream = null;
        try {
            /**获得连接*/
            HttpURLConnection connection = (HttpURLConnection) url.openConnection();
            connection.setConnectTimeout(1000);
            /**连接设置获得数据流*/
            connection.setDoInput(true);
            connection.connect();
            inputStream = connection.getInputStream();
            bitmap = BitmapFactory.decodeStream(inputStream);
        } catch (IOException e) {
            return null;
        } finally {
            try {
                if(null != inputStream) inputStream.close();
            } catch (IOException e) {
                return null;
            }
        }
        return bitmap;
    }

    /**
     * 根据原图获取缩略图
     * @param path 原图路径
     * @param length 缩略图最大一边的长度
     * @return 缩略图
     */
    public static Bitmap localPath2Bitmap(String path, int length){
        if(length == 0 || path.isEmpty()) return null;

        BitmapFactory.Options options = new BitmapFactory.Options();
        options.inJustDecodeBounds = true;
        BitmapFactory.decodeFile(path,options);
        if(length < options.outWidth && length < options.outHeight) {
            int ratio = 0 ;
            if (options.outHeight > options.outWidth) {
                ratio = options.outHeight / length;
            } else {
                ratio = options.outWidth / length;
            }
            options.inSampleSize = ratio;
        }else {
            options.inSampleSize = 1;
        }
        Log.e("----",options.outWidth+"///"+options.outHeight);
        options.inJustDecodeBounds = false;
        Bitmap bitmap = null;
        try {
            bitmap = BitmapFactory.decodeFile(path, options);

            Log.e("----",bitmap.getWidth()+"///"+bitmap.getHeight());

        }catch (OutOfMemoryError error){
            return null;
        }
        return bitmap;
    }

    /**
     * 根据原图获取缩略图,横向宽度为屏幕宽度
     * @param path 原图路径
     * @return 缩略图
     */
    public static Bitmap localPath2Bitmap(Context context, String path){
        if(path.isEmpty()) return null;

        BitmapFactory.Options options = new BitmapFactory.Options();
        options.inJustDecodeBounds = true;
        BitmapFactory.decodeFile(path, options);
        int length = getScreenWidth(context);
        /**缩小宽度，否则为原图*/
        if(length < options.outWidth) {
            int ratio = 0 ;
            ratio = options.outWidth / length;
            options.inSampleSize = ratio;
        }else {
            options.inSampleSize = 1;
        }
        options.inJustDecodeBounds = false;
        Bitmap bitmap = null;
        try {
            bitmap = BitmapFactory.decodeFile(path, options);
            Log.e("-----",bitmap.getWidth()+"---"+bitmap.getHeight());
            if(bitmap == null)  return null;
            /**宽度保持不变，只改变高度*/
            float scale = 1.0f;
            /**图片宽高*/
            int w = bitmap.getWidth();
            int h = bitmap.getHeight();
            /**屏幕宽高*/
            int screenWidth = length;
            int screenHight = getScreenHeight(context);
            /**宽高比*/
            float ratio = (float)w/(float)h;
            /**太高图片，减小高度*/
            if(ratio <= (float)screenWidth/(float)screenHight+0.05){
                scale = 0.8f;
            }
            /**太宽的图片，增加高度*/
            else if(ratio >= (float)screenHight/(float)screenWidth-0.05){
                scale = 1.5f;
            }
            Matrix matrix = new Matrix();
            matrix.postScale(1.0f, scale);
            context = null;
            return Bitmap.createBitmap(bitmap, 0, 0, w, h, matrix, true);
        }catch (OutOfMemoryError error){
            return null;
        }
    }

    /**
     * 根据原图获取缩略图
     * @param path 原图路径
     * @param ratio 压缩比例
     * @return 缩略图
     */
    public static Bitmap localPath2Bitmap(String path, float ratio){
        if(ratio <= 0 || path.isEmpty()) return null;

        BitmapFactory.Options options = new BitmapFactory.Options();
        options.inJustDecodeBounds = true;
        BitmapFactory.decodeFile(path,options);
        options.inSampleSize = (int) (1.0/ratio);

        options.inJustDecodeBounds = false;
        Bitmap bitmap = null;
        try {
            bitmap = BitmapFactory.decodeFile(path, options);
        }catch (OutOfMemoryError error){
            return null;
        }
        return bitmap;
    }

    /**
     * 获取sd卡中的文件夹路径字符串数组
     * @param fileName 文件夹名称字符串数组
     * @return
     */
    public static String[] getPath(String[] fileName){
        String[] path = new String[fileName.length];
        String pathTemp = getSDParth();
        if(null != pathTemp) {
            for (int i = 0; i < fileName.length; i++) {
                path[i] = pathTemp + "/" + fileName[i];
            }
            return path;
        }else{
            return null;
        }
    }

    /**获取sd卡路径*/
    public static String getSDParth(){
        String state = Environment.getExternalStorageState();
        if(state.equals(Environment.MEDIA_MOUNTED)) {
            return Environment.getExternalStorageDirectory().toString();
        }else{
            return null;
        }
    }


    /**
     * 从完整路径下提取文件名称（带后缀）
     * @param filePath 文件路劲
     * @return
     */
    public static String getFileName(String filePath){
        String fileName;
        String path = filePath;

        while (path.contains("/")){
            int index = path.indexOf("/");
            path = path.substring(index+1,path.length());
        }
        fileName = path;
        return fileName;
    }
    /**
     * 从完整路径下提取文件名称（不带后缀）
     * @param filePath 文件路劲
     * @return
     */
    public static String getFileNameOnly(String filePath){
        String fileName;
        String path = filePath;

        while (path.contains("/")){
            int index = path.indexOf("/");
            path = path.substring(index+1,path.length());
        }
        String temp = path;
        while(temp.contains(".")){
            int index = temp.indexOf(".");
            temp = temp.substring(index+1,temp.length());
        }
        path = path.substring(0,path.length()-temp.length()-1);

        fileName = path;
        return fileName;
    }

    /**获取app缓存路径*/
    public static String getAppPath(Context context){
        String state = Environment.getExternalStorageState();
        if(state.equals(Environment.MEDIA_MOUNTED)) {
            String appPath = context.getExternalCacheDir().toString();
            File file = new File(appPath);
            if (!file.exists()) {
                file.mkdirs();
            }
            return appPath;
        }else{
            return null;
        }
    }

    /**获取路径中文件夹的名称*/
    public static String getFolderFileName(String folderFilePath){
        String folderFileName = null;

        String sdPath = getSDParth();
        String path = folderFilePath;
        if(path.contains(sdPath)) {
            /**截取后半部分*/
            path = path.substring(sdPath.length()+1,path.length());
            int index = path.indexOf("/");
            if(index > 0) {
                folderFileName = path.substring(0, index);
            }else{
                folderFileName = "root";
            }
        }
        return folderFileName;
    }

   /**获取mico图，用于显示*/
    public static Bitmap getMicoBitmap(String path, int length){
        if(length == 0 || path == null) return null;

        BitmapFactory.Options options = new BitmapFactory.Options();
        options.inJustDecodeBounds = true;
        BitmapFactory.decodeFile(path,options);
        if(length < options.outWidth && length < options.outHeight) {
            int ratio = 0 ;
            if (options.outHeight < options.outWidth) {
                ratio = options.outHeight / length;
            } else {
                ratio = options.outWidth / length;
            }
            options.inSampleSize = ratio;
        }
        options.inJustDecodeBounds = false;
        Bitmap bitmap = null;
        try {
            bitmap = BitmapFactory.decodeFile(path, options);
            Bitmap bitmap1 = ThumbnailUtils.extractThumbnail(bitmap,100,100);

            if(null != bitmap1) {
                return bitmap1;
            }else {
                return bitmap;
            }
        }catch (OutOfMemoryError error){
            Log.e("ii","outofMemory");
            return null;
        }
    }

    /**
     * 根据原图路径，生成缩略图 并保存在本地
     * @param path 原图路径
     * @param ratio 压缩比例
     * @return 缩略图路径
     */
    public static String saveThumbnailBitmap(Context context, String path, float ratio){
        /**原图压缩后的缩略图*/
        Bitmap bitmap = BitmapUtil.localPath2Bitmap(path,ratio);
        String filePath = getAppPath(context) +"/tmp";
        File file = new File(filePath);
        if(!file.exists()){
            file.mkdirs();
        }
        filePath = filePath +"/"+ BitmapUtil.getFileName(path);
        saveBitmap(filePath,bitmap);

        return  filePath;
    }

    /**
     * 将图片保存到本地,如果存在不替换
     * @param filePath 保存路径
     * @param bitmap 保存图片
     */
    public static void saveBitmap(String filePath, Bitmap bitmap){
        if(TextUtils.isEmpty(filePath)){
            return;
        }
        File myCaptureFile = new File(filePath);
        if (!myCaptureFile.exists()) {
            try {
                myCaptureFile.createNewFile();
                BufferedOutputStream bos = null;
                bos = new BufferedOutputStream(new FileOutputStream(myCaptureFile));
                bitmap.compress(Bitmap.CompressFormat.JPEG, 80, bos);
                bos.flush();
                bos.close();
            } catch (FileNotFoundException e) {
                return ;
            } catch (IOException e) {
                return ;
            }
        }
    }

    /**
     * 将图片保存到本地，如果存在则替换
     * @param filePath 保存路径
     * @param bitmap 保存图片
     */
    public static void saveBitmapNew(String filePath, Bitmap bitmap){
        File myCaptureFile = new File(filePath);
        if (myCaptureFile.exists()) {
            myCaptureFile.delete();
        }

        try {
            myCaptureFile.createNewFile();
            BufferedOutputStream bos = null;
            bos = new BufferedOutputStream(new FileOutputStream(myCaptureFile));
            bitmap.compress(Bitmap.CompressFormat.JPEG, 80, bos);
            bos.flush();
            bos.close();
        } catch (FileNotFoundException e) {
            return ;
        } catch (IOException e) {
            return ;
        }
    }

    /**计算屏幕宽度*/
    public static int getScreenWidth(Context context){
        DisplayMetrics metrics = context.getResources().getDisplayMetrics();
        int widthPixels = metrics.widthPixels;
        return widthPixels;
    }

    /**计算屏幕高度度*/
    public static int getScreenHeight(Context context){
        DisplayMetrics metrics = context.getResources().getDisplayMetrics();
        int heightPixels = metrics.heightPixels;
        return heightPixels;
    }

    public static String getMimeTypeForExtension(String path) {
        String extension = path;
        int lastDot = extension.lastIndexOf('.');
        if (lastDot != -1) {
            extension = extension.substring(lastDot + 1);
        }
        // Convert the URI string to lower case to ensure compatibility with MimeTypeMap (see CB-2185).
        extension = extension.toLowerCase(Locale.getDefault());
        if (extension.equals("3ga")) {
            return "audio/3gpp";
        }
        return MimeTypeMap.getSingleton().getMimeTypeFromExtension(extension);
    }

    public static String getMimeType(Uri uri, Context context) {
        String mimeType = null;
        if ("content".equals(uri.getScheme())) {
            mimeType = context.getContentResolver().getType(uri);
        } else {
            mimeType = getMimeTypeForExtension(uri.getPath());
        }

        return mimeType;
    }

}
