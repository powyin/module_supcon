package com.supconit.hcmobile.widget;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.util.AttributeSet;
import android.view.View;

/**
 * Created by yanfei on 2017-11-8.
 */

public class HCMProgressBar extends View {
    private final int MAX = 100;
    private final int MIN = 0;
    private int mProgress;
    private int width, height;
    private int bgColor, progressColor;

    private Paint paint;

    public HCMProgressBar(Context context) {
        super(context);
        init();
    }

    public HCMProgressBar(Context context, AttributeSet attrs) {
        super(context, attrs);
        init();
    }

    private void init() {
        bgColor = Color.TRANSPARENT;
        progressColor = Color.parseColor("#63B8FF");

        paint = new Paint();
        paint.setAntiAlias(true);
        paint.setStyle(Paint.Style.FILL);
    }

    public void setProgress(int progress) {
        mProgress = progress;
        invalidate();
    }

    public int getProgress() {
        return mProgress;
    }

    public void setBgColor(int color) {
        bgColor = color;
        invalidate();
    }

    public void setProgressColor(int color) {
        progressColor = color;
        invalidate();
    }

    public void startLoad() {
        setProgress(MIN);
        setVisibility(VISIBLE);
    }

    public void endLoad() {
        setProgress(MAX);
        setVisibility(GONE);
    }

    public void reset() {
        setProgress(MIN);
        setVisibility(GONE);
    }

    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);

        width = getWidth();
        height = getHeight();

        paint.setColor(bgColor);
        canvas.drawRect(0, 0, width, height, paint);
        paint.setColor(progressColor);
        canvas.drawRect(0, 0, width * mProgress / 100, height, paint);
    }
}
