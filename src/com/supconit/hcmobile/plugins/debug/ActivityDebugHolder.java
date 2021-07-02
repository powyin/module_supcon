package com.supconit.hcmobile.plugins.debug;

import android.annotation.SuppressLint;
import android.graphics.Rect;
import android.support.v4.view.PagerAdapter;
import android.support.v4.view.ViewPager;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.text.TextUtils;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.view.ViewTreeObserver;
import android.view.inputmethod.EditorInfo;
import android.widget.EditText;
import android.widget.FrameLayout;
import android.widget.TabHost;
import android.widget.TextView;
import android.widget.Toast;

import com.powyin.scroll.adapter.MultipleRecycleAdapter;
import com.powyin.slide.widget.OnItemClickListener;
import com.powyin.slide.widget.SlideSwitch;
import com.supconit.hcmobile.MainActivity;
import com.supconit.hcmobile.model.ConsoleMs;
import com.supconit.hcmobile.plugins.debug.holder.LogItemHolder;
import com.supconit.hcmobile.plugins.debug.project.WorkSpace;
import com.supconit.hcmobile.util.NetUtil;
import com.supconit.inner_hcmobile.R;

import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.disposables.Disposable;
import io.reactivex.functions.Consumer;

public class ActivityDebugHolder {

    private RecyclerView recyclerView;

    private MainActivity mainActivity;
    private ViewGroup main;

    public ActivityDebugHolder(MainActivity activity) {
        mainActivity = activity;
        initDebugMode();
    }

    @SuppressWarnings("unchecked")
    private <K extends View> K findViewById(int resId) {
        return (K) main.findViewById(resId);
    }


