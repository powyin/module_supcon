package com.supconit.hcmobile.plugins.debug.holder;

import android.app.Activity;
import android.view.ViewGroup;
import android.widget.TextView;

import com.powyin.scroll.adapter.AdapterDelegate;
import com.powyin.scroll.adapter.PowViewHolder;
import com.supconit.inner_hcmobile.R;

public class LogItemHolder extends PowViewHolder<String> {
    public LogItemHolder(Activity activity, ViewGroup viewGroup) {
        super(activity, viewGroup);
        textView = findViewById(R.id.hc_mobile_log_id);
    }
    private TextView textView;
    @Override
    protected int getItemViewRes() {
        return R.layout.view_holder_log;
    }

    @Override
    public void loadData(AdapterDelegate<? super String> multipleAdapter, String data, int position) {
        textView.setText(data);
    }
}
