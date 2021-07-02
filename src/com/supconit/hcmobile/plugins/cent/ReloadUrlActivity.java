package com.supconit.hcmobile.plugins.cent;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.net.Uri;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.text.TextUtils;
import android.view.View;
import android.widget.EditText;
import android.widget.Toast;

import com.supconit.hcmobile.MainActivity;
import com.supconit.hcmobile.MainActivityNoSingle;
import com.supconit.hcmobile.permissions.BasePermissionsAppCompatActivityActivity;
import com.supconit.inner_hcmobile.R;

public class ReloadUrlActivity extends BasePermissionsAppCompatActivityActivity implements View.OnClickListener {

    private static final String SHARED_KEY = "hc_reload_url_key";
    private EditText ip;
    private EditText dk;
    private EditText pa;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.layout_reload);
        ip = findViewById(R.id.hc_reload_ip);
        dk = findViewById(R.id.hc_reload_dk);
        pa = findViewById(R.id.hc_reload_path);
        findViewById(R.id.hc_base_back_img).setOnClickListener(this);
        findViewById(R.id.hc_base_back_tex).setOnClickListener(this);

        SharedPreferences sharedPreferences = getSharedPreferences(SHARED_KEY, Context.MODE_PRIVATE);

        SharedPreferences sp = getSharedPreferences("supconit_hcmobile_android_for_platform", MODE_PRIVATE);

        Uri mu = null;
        try {
            mu = Uri.parse(sp.getString("launchUrl", ""));
        } catch (Exception ignored) {
        }

        String mIp = sharedPreferences.getString("hc_ip", "");
        if (TextUtils.isEmpty(mIp) && mu != null) {
            mIp = mu.getHost();
        }
        mIp = mIp != null && mIp.contains("/") ? mIp.substring(mIp.lastIndexOf("/") + 1, mIp.length()) : mIp;
        ip.setText(mIp);

        String mDk = sharedPreferences.getString("hc_dk", "");
        if (TextUtils.isEmpty(mDk) && mu != null && (mu.getPort() >= 0)) {
            mDk = String.valueOf(mu.getPort());
        }
        mDk = mDk.contains(":") ? mDk.substring(mDk.lastIndexOf(":") + 1, mDk.length()) : mDk;
        dk.setText(mDk);

        String mPa = sharedPreferences.getString("hc_pa", "");
        mPa = mPa.startsWith("/") ? mPa.substring(1) : mPa;
        if (TextUtils.isEmpty(mPa) && mu != null) {
            mPa = mu.getPath();
        }
        mPa = mPa != null && mPa.startsWith("/") ? mPa.substring(1) : mPa;
        mPa = TextUtils.isEmpty(mPa) ? mPa : mPa.replace("android_asset/www/offline", "");
        mPa = mPa != null && mPa.startsWith("/") ? mPa.substring(1) : mPa;
        pa.setText(mPa);

    }

    public void onClick(View view) {
        int i = view.getId();
        if (i == R.id.hc_base_back_img || i == R.id.hc_base_back_tex) {
            this.finish();

        } else if (i == R.id.hc_reload_config) {
            SharedPreferences sharedPreferences = getSharedPreferences(SHARED_KEY, Context.MODE_PRIVATE);
            SharedPreferences.Editor edit = sharedPreferences.edit();
            String eIp = ip.getText().toString().trim();

            String eDk = dk.getText().toString().trim();
            if (eDk.length() > 0 && !eDk.startsWith(":")) {
                eDk = ":" + eDk;
            }

            String ePa = pa.getText().toString().trim();
            if (ePa.length() > 0 && !ePa.startsWith("/")) {
                ePa = "/" + ePa;
            }

            edit.putString("hc_ip", eIp.contains("/") ? eIp.substring(eIp.lastIndexOf("/") + 1) : eIp);
            edit.putString("hc_dk", eDk.contains(":") ? eDk.substring(eDk.lastIndexOf(":") + 1) : eDk);
            edit.putString("hc_pa", ePa);
            edit.apply();

            Toast.makeText(this, "重新设置的主页为:\n" + eIp + eDk + ePa, Toast.LENGTH_LONG).show();
            SharedPreferences sp = getSharedPreferences("supconit_hcmobile_android_for_platform", MODE_PRIVATE);

            if (TextUtils.isEmpty(eIp) || TextUtils.isEmpty(eIp.trim())) {
                sp.edit().putString("launchUrl", "file:///android_asset/www/offline" + ePa).apply();
            } else {
                sp.edit().putString("launchUrl", "http://" + eIp + eDk + ePa).apply();
            }

            Intent outIntent = new Intent(ReloadUrlActivity.this,
                    MainActivityNoSingle.class);
            outIntent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK | Intent.FLAG_ACTIVITY_CLEAR_TOP
                    | Intent.FLAG_ACTIVITY_NEW_TASK);
            startActivity(outIntent);
            finish();

        }
    }


}
