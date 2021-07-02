package com.supconit.hcmobile.util;

import android.annotation.SuppressLint;
import android.text.TextUtils;

import java.lang.reflect.Method;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * 手机生产商
 */

public enum RomEnum {

    MIUI, // 小米
    Flyme, // 魅族
    EMUI, // 华为
    ColorOS, // OPPO
    FuntouchOS, // vivo
    SmartisanOS, // 锤子
    EUI, // 乐视
    Sense, // HTC
    AmigoOS, // 金立
    _360OS, // 奇酷360
    NubiaUI, // 努比亚
    H2OS, // 一加
    YunOS, // 阿里巴巴
    YuLong, // 酷派
    SamSung, // 三星
    Sony, // 索尼
    Lenovo, // 联想
    LG, // LG
    Google, // 原生
    Other; // CyanogenMod, Lewa OS, 百度云OS, Tencent OS, 深度OS, IUNI OS, Tapas OS, Mokee

    private int baseVersion = -1;
    private String version;

    private void setVersion(String version) {
        this.version = version;
    }

    private void setBaseVersion(int baseVersion) {
        this.baseVersion = baseVersion;
    }

    public int getBaseVersion() {
        return baseVersion;
    }

    public String getVersion() {
        return version;
    }

    public static RomEnum mType;

    //  ---------------------------------------------------------------获取---------------------------------------------------------------//

    private static final String KEY_DISPLAY_ID = "ro.build.display.id";
    private static final String KEY_BASE_OS_VERSION = "ro.build.version.base_os";
    private static final String KEY_CLIENT_ID_BASE = "ro.com.google.clientidbase";

    // 小米 : MIUI
    private static final String KEY_MIUI_VERSION = "ro.build.version.incremental"; // "7.6.15"
    private static final String KEY_MIUI_VERSION_NANE = "ro.miui.ui.version.name"; // "V8"
    private static final String KEY_MIUI_VERSION_CODE = "ro.miui.ui.version.code"; // "6"

    private static final String VALUE_MIUI_CLIENT_ID_BASE = "android-xiaomi";
    // 华为 : EMUI
    private static final String KEY_EMUI_VERSION = "ro.build.version.emui"; // "EmotionUI_3.0"
    private static final String KEY_EMUI_API_LEVEL = "ro.build.hw_emui_api_level"; //
    private static final String KEY_EMUI_SYSTEM_VERSION = "ro.confg.hw_systemversion"; // "T1-A21wV100R001C233B008_SYSIMG"
    // 魅族 : Flyme
    private static final String KEY_FLYME_PUBLISHED = "ro.flyme.published"; // "true"
    private static final String KEY_FLYME_SETUP = "ro.meizu.setupwizard.flyme"; // "true"

    private static final String VALUE_FLYME_DISPLAY_ID_CONTAIN = "Flyme"; // "Flyme OS 4.5.4.2U"
    // OPPO : ColorOS
    private static final String KEY_COLOROS_VERSION = "ro.oppo.theme.version"; // "703"
    private static final String KEY_COLOROS_THEME_VERSION = "ro.oppo.version"; // ""
    private static final String KEY_COLOROS_ROM_VERSION = "ro.rom.different.version"; // "ColorOS2.1"

    private static final String VALUE_COLOROS_BASE_OS_VERSION_CONTAIN = "OPPO"; // "OPPO/R7sm/R7sm:5.1.1/LMY47V/1440928800:user/release-keys"
    private static final String VALUE_COLOROS_CLIENT_ID_BASE = "android-oppo";
    // vivo : FuntouchOS
    private static final String KEY_FUNTOUCHOS_BOARD_VERSION = "ro.vivo.board.version"; // "MD"
    private static final String KEY_FUNTOUCHOS_OS_NAME = "ro.vivo.os.name"; // "Funtouch"
    private static final String KEY_FUNTOUCHOS_OS_VERSION = "ro.vivo.os.version"; // "3.0"
    private static final String KEY_FUNTOUCHOS_DISPLAY_ID = "ro.vivo.os.build.display.id"; // "FuntouchOS_3.0"
    private static final String KEY_FUNTOUCHOS_ROM_VERSION = "ro.vivo.rom.version"; // "rom_3.1"

