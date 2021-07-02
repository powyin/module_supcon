package com.supconit.hcmobile.widget;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.Context;
import android.content.SharedPreferences;
import android.content.res.Configuration;
import android.content.res.Resources;
import android.graphics.Point;
import android.graphics.Rect;
import android.support.annotation.AttrRes;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.util.AttributeSet;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.view.ViewTreeObserver;
import android.view.inputmethod.InputMethodManager;
import android.widget.EditText;
import android.widget.FrameLayout;


/**
 * Created by powyin on 2018/6/16.
 */

public class KeyBoardWatchLayout extends FrameLayout {
    private static int NAVIGATION_BAR_HEIGHT = -1;
    private static int STATUS_BAR_HEIGHT = -1;
    private static int LAST_SAVE_KEYBOARD_HEIGHT = 0;

    private final static String STATUS_BAR_DEF_PACKAGE = "android";
    private final static String STATUS_BAR_DEF_TYPE = "dimen";
    private final static String STATUS_BAR_NAME = "status_bar_height";
    private final static String FILE_NAME = "keyboard.common";
    private final static String KEY_KEYBOARD_HEIGHT = "sp.key.keyboard.height";

    private int mDisplayHeight;
    private int previousDisplayHeight = 0;
    private boolean isProcessKeyboardChange = false;
    private boolean lastKeyboardShowing;
    private boolean isPanelHeightTargetInit = false;
    private View mTopView = null;
    private int targetHeight = 0;


    private Point temPoint = new Point();

    private final View.OnTouchListener mOnTouchListener = new View.OnTouchListener() {
        @Override
        public boolean onTouch(View v, MotionEvent event) {
            if (event.getAction() == MotionEvent.ACTION_UP) {
                showKeyboard((EditText) v);
            }
            return false;
        }
    };

    private final ViewTreeObserver.OnGlobalLayoutListener layoutListener = new ViewTreeObserver.OnGlobalLayoutListener() {
        Rect rect = new Rect();
        @Override
        public void onGlobalLayout() {
            if(isInEditMode()){
                return;
            }
            ((Activity) getContext()).getWindowManager().getDefaultDisplay().getSize(temPoint);
            mDisplayHeight = temPoint.y;
            mTopView.getWindowVisibleDisplayFrame(rect);
            calculateKeyboardHeight(rect);
            calculateKeyboardShowing(rect);
        }

        private void calculateKeyboardHeight(Rect displayFrame) {
            // first result.
            if (previousDisplayHeight == 0) {
                previousDisplayHeight = displayFrame.bottom;
                onKeyboardRefreshHeight(getKeyboardHeight());
                return;
            }

            //int keyboardHeight = Math.abs(mTopView.getHeight() - displayHeight);
            int keyboardHeight = Math.abs(mTopView.getHeight() + displayFrame.top - displayFrame.bottom);

            // skip invalid height
            if (mDisplayHeight <= 0) {
                ((Activity) getContext()).getWindowManager().getDefaultDisplay().getSize(temPoint);
                mDisplayHeight = temPoint.y;
            }
            float minKeyBoardHei = mDisplayHeight * 0.3f;
            if (keyboardHeight <= minKeyBoardHei) {
                return;
            }

            // reSave keyboardHeight and notify
            boolean changed = saveKeyboardHeight(getContext(), keyboardHeight);
            if (changed) {
                onKeyboardRefreshHeight(getKeyboardHeight());
            }
        }

        private void calculateKeyboardShowing(Rect displayFrame) {
            boolean currentKeyboardShow;

            if (mDisplayHeight <= 0) {
                ((Activity) getContext()).getWindowManager().getDefaultDisplay().getSize(temPoint);
                mDisplayHeight = temPoint.y;
            }
            currentKeyboardShow = displayFrame.bottom < mDisplayHeight * 0.7f;

            if (lastKeyboardShowing != currentKeyboardShow && !isProcessKeyboardChange) {
                onKeyboardShowing(mTopView.findFocus(),currentKeyboardShow);
                lastKeyboardShowing = currentKeyboardShow;
            }

            if (!isPanelHeightTargetInit && !isProcessKeyboardChange) {
                onKeyboardShowing(mTopView.findFocus(),lastKeyboardShowing);
                isPanelHeightTargetInit = true;
            }

            if (lastKeyboardShowing == currentKeyboardShow) {
                isProcessKeyboardChange = false;
            }
        }
    };

    public KeyBoardWatchLayout(@NonNull Context context) {
        super(context);
        init();
    }

    public KeyBoardWatchLayout(@NonNull Context context, @Nullable AttributeSet attrs) {
        super(context, attrs);
        init();
    }

    public KeyBoardWatchLayout(@NonNull Context context, @Nullable AttributeSet attrs, @AttrRes int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        init();
    }


    // 保存键盘高度
    private boolean saveKeyboardHeight(final Context context, int keyboardHeight) {

        //    keyboardHeight -= getNavigationBarHeight(context);
        if (LAST_SAVE_KEYBOARD_HEIGHT == keyboardHeight) {
            return false;
        }
        LAST_SAVE_KEYBOARD_HEIGHT = keyboardHeight;

        SharedPreferences sp = context.getSharedPreferences(FILE_NAME, Context.MODE_PRIVATE);
        return sp.edit().putInt(KEY_KEYBOARD_HEIGHT, keyboardHeight).commit();
    }


