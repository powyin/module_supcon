package com.supconit.hcmobile;

import android.annotation.TargetApi;
import android.content.Intent;
import android.os.Build;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.view.View;
import android.widget.EditText;
import android.widget.TextView;

import com.gyf.immersionbar.ImmersionBar;
import com.supconit.hcmobile.permissions.BasePermissionsActivity;
import com.supconit.hcmobile.permissions.BasePermissionsAppCompatActivityActivity;
import com.supconit.inner_hcmobile.R;

public class Test extends BasePermissionsAppCompatActivityActivity {
    EditText ipConfig;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.test);
        TextView textView = findViewById(R.id.edit_ip);
        textView.setText("aaaaaaaa");
//        ipConfig = findViewById(R.id.edit_ip);
//        SharedPreferences sharedPreferences = getSharedPreferences("powyin_shar_edit", Context.MODE_PRIVATE);
//        String ip_edit = sharedPreferences.getString("ip_edit", "http://baidu.com");
//        ip_edit = "http://192.168.2.26:8080/offline2/index.html";
//        ip_edit = "http://10.10.100.68:8080/zhyy/mobile/index";
//     //   ip_edit = "http://10.10.19.39:8080/zhyy/login";
//     //   ip_edit = "http://10.10.21.17:8081/TestApp/assets/index.html";
//     //   ip_edit = "http://baidu.com";
//        ipConfig.setText(ip_edit);

        ImmersionBar.with(this).init();


//        getWindow().getDecorView().setSystemUiVisibility(View.SYSTEM_UI_FLAG_IMMERSIVE_STICKY|View.SYSTEM_UI_FLAG_LAYOUT_HIDE_NAVIGATION);



    }



    public void onClick(View view) {
        Intent intent;
        finish();
    }

    @Override
    public void finish() {
        super.finish();
        // overridePendingTransition(R.anim.launch_activity_in, R.anim.launch_activity_out);
    }

}
