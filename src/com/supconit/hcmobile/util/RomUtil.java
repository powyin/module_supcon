package com.supconit.hcmobile.util;

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.SharedPreferences;
import android.provider.Settings;
import android.telephony.TelephonyManager;

import com.supconit.hcmobile.HcmobileApp;

import java.io.BufferedReader;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.nio.charset.StandardCharsets;
import java.util.UUID;

public class RomUtil {
    private static final String MEM_INFO_PATH = "/proc/meminfo";
    private static final String MEMTOTAL = "MemTotal";
    private static int mMemory = -1;

    private static final String PREFS_FILE = "device_id_supcon";
    private static final String PREFS_DEVICE_ID = "device_id";
    private static volatile UUID uuid;

    public static int getTotalMemory() {
        if (mMemory == -1) {
            mMemory = getMemInfoInfo();
        }
        return mMemory;
    }

    private static int getMemInfoInfo() {
        try {
            FileReader fileReader = new FileReader(MEM_INFO_PATH);
            BufferedReader bufferedReader = new BufferedReader(fileReader, 4 * 1024);
            String str = null;
            while ((str = bufferedReader.readLine()) != null) {
                if (str.contains(MEMTOTAL)) {
                    break;
                }
            }
            bufferedReader.close();
            assert str != null;
            System.out.println(str);
            String[] array = str.split("\\s+");
            float length = Integer.valueOf(array[1]) / 1024f / 1024f;
            return Math.round(length);
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }
        return 0;
    }


    @SuppressLint("HardwareIds")
    public static String getDeviceId() {
        if (uuid == null) {
            synchronized (PREFS_FILE) {
                if (uuid == null) {
                    final SharedPreferences prefs = HcmobileApp.getApplication().getSharedPreferences(PREFS_FILE, 0);
                    final String id = prefs.getString(PREFS_DEVICE_ID, null);
                    if (id != null) {
                        uuid = UUID.fromString(id);
                    } else {
                        final String androidId = Settings.Secure.getString(HcmobileApp.getApplication().getContentResolver(), Settings.Secure.ANDROID_ID);
                        if (!"9774d56d682e549c".equals(androidId)) {
                            uuid = UUID.nameUUIDFromBytes(androidId.getBytes(StandardCharsets.UTF_8));
                        } else {
                            uuid = UUID.randomUUID();
                        }
                        prefs.edit().putString(PREFS_DEVICE_ID, uuid.toString()).apply();
                    }
                }
            }
        }
        return uuid.toString();
    }
}