    private static final String VALUE_FUNTOUCHOS_CLIENT_ID_BASE = "android-vivo";
    // Samsung
    private static final String VALUE_SAMSUNG_BASE_OS_VERSION_CONTAIN = "samsung"; // "samsung/zeroltezc/zeroltechn:6.0.1/MMB29K/G9250ZCU2DQD1:user/release-keys"
    private static final String VALUE_SAMSUNG_CLIENT_ID_BASE = "android-samsung";
    // Sony
    private static final String KEY_SONY_PROTOCOL_TYPE = "ro.sony.irremote.protocol_type"; // "2"
    private static final String KEY_SONY_ENCRYPTED_DATA = "ro.sony.fota.encrypteddata"; // "supported"

    private static final String VALUE_SONY_CLIENT_ID_BASE = "android-sonyericsson";
    // 乐视 : eui
    private static final String KEY_EUI_VERSION = "ro.letv.release.version"; // "5.9.023S"
    private static final String KEY_EUI_VERSION_DATE = "ro.letv.release.version_date"; // "5.9.023S_03111"
    private static final String KEY_EUI_NAME = "ro.product.letv_name"; // "乐1s"
    private static final String KEY_EUI_MODEL = "ro.product.letv_model"; // "Letv X500"
    // 金立 : amigo
    private static final String KEY_AMIGO_ROM_VERSION = "ro.gn.gnromvernumber"; // "GIONEE ROM5.0.16"
    private static final String KEY_AMIGO_SYSTEM_UI_SUPPORT = "ro.gn.amigo.systemui.support"; // "yes"

    private static final String VALUE_AMIGO_DISPLAY_ID_CONTAIN = "amigo"; // "amigo3.5.1"
    private static final String VALUE_AMIGO_CLIENT_ID_BASE = "android-gionee";
    // 酷派 : yulong
    private static final String KEY_YULONG_VERSION_RELEASE = "ro.yulong.version.release"; // "5.1.046.P1.150921.8676_M01"
    private static final String KEY_YULONG_VERSION_TAG = "ro.yulong.version.tag"; // "LC"

    private static final String VALUE_YULONG_CLIENT_ID_BASE = "android-coolpad";
    // HTC : Sense
    private static final String KEY_SENSE_BUILD_STAGE = "htc.build.stage"; // "2"
    private static final String KEY_SENSE_BLUETOOTH_SAP = "ro.htc.bluetooth.sap"; // "true"

    private static final String VALUE_SENSE_CLIENT_ID_BASE = "android-htc-rev";
    // LG : LG
    private static final String KEY_LG_SW_VERSION = "ro.lge.swversion"; // "D85720b"
    private static final String KEY_LG_SW_VERSION_SHORT = "ro.lge.swversion_short"; // "V20b"
    private static final String KEY_LG_FACTORY_VERSION = "ro.lge.factoryversion"; // "LGD857AT-00-V20b-CUO-CN-FEB-17-2015+0"
    // 联想
    private static final String KEY_LENOVO_DEVICE = "ro.lenovo.device"; // "phone"
    private static final String KEY_LENOVO_PLATFORM = "ro.lenovo.platform"; // "qualcomm"
    private static final String KEY_LENOVO_ADB = "ro.lenovo.adb"; // "apkctl,speedup"

    private static final String VALUE_LENOVO_CLIENT_ID_BASE = "android-lenovo";


    private static boolean containsKey(String key) {
        String value = null;
        try {
            @SuppressLint("PrivateApi") Class<?> clz = Class.forName("android.os.SystemProperties");
            Method get = clz.getMethod("get", String.class, String.class);
            value = (String) get.invoke(clz, key, null);
        } catch (Exception ignored) {
        }
        return value != null && value.length() > 0;
    }

    private static String getProperty(String key) {
        try {
            @SuppressLint("PrivateApi") Class<?> clz = Class.forName("android.os.SystemProperties");
            Method get = clz.getMethod("get", String.class, String.class);
            return (String) get.invoke(clz, key, "");
        } catch (Exception ignored) {
            return "";
        }
    }

