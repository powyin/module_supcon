package com.supconit.hcmobile;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.Nullable;

import com.supconit.inner_hcmobile.R;

import java.util.Iterator;

public class EnterActivity extends Activity {
    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        // Bundle extras = getIntent().getExtras();
        Intent intent = null;

        if (getIntent() == null || getIntent().getExtras() == null) {
            intent = new Intent(EnterActivity.this, MainActivity.class);
        } else {
            String brand = android.os.Build.BRAND;
            if (brand.equals("OPPO")) {
                try {
                    intent = new Intent(EnterActivity.this, Class.forName("com.supconit.hcmobile.plugins.rongim.ConversationListActivity"));
                } catch (ClassNotFoundException e) {
                    e.printStackTrace();
                    intent = new Intent(EnterActivity.this, MainActivity.class);
                }
            } else {
                intent = new Intent(EnterActivity.this, MainActivity.class);
            }
        }
        EnterActivity.this.startActivity(intent);
        Iterator<HcmobileApp.TaskLaunch> iterator = HcmobileApp.mTasks.iterator();
        while (iterator.hasNext()) {
            try {
                HcmobileApp.TaskLaunch launch = iterator.next();
                if (launch.single) {
                    iterator.remove();
                }
                Class<?> contextClass = Class.forName(launch.targetClass);
                intent = new Intent(EnterActivity.this, contextClass);
                EnterActivity.this.startActivity(intent);
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
        finish();
    }


// todo 为保证activity退出进入动画正常 请在所以先与MainActivity启动的页面中加入
//    @Override
//    public void finish() {
//        super.finish();
//        overridePendingTransition(R.anim.launch_activity_in, R.anim.launch_activity_out);
//    }

}