    // 调试控制相关
    @SuppressLint("CheckResult")
    private void initDebugMode() {
        ViewGroup content = mainActivity.findViewById(android.R.id.content);
        main = (ViewGroup) mainActivity.getLayoutInflater().inflate(R.layout.hc_mobile_layout_dev_show, content, false);
        content.addView(main, new FrameLayout.LayoutParams(-1, -1));


        findViewById(R.id.hc_mobile_debug_sin_page_block_1).getViewTreeObserver().addOnGlobalLayoutListener(new ViewTreeObserver.OnGlobalLayoutListener() {
            @Override
            public void onGlobalLayout() {
                View block = findViewById(R.id.hc_mobile_debug_sin_page_block_1);
                if (block.getLayoutParams().height != ((View) (block.getParent())).getHeight() * 2 / 3) {
                    block.getLayoutParams().height = ((View) (block.getParent())).getHeight() * 2 / 3;
                    block.setLayoutParams(block.getLayoutParams());
                }
            }
        });

        findViewById(R.id.hc_mobile_debug_sin_page_block).setOnTouchListener(new View.OnTouchListener() {

            private float x;
            private float y;
            private float orX;
            private float orY;
            private Rect rect = new Rect();

            @Override
            public boolean onTouch(View v, MotionEvent event) {
                switch (event.getAction()) {
                    case MotionEvent.ACTION_DOWN:
                        x = event.getRawX();
                        y = event.getRawY();
                        orX = v.getTranslationX();
                        orY = v.getTranslationY();
                        break;
                    case MotionEvent.ACTION_MOVE:
                        float dx = event.getRawX();
                        float dy = event.getRawY();
                        float oriX = v.getTranslationX();
                        float oriY = v.getTranslationY();
                        v.setTranslationX(dx - x + orX);
                        v.setTranslationY(dy - y + orY);
                        rect.set(0, 0, 0, 0);
                        ViewGroup group = (ViewGroup) v.getParent().getParent();
                        group.offsetDescendantRectToMyCoords(v, rect);
                        if ((rect.left + (int) v.getTranslationX() < 0) || (group.getWidth() - (rect.left + v.getWidth() + (int) v.getTranslationX()) < 0)) {
                            v.setTranslationX(oriX);
                        }
                        if ((rect.top + (int) v.getTranslationY() < 0 || group.getHeight() - (rect.top + v.getHeight() + (int) v.getTranslationY()) < 0)) {
                            v.setTranslationY(oriY);
                        }
                        break;
                    case MotionEvent.ACTION_UP:
                        if (Math.abs(event.getRawY() - y) + Math.abs(event.getRawX() - x) < v.getWidth() * 0.2f) {
                            onClick(v);
                        }
                        break;
                }
                return true;
            }

            void onClick(View v) {
                findViewById(R.id.hc_mobile_debug_sin_page_block_0).setVisibility(View.VISIBLE);
                findViewById(R.id.hc_mobile_debug_sin_page_block_1).setVisibility(View.VISIBLE);
                TextView address = findViewById(R.id.hc_mobile_debug_sin_address);
                String addressText = "http://" + NetUtil.getIP() + ":" + ServerObserver.serverPort;
                addressText += " (局域网内pc端访问)";
                address.setText(addressText);
            }
        });
        findViewById(R.id.hc_mobile_debug_sin_page_block_0).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                findViewById(R.id.hc_mobile_debug_sin_page_block_0).setVisibility(View.GONE);
                findViewById(R.id.hc_mobile_debug_sin_page_block_1).setVisibility(View.GONE);
            }
        });
        findViewById(R.id.hc_mobile_debug_sin_page_block_1).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // skip
            }
        });
        findViewById(R.id.hc_mobile_debug_sin_page_block_0).setVisibility(View.GONE);
        findViewById(R.id.hc_mobile_debug_sin_page_block_1).setVisibility(View.GONE);


        // case ViewPage and ViewSwitch
        SlideSwitch slideSwitch = findViewById(R.id.hc_mobile_debug_sin_slide);
        ViewPager viewPager = findViewById(R.id.hc_mobile_debug_sin_page);
        viewPager.setAdapter(new PagerAdapter() {
            @Override
            public int getCount() {
                return 2;
            }

            @Override
            public boolean isViewFromObject(View view, Object object) {
                return view == object;
            }

            @Override
            public Object instantiateItem(ViewGroup container, int position) {
                switch (position) {
                    case 0:
                        return findViewById(R.id.hc_mobile_debug_sin_page_0);
                    case 1:
                        return findViewById(R.id.hc_mobile_debug_sin_page_1);
                }
                return null;
            }
        });
        viewPager.addOnPageChangeListener(slideSwitch.getSupportOnPageChangeListener());
        slideSwitch.setOnItemClickListener(new OnItemClickListener() {
            @Override
            public void onItemClicked(int position, View view) {
                TextView textView;
                switch (position) {
                    case 0:
                        textView = findViewById(R.id.hc_mobile_debug_sin_slide_0);
                        textView.setTextColor(0xff0179ff);
                        textView = findViewById(R.id.hc_mobile_debug_sin_slide_1);
                        textView.setTextColor(0xff505251);
                        break;
                    case 1:
                        textView = findViewById(R.id.hc_mobile_debug_sin_slide_0);
                        textView.setTextColor(0xff505251);
                        textView = findViewById(R.id.hc_mobile_debug_sin_slide_1);
                        textView.setTextColor(0xff0179ff);
                        break;
                }
                viewPager.setCurrentItem(position);
            }
        });


        // todo 0 :

        recyclerView = findViewById(R.id.hc_mobile_debug_sin_recycler);
        final MultipleRecycleAdapter<String> multipleRecycleAdapter = MultipleRecycleAdapter.getByViewHolder(mainActivity, LogItemHolder.class);
        recyclerView.setAdapter(multipleRecycleAdapter);
        recyclerView.setLayoutManager(new LinearLayoutManager(mainActivity));
        mainActivity.consoleMessagePublishSubject
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(new Consumer<ConsoleMs>() {
                    @Override
                    public void accept(ConsoleMs consoleMs) throws Exception {
                        int postion = multipleRecycleAdapter.getDataCount();
                        multipleRecycleAdapter.addData(postion, consoleMs.index + ":" + consoleMs.message);
                        multipleRecycleAdapter.notifyItemInserted(postion);
                    }
                });

        findViewById(R.id.hc_mobile_activity_sign_qk).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                multipleRecycleAdapter.clearData();
                multipleRecycleAdapter.notifyDataSetChanged();
            }
        });
        findViewById(R.id.hc_mobile_activity_sign_qx_0).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                findViewById(R.id.hc_mobile_debug_sin_page_block_0).setVisibility(View.GONE);
                findViewById(R.id.hc_mobile_debug_sin_page_block_1).setVisibility(View.GONE);
            }
        });

        // todo 1 :


        findViewById(R.id.hc_mobile_activity_sign_wc).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                TextView textView = findViewById(R.id.hc_mobile_password_0);
                String trim = textView.getText().toString().trim();
                textView = findViewById(R.id.hc_mobile_password_1);
                String trim2 = textView.getText().toString().trim();
                if (TextUtils.isEmpty(trim) || TextUtils.isEmpty(trim2)) {
                    Toast.makeText(mainActivity, "请输入密码", Toast.LENGTH_SHORT).show();
                    return;
                }

                if (!trim.equals(trim2)) {
                    Toast.makeText(mainActivity, "两次输入密码不一致", Toast.LENGTH_SHORT).show();
                    return;
                }

                WorkSpace.resetPassWord(trim);
                Toast.makeText(mainActivity, "修改密码成功", Toast.LENGTH_SHORT).show();
                textView = findViewById(R.id.hc_mobile_password_0);
                textView.setText("");
                textView = findViewById(R.id.hc_mobile_password_1);
                textView.setText("");
            }
        });


        findViewById(R.id.hc_mobile_activity_sign_qx_1).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                findViewById(R.id.hc_mobile_debug_sin_page_block_0).setVisibility(View.GONE);
                findViewById(R.id.hc_mobile_debug_sin_page_block_1).setVisibility(View.GONE);
            }
        });
        findViewById(R.id.hc_mobile_password_text_0).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                v.setSelected(!v.isSelected());
                if (v.isSelected()) {
                    EditText editText = findViewById(R.id.hc_mobile_password_0);
                    editText.setInputType(EditorInfo.TYPE_TEXT_VARIATION_PASSWORD);
                    editText.invalidate();
                } else {
                    EditText editText = findViewById(R.id.hc_mobile_password_0);
                    editText.setInputType(129);
                    editText.invalidate();
                }
            }
        });
        findViewById(R.id.hc_mobile_password_text_1).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                v.setSelected(!v.isSelected());
                if (v.isSelected()) {
                    EditText editText = findViewById(R.id.hc_mobile_password_1);
                    editText.setInputType(EditorInfo.TYPE_TEXT_VARIATION_PASSWORD);
                    editText.invalidate();
                } else {
                    EditText editText = findViewById(R.id.hc_mobile_password_1);
                    editText.setInputType(129);
                    editText.invalidate();
                }
            }
        });


    }


}
