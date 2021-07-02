package com.supconit.hcmobile.util;

import android.content.ComponentName;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.net.Uri;
import android.provider.MediaStore;
import android.provider.Settings;
import android.text.TextUtils;
import android.util.Log;

import com.supconit.hcmobile.HcmobileApp;


public class IntentUtil {

    /**
     * 获取跳转「图片剪裁」的意图
     *
     * @param inputUri  剪裁图片文件的 Uri
     * @param outputUri 输出图片文件的 Uri
     * @param aspectX   输出图片宽高比中的宽
     * @param aspectY   输出图片宽高比中的高
     * @param outputX   输出图片的宽
     * @param outputY   输出图片的高
     * @return 意图
     */
    public static Intent getCropPhotoIntent(Uri inputUri, Uri outputUri, int aspectX, int aspectY, int outputX, int outputY) {
        Intent intent = new Intent("com.android.camera.action.CROP");
        intent.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION);
        intent.setDataAndType(inputUri, "image/*");
        intent.putExtra("crop", "true");
        // 指定输出宽高比
        intent.putExtra("aspectX", aspectX);
        intent.putExtra("aspectY", aspectY);
        // 指定输出宽高
        intent.putExtra("outputX", outputX);
        intent.putExtra("outputY", outputY);
        // 指定输出路径和文件类型
        intent.putExtra(MediaStore.EXTRA_OUTPUT, outputUri);
        if (outputUri.getPath().endsWith(".jpg") || outputUri.getPath().endsWith(".jpeg")) {
            intent.putExtra("outputFormat", Bitmap.CompressFormat.JPEG.toString());
        } else if (outputUri.getPath().endsWith(".png") || inputUri.getPath().endsWith(".png")) {
            intent.putExtra("outputFormat", Bitmap.CompressFormat.PNG.toString());
        } else {
            intent.putExtra("outputFormat", Bitmap.CompressFormat.JPEG.toString());
        }
        intent.putExtra("return-data", false);
        return intent;
    }


    /**
     * 获取跳转「卸载应用」的意图
     *
     * @param packageName 应用包名
     * @return 意图
     */
    public static Intent getUninstallAppIntent(String packageName) {
        if (TextUtils.isEmpty(packageName)) return null;

        Intent intent = new Intent(Intent.ACTION_DELETE);
        intent.setData(Uri.parse("package:" + packageName));
        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        return intent;
    }

    /**
     * 跳转: 「权限设置」界面
     * 根据各大厂商的不同定制而跳转至其权限设置
     */
    public static Intent getPermissionSettingIntent() {
        Intent intent = new Intent();
        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        String packageName = HcmobileApp.getApplication().getPackageName();
        RomEnum romType = RomEnum.mType;
        switch (romType) {
            case EMUI: // 华为
                intent.putExtra("packageName", packageName);
                intent.setComponent(new ComponentName("com.huawei.systemmanager", "com.huawei.permissionmanager.ui.MainActivity"));
                break;
            case Flyme: // 魅族
                intent.setAction("com.meizu.safe.security.SHOW_APPSEC");
                intent.addCategory(Intent.CATEGORY_DEFAULT);
                intent.putExtra("packageName", packageName);
                break;
            case MIUI: // 小米
                intent.setAction("miui.intent.action.APP_PERM_EDITOR");
                intent.putExtra("extra_pkgname", packageName);
                intent.setClassName("com.miui.securitycenter", "com.miui.permcenter.permissions.PermissionsEditorActivity");
                if (HcmobileApp.getApplication().getPackageManager().queryIntentActivities(intent, PackageManager.MATCH_DEFAULT_ONLY).size() > 0) {
                    break;
                }
                intent.setClassName("com.miui.securitycenter", "com.miui.permcenter.permissions.AppPermissionsEditorActivity");
                break;
            case Sony: // 索尼
                intent.putExtra("packageName", packageName);
                intent.setComponent(new ComponentName("com.sonymobile.cta", "com.sonymobile.cta.SomcCTAMainActivity"));
                break;
            case ColorOS: // OPPO
                intent.putExtra("packageName", packageName);
                intent.setComponent(new ComponentName("com.coloros.safecenter", "com.coloros.safecenter.permission.PermissionManagerActivity"));
                break;
            case EUI: // 乐视
                intent.putExtra("packageName", packageName);
                intent.setComponent(new ComponentName("com.letv.android.letvsafe", "com.letv.android.letvsafe.PermissionAndApps"));
                break;
            case LG: // LG
                intent.setAction("android.intent.action.MAIN");
                intent.putExtra("packageName", packageName);
                ComponentName comp = new ComponentName("com.android.settings", "com.android.settings.Settings$AccessLockSummaryActivity");
                intent.setComponent(comp);
                break;
            case SamSung: // 三星
            case SmartisanOS: // 锤子
                intent.setAction(Settings.ACTION_APPLICATION_DETAILS_SETTINGS)
                        .setData(Uri.parse("package:" + packageName))
                        .addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                break;
            default:
                intent.setAction(Settings.ACTION_APPLICATION_DETAILS_SETTINGS)
                        .setData(Uri.parse("package:" + packageName))
                        .addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                Log.d("powyin", "没有找到该机型");
                break;
        }

        if (!(HcmobileApp.getApplication().getPackageManager().queryIntentActivities(intent, PackageManager.MATCH_DEFAULT_ONLY).size() > 0)) {
            intent = new Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS);
            intent.setData(Uri.fromParts("package", packageName, null));
            intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
            Log.d("powyin", "没有适配该机型, 跳转普通设置界面");
        }

        return intent;
    }


}
