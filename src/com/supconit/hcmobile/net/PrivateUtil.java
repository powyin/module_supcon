package com.supconit.hcmobile.net;

import android.content.Context;
import android.os.Environment;
import android.util.Log;
import java.io.File;

class PrivateUtil {


    /**
     * 获取 cache 随机文件   可设置后缀名
     *
     * @param context    content
     * @param suffixName 后缀名
     * @param isDir      是否目录
     */
    static File getRandomFilePath(Context context, String prefixName, String suffixName, boolean isDir) {
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
    static boolean ensureFileExist(File file, boolean isDir) {
        if ((!isDir && file.isDirectory()) || (isDir && file.isFile())) {
            if (!file.delete()) {
                Log.e("FileUtil", "cannot delete file : " + file);
                return false;
            }
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
    }


    /**
     * 提取url中 前缀名 后缀名 完整名
     *
     * @return https://github.com/apache/incubator-weex.txt#cc=23   return   ["incubator-weex","txt","incubator-weex.txt"]
     */
    static String[] getFileNameFromUrl(String url) {
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
     * 先根遍历序递归删除文件夹
     */
    static void deleteFile(File dirFile) {
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



}
