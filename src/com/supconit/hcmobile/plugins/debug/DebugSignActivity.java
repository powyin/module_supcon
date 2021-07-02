package com.supconit.hcmobile.plugins.debug;

import android.content.res.Configuration;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.view.View;
import android.view.ViewTreeObserver;

import com.supconit.hcmobile.HcmobileApp;
import com.supconit.hcmobile.permissions.BasePermissionsAppCompatActivityActivity;
import com.supconit.inner_hcmobile.R;

public class DebugSignActivity extends BasePermissionsAppCompatActivityActivity {


    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.hc_mobile_activity_debug_sing);

        ViewTreeObserver.OnGlobalLayoutListener onGlobalLayoutListener = new ViewTreeObserver.OnGlobalLayoutListener() {
            @Override
            public void onGlobalLayout() {
                View contentView = findViewById(R.id.hc_mobile_activity_content);

                View view = findViewById(R.id.hc_mobile_activity_sign_error);
                int hei = (int) (1920f / 1080f * contentView.getWidth());
                if (view.getLayoutParams().height != hei) {
                    view.getLayoutParams().height = hei;
                    view.setLayoutParams(view.getLayoutParams());
                }

                view = findViewById(R.id.hc_mobile_activity_sign_error_h);
                hei = (int) (contentView.getHeight() * 0.85);
                if (view.getLayoutParams().height != hei) {
                    view.getLayoutParams().height = hei;
                    view.setLayoutParams(view.getLayoutParams());
                }


                Configuration mConfiguration = DebugSignActivity.this.getResources().getConfiguration(); //获取设置的配置信息
                int ori = mConfiguration.orientation; //获取屏幕方向
                if (ori == Configuration.ORIENTATION_LANDSCAPE) {

                    //横屏
                    findViewById(R.id.hc_mobile_activity_sign).setVisibility(View.GONE);
                    findViewById(R.id.hc_mobile_activity_sign_h).setVisibility(View.VISIBLE);
                } else if (ori == Configuration.ORIENTATION_PORTRAIT) {
                    //竖屏
                    findViewById(R.id.hc_mobile_activity_sign).setVisibility(View.VISIBLE);
                    findViewById(R.id.hc_mobile_activity_sign_h).setVisibility(View.GONE);
                }
            }
        };


        findViewById(R.id.hc_mobile_activity_sign_error).getViewTreeObserver().addOnGlobalLayoutListener(onGlobalLayoutListener);
        findViewById(R.id.hc_mobile_activity_sign_error_h).getViewTreeObserver().addOnGlobalLayoutListener(onGlobalLayoutListener);


        findViewById(R.id.hc_mobile_activity_sign_skip).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                finish();
            }
        });


        findViewById(R.id.hc_mobile_activity_sign_skip_h).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                finish();
            }
        });


    }


    @Override
    public void finish() {
        super.finish();
        overridePendingTransition(R.anim.launch_activity_in, R.anim.launch_activity_out);
    }
}
