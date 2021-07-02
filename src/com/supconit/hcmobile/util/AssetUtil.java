package com.supconit.hcmobile.util;

import android.content.Context;
import android.content.pm.PackageManager.NameNotFoundException;
import android.content.res.AssetManager;
import android.os.Environment;
import android.util.Log;

import com.supconit.hcmobile.HcmobileApp;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.zip.CRC32;


public class AssetUtil {

    static final String TAG = "AssetUtil";

    // static Context context;
    private static List<AssetFileTransfer> copyAssetFolder(String assetBasePath, String targetDirPath) {
        Set<String> filenames = getAssetFilenames(assetBasePath);
        List<AssetFileTransfer> transfers = new ArrayList<AssetFileTransfer>();

        for (String f : filenames) {
            AssetFileTransfer aft = new AssetFileTransfer();
            transfers.add(aft);
            try {
                aft.copyAssetToTargetDir(HcmobileApp.getApplication().getAssets(), f, targetDirPath);
            } catch (RuntimeException afte) {
                afte.printStackTrace();
            }
        }
        return transfers;
    }

    public static void cacheAssetFolder(String assetBasePath) {
        Context ctx = HcmobileApp.getApplication();
        boolean reCache = false;
        int versionCode = 0;
        try {
            versionCode = ctx.getPackageManager().getPackageInfo(ctx.getPackageName(), 0).versionCode;
        } catch (NameNotFoundException nnfe) {
            nnfe.printStackTrace();
        }
        File cacheFolder = new File(ctx.getCacheDir().getAbsolutePath() + "/" + assetBasePath);
        File cacheIndexFile = new File(cacheFolder, "cacheIndex-" + versionCode + ".txt");

        BufferedReader inBuf = null;
        try {
            inBuf = new BufferedReader(new FileReader(cacheIndexFile));
            String line;
            while ((line = inBuf.readLine()) != null) {
                File cachedFile = new File(line);
                if (!cachedFile.exists()) {
                    Log.i(TAG, "cacheAssetFolder(): Cache for folder '" + assetBasePath + "' incomplete. Re-caching.");
                    reCache = true;
                    break;
                }
            }
        } catch (FileNotFoundException fnfe) {
            Log.i(TAG, "cacheAssetFolder(): Cache index not found for folder '" + assetBasePath + "'. Re-caching.");
            reCache = true;
        } catch (IOException ioe) {
            ioe.printStackTrace();
            return;
        } finally {
            if (inBuf != null) {
                try {
                    inBuf.close();
                    inBuf = null;
                } catch (IOException ioe) {
                    ioe.printStackTrace();
                }
            }
        }

        if (reCache) {
            FileUtil.deleteFile(cacheFolder);
            List<AssetFileTransfer> transfers = copyAssetFolder(assetBasePath, ctx.getCacheDir().getAbsolutePath()); // Recreate it.

            // Now write a new cache index inside the folder.
            BufferedWriter outBuf = null;
            try {
                outBuf = new BufferedWriter(new FileWriter(cacheIndexFile));
                for (AssetFileTransfer aft : transfers) {
                    outBuf.write(aft.targetFile.getAbsolutePath());
                    outBuf.newLine();
                }
            } catch (Exception fnfe) {
                fnfe.printStackTrace();
            } finally {
                if (outBuf != null) {
                    try {
                        outBuf.flush();
                        outBuf.close();
                        outBuf = null;
                    } catch (IOException ioe) {
                        ioe.printStackTrace();
                    }
                }
            }
        } else {
            Log.i(TAG, "cacheAssetFolder(): Using cached folder '" + assetBasePath + "'.");
        }

    }

    private static Set<String> getAssetFilenames(String path) {
        Set<String> files = new HashSet<String>();
        getAssetFilenames(path, files);
        return files;
    }


    private static void getAssetFilenames(String path, Set<String> files) {
        try {
            String[] filenames = HcmobileApp.getApplication().getAssets().list(path); // Equivalent to "ls path"
            // Recursion logic.
            // A little logic to decide if a path is a file. Empty folders are never saved
            // in the archive, so if there are no files below this path, it's a file.
            // If there are files beneath, then this is a directory and we can add it to the folder
            // structure.
            if (filenames.length == 0) { // A file.
                files.add(path);
                Log.i(TAG, "getAssetFilenames(): Found asset '" + path + "'");
            } else { // A directory.
                for (String f : filenames) {
                    // Create a full path by concatenating path and the filename
                    File file = new File(path, f);
                    String fileName = file.getPath();
                    getAssetFilenames(fileName, files); // Recurse.
                }
            }
        } catch (IOException ioe) {
            ioe.printStackTrace();
        }
    }

