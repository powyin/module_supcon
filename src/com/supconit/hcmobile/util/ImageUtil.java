package com.supconit.hcmobile.util;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.text.TextUtils;
import android.util.Log;

import java.io.BufferedInputStream;
import java.io.Closeable;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.UUID;

public class ImageUtil {


    /**
     * 获取bitmap
     *
     * @param file      文件
     * @param maxWidth  最大宽度
     * @param maxHeight 最大高度
     * @return bitmap
     */
    public static Bitmap getBitmap(File file, int maxWidth, int maxHeight) {
        if (file == null || !file.exists()) return null;
        InputStream is = null;
        try {
            BitmapFactory.Options options = new BitmapFactory.Options();
            options.inJustDecodeBounds = true;

            is = new BufferedInputStream(new FileInputStream(file));
            BitmapFactory.decodeStream(is, null, options);
            options.inSampleSize = calculateInSampleSize(options, maxWidth, maxHeight);
            options.inJustDecodeBounds = false;
            options.inScaled = true;

            return BitmapFactory.decodeFile(file.getAbsolutePath(), options);
        } catch (FileNotFoundException e) {
            e.printStackTrace();
            return null;
        } finally {
            closeIO(is);
        }
    }


    /**
     * Bitmap 转成文件
     *
     * @param bitmap
     * @return
     */
    public static boolean bitmapToFile(File targetFile, Bitmap bitmap) {
        if (targetFile == null || !FileUtil.ensureFileExist(targetFile, false)) {
            return false;
        }

        FileOutputStream fos = null;
        try {
            fos = new FileOutputStream(targetFile);
            bitmap.compress(Bitmap.CompressFormat.PNG, 70, fos);
            return true;
        } catch (Exception e) {
            e.printStackTrace();
            return false;
        } finally {
            closeIO(fos);
        }
    }

    public static File scaleBitMap(Context context, File targetFile, int maxWid, int maxHei) {
        Bitmap bitmap = getBitmap(targetFile, maxWid, maxHei);
        if (bitmap == null) {
            Log.e("powyin", "scale bitmap file error: ensureFileExist");
            return null;
        }
        String[] fileNameFromUrl = FileUtil.getFileNameFromUrl(targetFile.getAbsolutePath());
        String prefixName = TextUtils.isEmpty(fileNameFromUrl[0]) ? Util.hashKey(targetFile.getAbsolutePath()) : fileNameFromUrl[0];
        prefixName += "_" + maxWid + "_" + maxHei;
        File temSave = FileUtil.getRandomFilePath(context, prefixName, "png", false);
        boolean success = bitmapToFile(temSave, bitmap);
        if (!success) {
            Log.e("powyin", "scale bitmap file error: bitmapToFile");
            return null;
        }
        return temSave;
    }


    /**
     * 计算采样大小
     *
     * @param options   选项
     * @param maxWidth  最大宽度
     * @param maxHeight 最大高度
     * @return 采样大小
     */
    private static int calculateInSampleSize(BitmapFactory.Options options, int maxWidth, int maxHeight) {
        if (maxWidth == 0 || maxHeight == 0) return 1;
        int height = options.outHeight;
        int width = options.outWidth;
        int inSampleSize = 1;
        while (height > maxHeight || width > maxWidth) {
            height >>= 1;
            width >>= 1;
            inSampleSize <<= 1;
        }
        return inSampleSize;
    }


    /**
     * 关闭IO
     *
     * @param closeables closeable
     */
    private static void closeIO(Closeable... closeables) {
        if (closeables == null) return;
        for (Closeable closeable : closeables) {
            if (closeable != null) {
                try {
                    closeable.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }
    }

}
