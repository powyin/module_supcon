package com.supconit.hcmobile.util;

import android.annotation.SuppressLint;
import android.content.ContentResolver;
import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.net.Uri;
import android.os.Environment;
import android.support.v4.content.FileProvider;
import android.text.TextUtils;
import android.util.Log;

import com.supconit.hcmobile.HcmobileApp;

import org.apache.cordova.CordovaWebView;
import org.apache.cordova.PluginManager;
import org.json.JSONObject;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.lang.reflect.Field;
import java.lang.reflect.Method;

public class FileUtil {

    //todo ------------------------------------------------------------------------------------文件处理 start------------------------------------------------------------------------//

    /**
     * 获取 file 随机文件   可设置后缀名
     *
     * @param context    content
     * @param suffixName 后缀名
     * @param isDir      是否目录
     */
    public static File getOfficeFilePath(Context context, String prefixName, String suffixName, boolean isDir) {
        String path;
        String cachePath = "/office_file";
        // 正式用法
        if (context.getFilesDir() != null) {
            path = context.getFilesDir().getAbsolutePath() + cachePath;
        } else {
            path = Environment.getExternalStorageDirectory().getAbsolutePath() + cachePath;
        }

        if (suffixName == null) {
            suffixName = "";
        }
        if (suffixName.length() > 0 && !suffixName.startsWith(".")) {
            suffixName = "." + suffixName;
        }

        if (prefixName == null || prefixName.length() == 0) {
            prefixName = System.currentTimeMillis() + "";
        }
        path = path + "/" + prefixName + suffixName;
        Log.d("office_file", path);

        File pathFile = new File(path);
        ensureFileExist(pathFile, isDir);
        return pathFile;
    }


    /**
     * 获取 cache 随机文件   可设置后缀名
     *
     * @param context    content
     * @param suffixName 后缀名
     * @param isDir      是否目录
     */
    public static File getRandomFilePath(Context context, String prefixName, String suffixName, boolean isDir) {
        String path;
        String cachePath = "/random_cache";
        // 正式用法
        if (context.getExternalCacheDir() != null) {
            path = context.getExternalCacheDir().getAbsolutePath() + cachePath;
        } else if (context.getCacheDir() != null) {
            path = context.getCacheDir().getAbsolutePath() + cachePath;
        } else {
            path = Environment.getExternalStorageDirectory().getAbsolutePath() + cachePath;
        }

        if (suffixName == null) {
            suffixName = "";
        }
        if (suffixName.length() > 0 && !suffixName.startsWith(".")) {
            suffixName = "." + suffixName;
        }

        if (prefixName == null || prefixName.length() == 0) {
            prefixName = System.currentTimeMillis() + "";
        }
        path = path + "/" + prefixName + suffixName;
        Log.d("random_file", path);

        File pathFile = new File(path);
        ensureFileExist(pathFile, isDir);
        return pathFile;
    }


    /**
     * 确保文件存在 否则创建它
     *
     * @param file  目标检查的文件
     * @param isDir 目标文件是否为目录
     * @return
     */
    public static boolean ensureFileExist(File file, boolean isDir) {
        try {
            if ((!isDir && file.isDirectory()) || (isDir && file.isFile())) {
                deleteFile(file);
            }
            if (file.exists()) {
                return true;
            }
            if (ensureFileExist(file.getParentFile(), true)) {
                if (isDir) {
                    return file.mkdir();
                } else {
                    try {
                        return file.createNewFile();
                    } catch (Exception e) {
                        e.printStackTrace();
                        return false;
                    }
                }
            } else {
                return false;
            }
        } catch (Exception e) {
            e.printStackTrace();
            return false;
        }
    }


    /**
     * 先根遍历序递归删除文件夹
     */
    public static void deleteFile(File dirFile) {
        if (dirFile == null || !dirFile.exists()) {
            return;
        }
        if (dirFile.isFile()) {
            if (!dirFile.delete()) {
                Log.e("powyin", "delete file fail " + dirFile);
            }
        } else {
            File[] files = dirFile.listFiles();
            for (int i = 0; files != null && i < files.length; i++) {
                deleteFile(files[i]);
            }
            if (!dirFile.delete()) {
                Log.e("powyin", "delete file dir fail " + dirFile);
            }
        }
    }


    /**
     * 拷贝文件或者文件夹
     */
    public static void copyDirOrFile(File source, File target) {
        if (source == null || target == null || !source.exists()) {
            return;
        }
        if (source.isFile()) {
            copy(source, target);
        } else {
            ensureFileExist(target, true);
            File[] files = source.listFiles();
            if (files != null) {
                for (File tem : files) {
                    File temTarget = new File(target, tem.getName());
                    copyDirOrFile(tem, temTarget);
                }
            }
        }
    }