    // 得到键盘高度
    private int getKeyboardHeight() {
        if (LAST_SAVE_KEYBOARD_HEIGHT == 0) {
            SharedPreferences sp = getContext().getSharedPreferences(FILE_NAME, Context.MODE_PRIVATE);
            LAST_SAVE_KEYBOARD_HEIGHT = sp.getInt(KEY_KEYBOARD_HEIGHT, 0);
            if (LAST_SAVE_KEYBOARD_HEIGHT == 0) {
                if (mDisplayHeight <= 0) {
                    ((Activity) getContext()).getWindowManager().getDefaultDisplay().getSize(temPoint);
                    mDisplayHeight = temPoint.y;
                }
                float minKeyBoardHei = mDisplayHeight * 0.3f;
                return (int) (minKeyBoardHei * (1.1f));
            }
        }
        return LAST_SAVE_KEYBOARD_HEIGHT;
    }


    // 得到底部透明导航栏高度
    private int getNavigationBarHeight(Context context) {
        if (NAVIGATION_BAR_HEIGHT < 0) {
            Resources resources = context.getResources();
            int resourceId = resources.getIdentifier("navigation_bar_height", "dimen", "android");
            NAVIGATION_BAR_HEIGHT = resources.getDimensionPixelSize(resourceId);
        }

        return NAVIGATION_BAR_HEIGHT;
    }

    // 得到状态栏高度
    public static int getStatusBarHeight(Context context) {
        if (STATUS_BAR_HEIGHT < 0) {
            int resourceId = context.getResources().
                    getIdentifier(STATUS_BAR_NAME, STATUS_BAR_DEF_TYPE, STATUS_BAR_DEF_PACKAGE);
            if (resourceId > 0) {
                STATUS_BAR_HEIGHT = context.getResources().getDimensionPixelSize(resourceId);
            } else {
                STATUS_BAR_HEIGHT = 0;
            }
        }
        return STATUS_BAR_HEIGHT;
    }


    private void init() {
        try {
            mTopView = ((Activity) getContext()).findViewById(android.R.id.content);
        } catch (Exception e) {
            e.printStackTrace();
            mTopView = this;
            while (mTopView.getParent() instanceof View) {
                mTopView = (View) mTopView.getParent();
            }
        }


    }

    @Override
    protected void onAttachedToWindow() {
        super.onAttachedToWindow();
        if(!isInEditMode()){
            ((Activity) getContext()).getWindow().getDecorView().getViewTreeObserver().addOnGlobalLayoutListener(layoutListener);;
        }
    }

    @Override
    protected void onDetachedFromWindow() {
        super.onDetachedFromWindow();
        if(!isInEditMode()) {
            ((Activity) getContext()).getWindow().getDecorView().getViewTreeObserver().removeOnGlobalLayoutListener(layoutListener);
        }
    }

    @Override
    protected void onConfigurationChanged(Configuration newConfig) {
        super.onConfigurationChanged(newConfig);
        if (!isInEditMode()) {
            ((Activity) getContext()).getWindowManager().getDefaultDisplay().getSize(temPoint);
            mDisplayHeight = temPoint.y;
        }
    }


// -------------------------------------------------------------------------imp-------------------------------------------------------------------//

    // 刷新键盘高度
    protected void onKeyboardRefreshHeight(int height) {
        targetHeight = height;
        ViewGroup.LayoutParams layoutParams = getLayoutParams();
        if (layoutParams.height != height) {
            layoutParams.height = height;
            setLayoutParams(layoutParams);
        }

        if (mOnKeyBoardStatusListener != null) {
            mOnKeyBoardStatusListener.onFreshKeyBoardHei(height);
        }
    }


    // 键盘高度监听
    protected void onKeyboardShowing(View focus, boolean isShow) {
        if (getVisibility() == VISIBLE || getVisibility() == INVISIBLE) {
            ViewGroup.LayoutParams layoutParams = getLayoutParams();
            if (isShow) {
                if (layoutParams.height != targetHeight) {
                    layoutParams.height = targetHeight;
                    setLayoutParams(layoutParams);
                }
            } else {
                if (layoutParams.height != 0) {
                    layoutParams.height = 0;
                    setLayoutParams(layoutParams);
                }
            }
        }
        if (mOnKeyBoardStatusListener != null) {
            mOnKeyBoardStatusListener.onShow(focus,isShow);
        }
    }

    // -------------------------------------------------------------------------common-------------------------------------------------------------------//


    // 打开键盘
    public void showKeyboard(EditText view) {
        onKeyboardShowing(view,true);
        if (!lastKeyboardShowing) {
            isProcessKeyboardChange = true;
        }

        lastKeyboardShowing = true;
        view.requestFocus();
        InputMethodManager inputManager =
                (InputMethodManager) view.getContext().getSystemService(
                        Context.INPUT_METHOD_SERVICE);
        if (inputManager != null) {
            inputManager.showSoftInput(view, 0);
        }
    }

    public boolean isKeyBoardShow() {
        return lastKeyboardShowing;
    }

    // 关闭键盘
    public void hideKeyboard() {
        InputMethodManager imm =
                (InputMethodManager) getContext().getSystemService(Context.INPUT_METHOD_SERVICE);
        View focus = ((Activity) getContext()).getCurrentFocus();
        if (focus == null || imm == null) return;
        imm.hideSoftInputFromWindow(focus.getWindowToken(), 0);
    }


    // 加入监听对象
    @SuppressLint("ClickableViewAccessibility")
    public void addWatchTarget(EditText... editText) {
        for (int i = 0; editText != null && i < editText.length; i++) {
            editText[i].setOnTouchListener(mOnTouchListener);
        }
    }


    public void setOnKeyBoardStatusListener(OnKeyBoardStatusListener listener) {
        this.mOnKeyBoardStatusListener = listener;
    }

    protected OnKeyBoardStatusListener mOnKeyBoardStatusListener;

    public interface OnKeyBoardStatusListener {
        void onShow(View focus,boolean isShow);

        void onFreshKeyBoardHei(int hei);
    }


}