    static {
        RomEnum rom = RomEnum.Other;
        try {
            if (containsKey(KEY_MIUI_VERSION_NANE) || containsKey(KEY_MIUI_VERSION_CODE)) {
                // MIUI
                rom = RomEnum.MIUI;
                if (containsKey(KEY_MIUI_VERSION_NANE)) {
                    String versionName = getProperty(KEY_MIUI_VERSION_NANE);
                    if (!TextUtils.isEmpty(versionName) && versionName.matches("[Vv]\\d+")) { // V8
                        try {
                            rom.setBaseVersion(Integer.parseInt(versionName.split("[Vv]")[1]));
                        } catch (Exception e) {
                            e.printStackTrace();
                        }
                    }
                }
                if (containsKey(KEY_MIUI_VERSION)) {
                    String versionStr = getProperty(KEY_MIUI_VERSION);
                    if (!TextUtils.isEmpty(versionStr) && versionStr.matches("[\\d.]+")) {
                        rom.setVersion(versionStr);
                    }
                }
            } else if (containsKey(KEY_EMUI_VERSION) || containsKey(KEY_EMUI_API_LEVEL)
                    || containsKey(KEY_EMUI_SYSTEM_VERSION)) {
                // EMUI
                rom = RomEnum.EMUI;
                if (containsKey(KEY_EMUI_VERSION)) {
                    String versionStr = getProperty(KEY_EMUI_VERSION);
                    Matcher matcher = Pattern.compile("EmotionUI_([\\d.]+)").matcher(versionStr); // EmotionUI_3.0
                    if (!TextUtils.isEmpty(versionStr) && matcher.find()) {
                        try {
                            String version = matcher.group(1);
                            rom.setVersion(version);
                            rom.setBaseVersion(Integer.parseInt(version.split("\\.")[0]));
                        } catch (Exception e) {
                            e.printStackTrace();
                        }
                    }
                }
            } else if (containsKey(KEY_FLYME_SETUP) || containsKey(KEY_FLYME_PUBLISHED)) {
                // Flyme
                rom = RomEnum.Flyme;
                if (containsKey(KEY_DISPLAY_ID)) {
                    String versionStr = getProperty(KEY_DISPLAY_ID);
                    Matcher matcher = Pattern.compile("Flyme[^\\d]*([\\d.]+)[^\\d]*").matcher(versionStr); // Flyme OS 4.5.4.2U
                    if (!TextUtils.isEmpty(versionStr) && matcher.find()) {
                        try {
                            String version = matcher.group(1);
                            rom.setVersion(version);
                            rom.setBaseVersion(Integer.parseInt(version.split("\\.")[0]));
                        } catch (Exception e) {
                            e.printStackTrace();
                        }
                    }
                }
            } else if (containsKey(KEY_COLOROS_VERSION) || containsKey(KEY_COLOROS_THEME_VERSION)
                    || containsKey(KEY_COLOROS_ROM_VERSION)) {
                // ColorOS
                rom = RomEnum.ColorOS;
                if (containsKey(KEY_COLOROS_ROM_VERSION)) {
                    String versionStr = getProperty(KEY_COLOROS_ROM_VERSION);
                    Matcher matcher = Pattern.compile("ColorOS([\\d.]+)").matcher(versionStr); // ColorOS2.1
                    if (!TextUtils.isEmpty(versionStr) && matcher.find()) {
                        try {
                            String version = matcher.group(1);
                            rom.setVersion(version);
                            rom.setBaseVersion(Integer.parseInt(version.split("\\.")[0]));
                        } catch (Exception e) {
                            e.printStackTrace();
                        }
                    }
                }
            } else if (containsKey(KEY_FUNTOUCHOS_OS_NAME) || containsKey(KEY_FUNTOUCHOS_OS_VERSION)
                    || containsKey(KEY_FUNTOUCHOS_DISPLAY_ID)) {
                // FuntouchOS
                rom = RomEnum.FuntouchOS;
                if (containsKey(KEY_FUNTOUCHOS_OS_VERSION)) {
                    String versionStr = getProperty(KEY_FUNTOUCHOS_OS_VERSION);
                    if (!TextUtils.isEmpty(versionStr) && versionStr.matches("[\\d.]+")) { // 3.0
                        try {
                            rom.setVersion(versionStr);
                            rom.setBaseVersion(Integer.parseInt(versionStr.split("\\.")[0]));
                        } catch (Exception e) {
                            e.printStackTrace();
                        }
                    }
                }
            } else if (containsKey(KEY_EUI_VERSION) || containsKey(KEY_EUI_NAME)
                    || containsKey(KEY_EUI_MODEL)) {
                // EUI
                rom = RomEnum.EUI;
                if (containsKey(KEY_EUI_VERSION)) {
                    String versionStr = getProperty(KEY_EUI_VERSION);
                    Matcher matcher = Pattern.compile("([\\d.]+)[^\\d]*").matcher(versionStr); // 5.9.023S
                    if (!TextUtils.isEmpty(versionStr) && matcher.find()) {
                        try {
                            String version = matcher.group(1);
                            rom.setVersion(version);
                            rom.setBaseVersion(Integer.parseInt(version.split("\\.")[0]));
                        } catch (Exception e) {
                            e.printStackTrace();
                        }
                    }
                }
            } else if (containsKey(KEY_AMIGO_ROM_VERSION) || containsKey(KEY_AMIGO_SYSTEM_UI_SUPPORT)) {
                // amigo
                rom = RomEnum.AmigoOS;
                if (containsKey(KEY_DISPLAY_ID)) {
                    String versionStr = getProperty(KEY_DISPLAY_ID);
                    Matcher matcher = Pattern.compile("amigo([\\d.]+)[a-zA-Z]*").matcher(versionStr); // "amigo3.5.1"
                    if (!TextUtils.isEmpty(versionStr) && matcher.find()) {
                        try {
                            String version = matcher.group(1);
                            rom.setVersion(version);
                            rom.setBaseVersion(Integer.parseInt(version.split("\\.")[0]));
                        } catch (Exception e) {
                            e.printStackTrace();
                        }
                    }
                }
            } else if (containsKey(KEY_SONY_PROTOCOL_TYPE) || containsKey(KEY_SONY_ENCRYPTED_DATA)) {
                // Sony
                rom = RomEnum.Sony;
            } else if (containsKey(KEY_YULONG_VERSION_RELEASE) || containsKey(KEY_YULONG_VERSION_TAG)) {
                // YuLong
                rom = RomEnum.YuLong;
            } else if (containsKey(KEY_SENSE_BUILD_STAGE) || containsKey(KEY_SENSE_BLUETOOTH_SAP)) {
                // Sense
                rom = RomEnum.Sense;
            } else if (containsKey(KEY_LG_SW_VERSION) || containsKey(KEY_LG_SW_VERSION_SHORT)
                    || containsKey(KEY_LG_FACTORY_VERSION)) {
                // LG
                rom = RomEnum.LG;
            } else if (containsKey(KEY_LENOVO_DEVICE) || containsKey(KEY_LENOVO_PLATFORM)
                    || containsKey(KEY_LENOVO_ADB)) {
                // Lenovo
                rom = RomEnum.Lenovo;
            } else if (containsKey(KEY_DISPLAY_ID)) {
                String displayId = getProperty(KEY_DISPLAY_ID);
                if (!TextUtils.isEmpty(displayId)) {
                    if (displayId.contains(VALUE_FLYME_DISPLAY_ID_CONTAIN)) {
                        rom = RomEnum.Flyme;
                    } else if (displayId.contains(VALUE_AMIGO_DISPLAY_ID_CONTAIN)) {
                        rom = RomEnum.AmigoOS;
                    }
                }
            } else if (containsKey(KEY_BASE_OS_VERSION)) {
                String baseOsVersion = getProperty(KEY_BASE_OS_VERSION);
                if (!TextUtils.isEmpty(baseOsVersion)) {
                    if (baseOsVersion.contains(VALUE_COLOROS_BASE_OS_VERSION_CONTAIN)) {
                        rom = RomEnum.ColorOS;
                    } else if (baseOsVersion.contains(VALUE_SAMSUNG_BASE_OS_VERSION_CONTAIN)) {
                        rom = RomEnum.SamSung;
                    }
                }
            } else if (containsKey(KEY_CLIENT_ID_BASE)) {
                String clientIdBase = getProperty(KEY_CLIENT_ID_BASE);
                switch (clientIdBase) {
                    case VALUE_MIUI_CLIENT_ID_BASE:
                        rom = RomEnum.MIUI;
                        break;
                    case VALUE_COLOROS_CLIENT_ID_BASE:
                        rom = RomEnum.ColorOS;
                        break;
                    case VALUE_FUNTOUCHOS_CLIENT_ID_BASE:
                        rom = RomEnum.FuntouchOS;
                        break;
                    case VALUE_SAMSUNG_CLIENT_ID_BASE:
                        rom = RomEnum.SamSung;
                        break;
                    case VALUE_SONY_CLIENT_ID_BASE:
                        rom = RomEnum.Sony;
                        break;
                    case VALUE_YULONG_CLIENT_ID_BASE:
                        rom = RomEnum.YuLong;
                        break;
                    case VALUE_SENSE_CLIENT_ID_BASE:
                        rom = RomEnum.Sense;
                        break;
                    case VALUE_LENOVO_CLIENT_ID_BASE:
                        rom = RomEnum.Lenovo;
                        break;
                    case VALUE_AMIGO_CLIENT_ID_BASE:
                        rom = RomEnum.AmigoOS;
                    default:
                        break;
                }
            }
        } catch (Exception ignored) { }
        mType = rom;
    }
}