    /**
     * 拷贝文件
     */
    public static void copy(File source, File target) {
        if (source == null || target == null || !source.exists()) {
            return;
        }
        ensureFileExist(target, false);
        try {
            BufferedInputStream bis = new BufferedInputStream(new FileInputStream(source));
            BufferedOutputStream bos = new BufferedOutputStream(new FileOutputStream(target));
            int len = 0;
            byte[] b = new byte[1024 * 64];
            while ((len = bis.read(b)) != -1) {
                bos.write(b, 0, len);
            }
            bis.close();
            bos.close();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public static File mapUriToFile(Uri uri) {
        String scheme = uri.getScheme();
        if (ContentResolver.SCHEME_CONTENT.equalsIgnoreCase(scheme)) {
            Cursor cursor = HcmobileApp.getApplication().getContentResolver().query(uri, new String[]{"_data"}, null, null, null);
            if (cursor != null) {
                try {
                    int columnIndex = cursor.getColumnIndex("_data");
                    if (columnIndex != -1 && cursor.getCount() > 0) {
                        cursor.moveToFirst();
                        String realPath = cursor.getString(columnIndex);
                        if (realPath != null) {
                            return new File(realPath);
                        }
                    }
                } finally {
                    cursor.close();
                }
            }
        }
        if (ContentResolver.SCHEME_ANDROID_RESOURCE.equalsIgnoreCase(scheme)) {
            return null;
        }
        if (ContentResolver.SCHEME_FILE.equalsIgnoreCase(scheme)) {
            String path = uri.getPath();
            if (!TextUtils.isEmpty(path) && !path.startsWith("/android_asset/")) {
                return new File(path);
            }
        }

        try {
            String path = uri.getPath();
            if (!TextUtils.isEmpty(path)) {
                return new File(path);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }

        return null;
    }


    /**
     * 提取url中 前缀名 后缀名 完整名
     *
     * @return https://github.com/apache/incubator-weex.txt#cc=23   return   ["incubator-weex","txt","incubator-weex.txt"]
     */
    public static String[] getFileNameFromUrl(String url) {
        String[] preName_SuffixName_FullName = new String[3];
        preName_SuffixName_FullName[0] = "";
        preName_SuffixName_FullName[1] = "";
        preName_SuffixName_FullName[2] = "";
        if (url != null && url.length() > 0) {
            int fragment = url.lastIndexOf('#');
            if (fragment > 0) {
                url = url.substring(0, fragment);
            }
            int query = url.lastIndexOf('?');
            if (query > 0) {
                url = url.substring(0, query);
            }
            int filenamePos = url.lastIndexOf('/');
            String filename = 0 <= filenamePos ? url.substring(filenamePos + 1) : url;
            preName_SuffixName_FullName[2] = filename;
            int dotPos = filename.lastIndexOf('.');
            if (dotPos >= 0) {
                preName_SuffixName_FullName[0] = filename.substring(0, dotPos);
                preName_SuffixName_FullName[1] = filename.substring(dotPos + 1);
            } else {
                preName_SuffixName_FullName[0] = filename;
            }
        }
        return preName_SuffixName_FullName;
    }


    /**
     * 读取asset文件为String
     */
    public static String readAssetFileAsString(Context context, String path) {
        if (context == null || TextUtils.isEmpty(path)) {
            return null;
        }
        InputStream inputStream = null;
        try {
            inputStream = context.getAssets().open(path);
            return _readStreamAsString(inputStream);
        } catch (IOException e) {
            e.printStackTrace();
        }
        return null;
    }


    /**
     * 读取本地文件为String
     */
    public static String readLocalFileAsString(String path) {
        if (!TextUtils.isEmpty(path)) {
            File file = new File(path);
            if (file.exists()) {
                try {
                    FileInputStream fis = new FileInputStream(file);
                    return _readStreamAsString(fis);
                } catch (FileNotFoundException e) {
                    e.printStackTrace();
                }
            }
        }
        return "";
    }

    /**
     * 读取stream流为String
     */
    private static String _readStreamAsString(InputStream inputStream) {
        BufferedReader bufferedReader = null;
        try {
            StringBuilder builder = new StringBuilder(inputStream.available() + 10);
            bufferedReader = new BufferedReader(new InputStreamReader(inputStream));
            char[] data = new char[4096];
            int len = -1;
            while ((len = bufferedReader.read(data)) > 0) {
                builder.append(data, 0, len);
            }

            return builder.toString();
        } catch (IOException e) {
            e.printStackTrace();
        } finally {
            try {
                if (bufferedReader != null)
                    bufferedReader.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
            try {
                if (inputStream != null)
                    inputStream.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }

        return null;
    }


    /**
     * cdf协议文件转换成本地文件系统
     */
    @SuppressWarnings("unchecked")
    public static String cdvFileToLocal(CordovaWebView webView, String cdvFilePath) {
        if (cdvFilePath == null || !cdvFilePath.startsWith("cdv")) {
            Log.d("powyin", "cdvfile transfor error " + cdvFilePath);
            return cdvFilePath;
        }


        Class webViewClass = webView.getClass();
        PluginManager pm = null;
        try {
            Method gpm = webViewClass.getMethod("getPluginManager");
            pm = (PluginManager) gpm.invoke(webView);
        } catch (Exception e) {
            e.printStackTrace();
        }
        if (pm == null) {
            try {
                Field pmf = webViewClass.getField("pluginManager");
                pm = (PluginManager) pmf.get(webView);
            } catch (Exception e) {
                e.printStackTrace();
            }
        }

        Object filePlugin = pm == null ? null : pm.getPlugin("File");
        if (filePlugin == null) {
            Log.d("powyin", "cdvfile transfor error cannot get PluginManager " + pm);
            return cdvFilePath;
        }
        try {
            Method resolveLocalFileSystemURI = filePlugin.getClass().getDeclaredMethod("resolveLocalFileSystemURI", String.class);
            resolveLocalFileSystemURI.setAccessible(true);
            JSONObject invoke = (JSONObject) resolveLocalFileSystemURI.invoke(filePlugin, cdvFilePath);
            String value = invoke.getString("nativeURL");
            if (value.startsWith("file")) {
                value = value.replace("file://", "");
            }
            return value;
        } catch (Exception e) {
            e.printStackTrace();
            Log.d("powyin", "cdvfile transfor error 发生了严重错误");
        }
        return cdvFilePath;
    }


    /**
     * 本地文件系统转cdv协议文件
     */
    @SuppressWarnings("unchecked")
    public static String localToCdvFile(CordovaWebView webView, String localCdvFilePath) {

        Class webViewClass = webView.getClass();
        PluginManager pm = null;
        try {
            Method gpm = webViewClass.getMethod("getPluginManager");
            pm = (PluginManager) gpm.invoke(webView);
        } catch (Exception e) {
            e.printStackTrace();
        }

        if (pm == null) {
            try {
                Field pmf = webViewClass.getField("pluginManager");
                pm = (PluginManager) pmf.get(webView);
            } catch (Exception e) {
                e.printStackTrace();
            }
        }

        Object filePlugin = pm == null ? null : pm.getPlugin("File");
        if (filePlugin == null) {
            Log.d("powyin", "cdvfile transfor error cannot get PluginManager " + pm);
            return localCdvFilePath;
        }

        try {
            Method resolveLocalFileSystemURI = filePlugin.getClass().getDeclaredMethod("filesystemURLforLocalPath", String.class);
            return resolveLocalFileSystemURI.invoke(filePlugin, localCdvFilePath).toString();
        } catch (Exception e) {
            e.printStackTrace();
            return localCdvFilePath;
        }
    }


    // todo 调用系统第三方app打开文档
    public static void previewFileWithSystemApp(Context context, String path) {
        if (TextUtils.isEmpty(path)) {
            return;
        }

        String mType;
        String format = path.substring(path.lastIndexOf(".") + 1);
        format = format.toLowerCase();
        switch (format) {
            case "doc":
            case "docx":
                mType = "application/msword";
                break;
            case "xls":
            case "xlsx":
                mType = "application/vnd.ms-excel";
                break;
            case "zip":
            case "rar":
                mType = "application/x-gzip";
                break;
            case "pdf":
                mType = "application/pdf";
                break;
            case "ppt":
            case "pptx":
                mType = "application/vnd.ms-powerpoint";
                break;
            case "text":
            case "css":
            case "txt":
                mType = "text/plain";
                break;
            case "png":
            case "jpeg":
            case "gif":
                mType = "image/*";
                break;
            case "json":
                mType = "application/json";
                break;
            case "xml":
                mType = "text/xml";
                break;
            case "html":
                mType = "text/html";
                break;
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
                mType = "audio/mpeg";
                break;
            default:
                mType = "";
                break;
        }

        Intent intent = new Intent("android.intent.action.VIEW");
        intent.addCategory("android.intent.category.DEFAULT");
        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);

        try {
            if (path.startsWith("http")) {
                Uri uri = Uri.parse(path);
                intent.setData(uri);
                context.startActivity(intent);
            } else {
                if (path.startsWith("file://")) {
                    path = path.substring("file://".length());
                }
                Uri uri = FileProvider.getUriForFile(context, context.getApplicationContext().getPackageName() + ".provider", new File(path));
                intent.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION | Intent.FLAG_GRANT_WRITE_URI_PERMISSION);
                if (!TextUtils.isEmpty(mType)) {
                    intent.setDataAndType(uri, mType);
                } else {
                    intent.setData(uri);
                }
                context.startActivity(intent);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }


    /**
     * //删除文件夹以及文件夹下的所有文件
     *
     * @param dir think
     */
    public static void deleteDirWihtFile(File dir) {
        if (dir == null || !dir.exists() || !dir.isDirectory())
            return;
        for (File file : dir.listFiles()) {
            if (file.isFile())
                file.delete(); // 删除所有文件
            else if (file.isDirectory())
                deleteDirWihtFile(file); // 递规的方式删除文件夹
        }
        dir.delete();// 删除目录本身
    }

    /**
     * 读取文本文件中的内容
     *
     * @param strFilePath
     * @return
     */
    public static String ReadTxtFile(String strFilePath) {
        String path = strFilePath;
        String content = ""; //文件内容字符串
        //打开文件
        File file = new File(path);
        //如果path是传递过来的参数，可以做一个非目录的判断
        if (file.isDirectory()) {
            Log.d("TestFile", "The File doesn't not exist.");
        } else {
            try {
                InputStream instream = new FileInputStream(file);
                if (instream != null) {
                    InputStreamReader inputreader = new InputStreamReader(instream);
                    BufferedReader buffreader = new BufferedReader(inputreader);
                    String line;
                    //分行读取
                    while ((line = buffreader.readLine()) != null) {
                        content += line + "\n";
                    }
                    instream.close();
                }
            } catch (java.io.FileNotFoundException e) {
                Log.d("TestFile", "The File doesn't not exist.");
            } catch (IOException e) {
                Log.d("TestFile", e.getMessage());
            }
        }
        return content;
    }


    /**
     * 获取sd卡路径
     */
    @SuppressLint("SdCardPath")
    public static String getSDHcPath() {
        String state = Environment.getExternalStorageState();
        if (state.equals(Environment.MEDIA_MOUNTED)) {
            return Environment.getExternalStorageDirectory().toString() + "/hcFile";
        } else {
            return "/sdcard/hcFile";
        }
    }


}


//    /**
//     * 解压缩方法
//     *
//     * @param zipFilePath     压缩文件名   /sdcard/mm.zip
//     * @param targetDirectory 解压目标路径 /sdcard/cache/tem/
//     */
//    public static boolean unzip(File zipFilePath, File targetDirectory) {
//        ZipFile zipInputStream = null;
//        try {
//            zipInputStream = new ZipFile(zipFilePath, "GBK");
//            ZipEntry zipEntry = null;
//            byte[] buffer = new byte[512];
//            int readLength = 0;
//            Enumeration<ZipEntry> entries = zipInputStream.getEntries();
//
//            while (entries.hasMoreElements()) {
//                zipEntry = entries.nextElement();
//                File file = new File(targetDirectory.getAbsolutePath() + "/" + zipEntry.getName());
//                ensureFileExist(file, zipEntry.isDirectory());
//                if (zipEntry.isDirectory()) {
//                    continue;
//                }
//                OutputStream outputStream = new FileOutputStream(file);
//                InputStream inputStream = zipInputStream.getInputStream(zipEntry);
//                while ((readLength = inputStream.read(buffer, 0, 512)) != -1) {
//                    outputStream.write(buffer, 0, readLength);
//                }
//                try {
//                    inputStream.close();
//                } catch (Exception e) {
//                    e.printStackTrace();
//                }
//                try {
//                    outputStream.close();
//                } catch (Exception e) {
//                    e.printStackTrace();
//                }
//                Log.d("powyin", "uncompressed  " + file.getCanonicalPath());
//            }    // end while
//        } catch (Exception e) {
//            e.printStackTrace();
//            System.out.println("unzip fail!");
//            Log.d("powyin", "unzip fail!  " + zipFilePath);
//            return false;
//        } finally {
//            if (zipInputStream != null) {
//                try {
//                    zipInputStream.close();
//                } catch (Exception e) {
//                    e.printStackTrace();
//                }
//            }
//        }
//
//        Log.d("powyin", "unzip success!  ");
//        return true;
//    }