    static long computeCRC(String filename) throws RuntimeException {

        InputStream in = null;
        byte[] buffer = new byte[16384];
        int bytesRead = -1;
        CRC32 crc = new CRC32();

        try {
            in = new FileInputStream(filename);
        } catch (FileNotFoundException fnfe) {
            throw new RuntimeException("File not found: " + filename, fnfe);
        }

        //long crcStartTime = System.nanoTime();

        try {
            while ((bytesRead = in.read(buffer)) != -1) crc.update(buffer, 0, bytesRead);
            in.close();
            in = null;
        } catch (IOException ioe) {
            throw new RuntimeException("IOException while reading from file", ioe);
        }

        long value = crc.getValue();

        //long elapsedTime = System.nanoTime() - crcStartTime;
        //Log.i(TAG, "CRC time: " + (elapsedTime / 1000000.0f) + " ms");

        //Log.i(TAG, "CRC result of " + filename + ": " + value);

        return value;

    }


    static class AssetFileTransfer {
        private static final String TAG = "AssetFileTransfer";
        File targetFile;
        private File tempFile;
        private void copyContents(InputStream in, OutputStream out) throws IOException {
            final int bufferSize = 16384;
            byte[] buffer = new byte[bufferSize];
            int bytesRead;
            while ((bytesRead = in.read(buffer)) != -1) {
                out.write(buffer, 0, bytesRead);
            }
            out.flush();
        }

        void copyAssetToTargetDir(AssetManager manager, String assetFilePath, String targetDirPath) throws RuntimeException {
            InputStream in;
            OutputStream out;
            try {
                in = manager.open(assetFilePath);
            } catch (IOException e) {
                throw new RuntimeException("Unable to open the asset file: " + assetFilePath, e);
            }

            targetFile = new File(targetDirPath, assetFilePath);
            boolean targetFileAlreadyExists = targetFile.exists();

            Log.i(TAG, "copyAssetToTargetDir(): [" + assetFilePath + "] -> [" + targetFile.getPath() + "]");

            if (targetFileAlreadyExists) {
                try {
                    tempFile = File.createTempFile("unpacker", null, Environment.getExternalStorageDirectory());
                    //Log.i(TAG, "Created temp file for unpacking: " + tempFile.getPath());
                } catch (IOException ioe) {
                    throw new RuntimeException("Error creating temp file: " + tempFile.getPath(), ioe);
                }
                try {
                    out = new FileOutputStream(tempFile);
                } catch (FileNotFoundException fnfe) {
                    throw new RuntimeException("Error creating temp file: " + tempFile.getPath(), fnfe);
                }
                try {
                    copyContents(in, out);
                    in.close();
                    in = null;
                    out.close();
                    out = null;
                } catch (IOException ioe) {
                    throw new RuntimeException("Error copying asset to temp file: " + tempFile.getPath(), ioe);
                }
                long targetFileCRC;
                long tempFileCRC;
                try {
                    tempFileCRC = AssetUtil.computeCRC(tempFile.getPath());
                    targetFileCRC = AssetUtil.computeCRC(targetFile.getPath());
                } catch (RuntimeException hce) {
                    throw new RuntimeException("Error hashing files", hce);
                }

                if (tempFileCRC == targetFileCRC) {
                    tempFile.delete();
                } else {
                    targetFile.delete();
                    tempFile.renameTo(targetFile);
                }

            } else {

                Log.i(TAG, "copyAssetToTargetDir(): Target file does not exist. Creating directory structure.");

                // Ensure parent directories exist so we can create the file
                File targetDirectory = targetFile.getParentFile();
                targetDirectory.mkdirs();

                // Copy asset to target file
                try {
                    out = new FileOutputStream(targetFile);
                } catch (FileNotFoundException fnfe) {
                    throw new RuntimeException("Error creating target file: " + targetFile.getPath(), fnfe);
                }
                try {
                    copyContents(in, new FileOutputStream(targetFile));
                    //Log.i(TAG, "Copied asset to target file");

                    in.close();
                    in = null;
                    out.close();
                    out = null;
                } catch (IOException ioe) {
                    throw new RuntimeException("Error copying asset to target file: " + targetFile.getPath(), ioe);
                }
            }
        }
    }


}
