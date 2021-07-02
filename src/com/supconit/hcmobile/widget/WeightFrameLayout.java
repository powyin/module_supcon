package com.supconit.hcmobile.widget;

import android.content.Context;
import android.content.res.TypedArray;
import android.support.annotation.AttrRes;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.util.AttributeSet;
import android.view.View;
import android.widget.FrameLayout;

import com.supconit.inner_hcmobile.R;


/**
 * Created by powyin on 2017/5/31.
 */

public class WeightFrameLayout extends FrameLayout {
    private String TAG = "WeightFrameLayout";
    // 0 : 高度对齐宽度  1：宽度对齐高度
    int model = 0;
    // 宽度占比
    float widWeight = 1;
    // 高度占比
    float heiWeight = 1;

    public WeightFrameLayout(@NonNull Context context) {
        super(context);
    }

    public WeightFrameLayout(@NonNull Context context, @Nullable AttributeSet attrs) {
        super(context, attrs);
        TypedArray ta = context.obtainStyledAttributes(attrs, R.styleable.WeightFrameLayout);
        model = ta.getInt(R.styleable.WeightFrameLayout_align_orientation, 0);
        widWeight = ta.getFloat(R.styleable.WeightFrameLayout_wid_weight, -1);
        heiWeight = ta.getFloat(R.styleable.WeightFrameLayout_hei_weight, -1);
        ta.recycle();
    }

    public WeightFrameLayout(@NonNull Context context, @Nullable AttributeSet attrs, @AttrRes int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        TypedArray ta = context.obtainStyledAttributes(attrs, R.styleable.WeightFrameLayout);
        model = ta.getInt(R.styleable.WeightFrameLayout_align_orientation, 0);
        widWeight = ta.getFloat(R.styleable.WeightFrameLayout_wid_weight, -1);
        heiWeight = ta.getFloat(R.styleable.WeightFrameLayout_hei_weight, -1);
        ta.recycle();
    }


    public void setMeasureWei(float wid, float hei) {
        this.widWeight = wid;
        this.heiWeight = hei;
        this.invalidate();
    }


    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        super.onMeasure(widthMeasureSpec, heightMeasureSpec);
        if (widWeight > 0) {
            View parent = (View) getParent();
            int wei = (parent == null) ? 0 : parent.getWidth();
            wei = (wei > 0) ? wei : MeasureSpec.getSize(widthMeasureSpec);
            widthMeasureSpec = MeasureSpec.makeMeasureSpec((int) (widWeight * wei), MeasureSpec.getMode(widthMeasureSpec));
        }

        if (heiWeight > 0) {
            View parent = (View) getParent();
            int hei = (parent == null) ? 0 : parent.getHeight();
            hei = (hei > 0) ? hei : MeasureSpec.getSize(heightMeasureSpec);
            heightMeasureSpec = MeasureSpec.makeMeasureSpec((int) (heiWeight * hei), MeasureSpec.getMode(heightMeasureSpec));
        }

        switch (model) {
            case 0:
                heightMeasureSpec = MeasureSpec.makeMeasureSpec(getMeasuredWidth(), MeasureSpec.EXACTLY);
                super.onMeasure(widthMeasureSpec, heightMeasureSpec);
                break;
            case 1:
                widthMeasureSpec = MeasureSpec.makeMeasureSpec(getMeasuredHeight(), MeasureSpec.EXACTLY);
                super.onMeasure(widthMeasureSpec, heightMeasureSpec);
                break;
            default:
        }
    }
}
