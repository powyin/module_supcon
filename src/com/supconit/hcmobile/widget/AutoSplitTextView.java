package com.supconit.hcmobile.widget;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.support.annotation.Nullable;
import android.support.v7.widget.AppCompatTextView;
import android.util.AttributeSet;
import android.util.Log;

/**
 * 解决半角字符，英文等显示补全自动换行
 */

public class AutoSplitTextView extends AppCompatTextView {
    public AutoSplitTextView(Context context) {
        this(context, null);
    }

    public AutoSplitTextView(Context context, @Nullable AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public AutoSplitTextView(Context context, @Nullable AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
    }

    //解决首次渲染，没有补全的bug。
    int mWidth = -1;
    private String autoText;

    @Override
    protected void onDraw(Canvas canvas) {
        if (mWidth != getWidth() || !autoText.equals(getText().toString())) {
            Log.d("AutoSplitTextView", "autoText...");
            autoText = autoSplitText();
            setText(autoText);
            mWidth = getWidth();
        }
        super.onDraw(canvas);

    }

    private String autoSplitText() {
        CharSequence rawCharSequence = getText();
        String originText = rawCharSequence.toString();//获取原始文本
        float textWidth = getWidth() - getPaddingLeft() - getPaddingRight();
        float textHeight = getHeight();
        Paint textPaint = getPaint();
        Log.d("AutoSplitTextView", "textWidth = " + textWidth + ", textHeight = " + textHeight);
        String[] allTextLines = originText.replaceAll("\r", "").split("\n");
        StringBuilder stringBuilder = new StringBuilder();
        for (String nextLine : allTextLines) {
            if (textPaint.measureText(nextLine) <= textWidth) {
                stringBuilder.append(nextLine).append("\n");
            } else {
                float lineWidth = 0;
                //如果整行宽度超过控件所用宽度，则按字符测量，在超过可用宽度的最后一个字符
                for (int i = 0; i < nextLine.length(); i++) {
                    char[] cc = new char[2];
                    cc[0] = nextLine.charAt(i);
                    if (Character.isHighSurrogate(cc[0]) || Character.isLowSurrogate(cc[0])) {
                        if (i < nextLine.length()) {
                            cc[1] = nextLine.charAt(i + 1);
                            i++;
                        }
                    }
                    String keepWord = cc[1] == 0 ? String.valueOf(cc[0]) : new String(cc);
                    lineWidth += textPaint.measureText(keepWord);
                    if (lineWidth > textWidth) {
                        stringBuilder.append("\n");
                        lineWidth = 0;
                    }
                    stringBuilder.append(keepWord);
                }
                stringBuilder.append("\n");
            }
        }
        if (stringBuilder.length() > 0) {
            stringBuilder.setLength(stringBuilder.length() - 1);
        }
        return stringBuilder.toString();
    }
}
