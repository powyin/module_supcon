package com.supconit.hcmobile.permissions;

import android.app.Activity;
import android.content.Context;
import android.content.pm.PackageManager;
import android.content.res.XmlResourceParser;
import android.os.Build;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.ActivityCompat;
import android.text.TextUtils;
import android.util.Log;

import com.gyf.immersionbar.ImmersionBar;
import com.supconit.hcmobile.HcmobileApp;

import org.xmlpull.v1.XmlPullParser;

import java.util.HashMap;
import java.util.Map;

import io.reactivex.subjects.PublishSubject;

// 用于简化操作应用授权 引入rxJava

public abstract class BasePermissionsActivity extends Activity implements PermissionsActivityPart {

    private static final int PERMISSIONS_REQUEST_CODE = 42;
    private boolean isImmersionShow = false;
    private Map<String, PublishSubject<Permission>> mSubjects = new HashMap<>();

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (!onInterceptImmersion() && "true".equals(getCordovaConfigTag("layout_immersion", "value"))) {
            isImmersionShow = true;
            String color = getCordovaConfigTag("StatusBarStyle", "value");
            if ("1".equals(color)) {
                ImmersionBar.with(this).statusBarDarkFont(true).init();
            } else if ("2".equals(color)) {
                ImmersionBar.with(this).statusBarDarkFont(false).init();
            } else {
                ImmersionBar.with(this).init();
            }
        }

    }

    public boolean onInterceptImmersion() {
        return false;
    }

    public boolean isImmersionShow() {
        return isImmersionShow;
    }

    @Override
    public void requestPermissions(@NonNull String[] permissions) {
        ActivityCompat.requestPermissions(this, permissions, PERMISSIONS_REQUEST_CODE);
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String permissions[], @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);

        if (requestCode != PERMISSIONS_REQUEST_CODE) return;
        boolean[] shouldShowRequestPermissionRationale = new boolean[permissions.length];
        for (int i = 0; i < permissions.length; i++) {
            shouldShowRequestPermissionRationale[i] = ActivityCompat.shouldShowRequestPermissionRationale(this, permissions[i]);
        }
        onRequestPermissionsResult(permissions, grantResults, shouldShowRequestPermissionRationale);
    }

    void onRequestPermissionsResult(String permissions[], int[] grantResults, boolean[] shouldShowRequestPermissionRationale) {
        for (int i = 0, size = permissions.length; i < size; i++) {
            Log.d("ask permission", "onRequestPermissionsResult  " + permissions[i]);
            // Find the corresponding subject
            PublishSubject<Permission> subject = mSubjects.get(permissions[i]);
            if (subject == null) {
                // No subject found
                Log.e("ask permission", "RxPermissions.onRequestPermissionsResult invoked but didn't find the corresponding permission request.");
                return;
            }
            mSubjects.remove(permissions[i]);
            boolean granted = grantResults[i] == PackageManager.PERMISSION_GRANTED;
            subject.onNext(new Permission(permissions[i], granted, shouldShowRequestPermissionRationale[i]));
            subject.onComplete();
        }
    }


    @Override
    public boolean isGranted(String permission) {
        return ActivityCompat.checkSelfPermission(this, permission) == PackageManager.PERMISSION_GRANTED;
    }


    @Override
    public boolean isRevoked(String permission) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            return getPackageManager().isPermissionRevokedByPolicy(permission, this.getPackageName());
        }
        return false;
    }


    public PublishSubject<Permission> getSubjectByPermission(@NonNull String permission) {
        return mSubjects.get(permission);
    }

    public boolean containsByPermission(@NonNull String permission) {
        return mSubjects.containsKey(permission);
    }

    public void setSubjectForPermission(@NonNull String permission, @NonNull PublishSubject<Permission> subject) {
        mSubjects.put(permission, subject);
    }


    /**
     * 匹配res/xml/config.xml 文件元素
     */
    protected static String getCordovaConfigTag(String tagName, String attributeName) {
        if (TextUtils.isEmpty(tagName) || TextUtils.isEmpty(attributeName)) {
            return null;
        }
        Context context = HcmobileApp.getApplication();
        int id = context.getResources().getIdentifier("config", "xml", context.getPackageName());
        XmlResourceParser xml = context.getResources().getXml(id);
        int eventType = -1;
        while (eventType != XmlPullParser.END_DOCUMENT) {
            if (eventType == XmlPullParser.START_TAG) {
                String strNode = xml.getName();
                if (strNode.equals(tagName)) {
                    String value = xml.getAttributeValue(null, attributeName);
                    if (value != null) {
                        return value;
                    }
                }
            }
            try {
                eventType = xml.next();
            } catch (Exception e) {
                e.printStackTrace();
                break;
            }
        }
        return null;
    }

}
