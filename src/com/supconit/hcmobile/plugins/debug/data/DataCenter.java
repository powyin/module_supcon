package com.supconit.hcmobile.plugins.debug.data;

import android.text.TextUtils;
import android.util.Log;

import com.supconit.hcmobile.HcmobileApp;
import com.supconit.hcmobile.util.FileUtil;
import com.supconit.hcmobile.util.Util;

import org.apache.tools.zip.ZipEntry;
import org.apache.tools.zip.ZipFile;

import java.io.File;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.Enumeration;
import java.util.HashMap;

public class DataCenter {
    private volatile static DataCenter mInstance;

    public static DataCenter getInstance() {
        if (mInstance == null) {
            synchronized (DataCenter.class) {
                if (mInstance == null) {
                    mInstance = new DataCenter();
                }
            }
        }
        return mInstance;
    }

    private HashMap<String, FileDes> fileDesHashMap = new HashMap<>();
    private FileDes mRoot;
    private String rootSub;

    private DataCenter() {
        resetRootConfig();
    }

    public void resetRootConfig() {
        fileDesHashMap.clear();
        File root = FileUtil.getOfficeFilePath(HcmobileApp.getApplication(), "debug_www", "", true);
        rootSub = root.getAbsolutePath();
        mRoot = ensureFileDescribe(root);
        mRoot.path = "/";
        mRoot.name = "/";
    }

    // todo 构建文件描述
    private FileDes ensureFileDescribe(File file) {
        if (file == null || !file.exists()) {
            return null;
        }
        FileDes fileDes = new FileDes();
        fileDes.name = file.getName();
        fileDes.path = file.getAbsolutePath();
        fileDes.path = fileDes.path.substring(rootSub.length());
        int lastIndex = fileDes.path.lastIndexOf("/");
        if (lastIndex > 0) {
            fileDes.parentFile = fileDes.path.substring(0, lastIndex);
        }
        fileDesHashMap.put(fileDes.path, fileDes);
        fileDes.isFile = file.isFile();
        if (fileDes.isFile) {
            fileDes.length = file.length();
            fileDes.listFiles = null;
        } else {
            fileDes.length = -1;
            fileDes.listFiles = new ArrayList<>();
            File[] files = file.listFiles();
            if (files != null) {
                for (File tem : files) {
                    fileDes.listFiles.add(ensureFileDescribe(tem));
                }
            }
        }
        return fileDes;
    }

    // todo 获取现对路径下面的文件描述
    public FileDes getPath(String path) {
        if (TextUtils.isEmpty(path) || "/".equals(path) || ".".equals(path) || "\\".equals(path)) {
            return mRoot;
        } else {
            return fileDesHashMap.get(path);
        }
    }

    // todo 拷贝指定文件到目标地址
    public void writeFile(String location, File sourceFile) {
        location = Util.pathAppend(rootSub, location);
        Log.i("powyin", "writeFile: " + location);
        FileUtil.copy(sourceFile, new File(location));
        FileUtil.deleteFile(sourceFile);
        resetRootConfig();
    }

    // todo 解压缩zip包到根目录
    public void uZipFileToRootDirectory(File zipFile) {
        unzip(zipFile, new File(rootSub));
        resetRootConfig();
    }

    // todo 清空文件
    public void clear() {
        deleteFile(rootSub);
        resetRootConfig();
    }

    // todo 删除文件
    public void deleteFile(String path) {
        String location = Util.pathAppend(rootSub, path);
        Log.i("powyin", "deleteFile: " + path);
        FileUtil.deleteFile(new File(location));
        resetRootConfig();
    }

    // todo 现对路径转绝对路径
    public File getFile(String path) {
        path = Util.pathAppend(rootSub, path);
        Log.i("powyin", "getPath: " + path);
        return new File(path);
    }

    /**
     * 解压缩方法
     *
     * @param zipFilePath     压缩文件名   /sdcard/mm.zip
     * @param targetDirectory 解压目标路径 /sdcard/cache/tem/
     */
    private boolean unzip(File zipFilePath, File targetDirectory) {
        ZipFile zipInputStream = null;
        try {
            zipInputStream = new ZipFile(zipFilePath, "GBK");
            ZipEntry zipEntry = null;
            byte[] buffer = new byte[512];
            int readLength = 0;
            Enumeration<ZipEntry> entries = zipInputStream.getEntries();

            while (entries.hasMoreElements()) {
                zipEntry = entries.nextElement();
                File file = new File(targetDirectory.getAbsolutePath() + "/" + zipEntry.getName());
                FileUtil.ensureFileExist(file, zipEntry.isDirectory());
                if (zipEntry.isDirectory()) {
                    continue;
                }
                OutputStream outputStream = new FileOutputStream(file);
                InputStream inputStream = zipInputStream.getInputStream(zipEntry);
                while ((readLength = inputStream.read(buffer, 0, 512)) != -1) {
                    outputStream.write(buffer, 0, readLength);
                }
                try {
                    inputStream.close();
                } catch (Exception e) {
                    e.printStackTrace();
                }
                try {
                    outputStream.close();
                } catch (Exception e) {
                    e.printStackTrace();
                }
                Log.d("powyin", "uncompressed  " + file.getCanonicalPath());
            }    // end while
        } catch (Exception e) {
            e.printStackTrace();
            System.out.println("unzip fail!");
            Log.d("powyin", "unzip fail!  " + zipFilePath);
            return false;
        } finally {
            if (zipInputStream != null) {
                try {
                    zipInputStream.close();
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        }

        Log.d("powyin", "unzip success!  ");
        return true;
    }

}
