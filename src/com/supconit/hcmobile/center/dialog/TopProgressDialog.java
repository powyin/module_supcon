package com.supconit.hcmobile.center.dialog;

import android.app.Activity;
import android.app.Dialog;
import android.content.Context;
import android.graphics.drawable.ColorDrawable;
import android.view.Gravity;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.TextView;

import com.supconit.inner_hcmobile.R;

import java.lang.ref.WeakReference;
import java.util.Iterator;
import java.util.Map;
import java.util.WeakHashMap;


public class TopProgressDialog extends Dialog {
    private final static WeakHashMap<Context, WeakReference<TopProgressDialog>> impMap = new WeakHashMap<>();

    public static TopProgressDialog getInstance(Activity context) {
        if (context == null) {
            throw new RuntimeException("TopProgressDialog getInstance context == null");
        }
        Iterator<Map.Entry<Context, WeakReference<TopProgressDialog>>> iterator = impMap.entrySet().iterator();
        while (iterator.hasNext()) {
            Map.Entry<Context, WeakReference<TopProgressDialog>> next = iterator.next();
            if (next.getKey() == context) {
                TopProgressDialog imp = next.getValue().get();
                if (imp == null) {
                    iterator.remove();
                } else {
                    return imp;
                }
            }
        }
        TopProgressDialog imp = new TopProgressDialog(context);
        impMap.put(context, new WeakReference<TopProgressDialog>(imp));
        return imp;
    }

    private static final int LOADING = 0;                          //正在加载
    private static final int LOADINGFAIL = 1;                      //加载失败
    private static final int LOADINGSUCCESS = 2;                   //加载成功
    private int mStatus = -1;


    public void onLoading(String message) {
        setStatus(LOADING, message);
    }


    public void onProgress(float progress) {
        updateProgress(progress);
    }

    public void onLoadSuccess(final String message, boolean finishActivity) {
        setStatus(LOADINGSUCCESS, message);
        mFinishActivity = finishActivity;
    }

    public void onLoadFailure(final String message, boolean finishActivity) {
        setStatus(LOADINGFAIL, message);
        mFinishActivity = finishActivity;
    }


    private TextView tv_title;                                    //对话框标题
    private ImageView iv_icon;                                    //对话框左侧
    private ProgressBar iv_Progress;                                 //对话框左侧
    private ViewGroup rl_progress_dialog;                         //对话框背景
    private View v_bottom_line;                                   //对话框分隔线
    private ProgressBar pb_progress;                              //进度条

    private boolean mFinishActivity = false;
    private Activity bindActivity;


    private Runnable sFinishRunnable = new Runnable() {
        @Override
        public void run() {
            if (mFinishActivity) {
                bindActivity.finish();
            }
            try {
                dismiss();
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    };


    private TopProgressDialog(Activity activity) {
        super(activity, android.R.style.Theme_DeviceDefault_Light_Dialog_NoActionBar);
        bindActivity = activity;
        setContentView(R.layout.dialog_top_progress);
        tv_title = findViewById(R.id.tv_title);
        iv_icon = findViewById(R.id.iv_icon);
        iv_Progress = findViewById(R.id.iv_progress);
        rl_progress_dialog = findViewById(R.id.rl_progress_dialog);
        v_bottom_line = findViewById(R.id.v_bottom_line);
        pb_progress = (ProgressBar) findViewById(R.id.pb_progress);
        setCancelable(false);
        if (getWindow() != null) {
            getWindow().setBackgroundDrawable(new ColorDrawable(0));            //去除原背景
        }
        WindowManager.LayoutParams lp = getWindow().getAttributes();
        lp.flags = computeFlags(lp.flags);
        lp.gravity = Gravity.TOP;
        lp.width = -1;
        lp.height = -2;
        getWindow().setAttributes(lp);


    }


    private int computeFlags(int curFlags) {
        curFlags &= ~(
                WindowManager.LayoutParams.FLAG_IGNORE_CHEEK_PRESSES |
                        WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE |
                        WindowManager.LayoutParams.FLAG_NOT_TOUCHABLE |
                        WindowManager.LayoutParams.FLAG_WATCH_OUTSIDE_TOUCH |
                        WindowManager.LayoutParams.FLAG_LAYOUT_NO_LIMITS |
                        WindowManager.LayoutParams.FLAG_ALT_FOCUSABLE_IM |
                        WindowManager.LayoutParams.FLAG_SPLIT_TOUCH);

        curFlags |= WindowManager.LayoutParams.FLAG_FULLSCREEN;
        curFlags |= WindowManager.LayoutParams.FLAG_LAYOUT_INSET_DECOR;

        curFlags |= WindowManager.LayoutParams.FLAG_LAYOUT_IN_SCREEN;
        curFlags |= WindowManager.LayoutParams.FLAG_WATCH_OUTSIDE_TOUCH;

        return curFlags;
    }



    private void setStatus(int status, String text) {
        if (!tv_title.getText().toString().equals(text)) {
            tv_title.setText(text);
        }
        if (mStatus == status) {
            return;
        }
        mStatus = status;
        switch (status) {
            case LOADING:
                startAnimation();
                break;
            case LOADINGFAIL:
                setLoadFailureView();
                break;
            case LOADINGSUCCESS:
                setLoadSuccessView();
                break;
        }
    }

    private void updateProgress(float progress) {
        pb_progress.setProgress((int) (100 * progress));
    }

    private void startAnimation() {
        iv_icon.setVisibility(View.GONE);
        iv_Progress.setVisibility(View.VISIBLE);
        rl_progress_dialog.setBackgroundResource(R.color._ff150333);
        v_bottom_line.setBackgroundColor(0xff8AB7D3);
        tv_title.setTextColor(0xff0091EA);
        try {
            if (!isShowing()) {
                show();
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }


    private void setLoadSuccessView() {
        iv_icon.setVisibility(View.VISIBLE);
        iv_icon.setBackgroundResource(R.drawable.util_top_load_ok);
        iv_Progress.setVisibility(View.GONE);
        rl_progress_dialog.setBackgroundResource(R.color._ff2e115d);
        v_bottom_line.setBackgroundColor(0xff9AD59D);
        tv_title.setTextColor(0xff66BB6A);
        bindActivity.getWindow().getDecorView().postDelayed(sFinishRunnable, 1000);
    }


    private void setLoadFailureView() {
        iv_icon.setVisibility(View.VISIBLE);
        iv_Progress.setVisibility(View.GONE);
        iv_icon.setBackgroundResource(R.drawable.util_top_load_fail);
        rl_progress_dialog.setBackgroundResource(R.color._ffFEE0E0);
        v_bottom_line.setBackgroundColor(0xffE2A9A9);
        tv_title.setTextColor(0xffFB464A);
        pb_progress.setProgress(0);
        bindActivity.getWindow().getDecorView().postDelayed(sFinishRunnable, 1400);
    }


}