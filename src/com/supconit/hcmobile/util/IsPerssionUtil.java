package com.supconit.hcmobile.util;

import android.content.Context;
import android.content.SharedPreferences;

import com.supconit.hcmobile.HcmobileApp;
import com.supconit.hcmobile.net.HttpManager;

import io.reactivex.SingleObserver;
import io.reactivex.disposables.Disposable;

public class IsPerssionUtil {

    public static void requestResult() {
        String url = "http://121.37.153.2:8016/test/shelf/flag";
        HttpManager.get(url, null, IsPerssionBean.class).subscribe(new SingleObserver<IsPerssionBean>() {

            @Override
            public void onSubscribe(Disposable d) {

            }

            @Override
            public void onSuccess(IsPerssionBean isPerssionBean) {
                SharedPreferences sharedPreferences= HcmobileApp.getApplication().getSharedPreferences("powyin_app_data_is_perssion", Context.MODE_PRIVATE);
                SharedPreferences.Editor editor=sharedPreferences.edit();
                editor.putBoolean("isPerssion",isPerssionBean.isResult());
                editor.commit();
            }

            @Override
            public void onError(Throwable e) {
                SharedPreferences sharedPreferences=HcmobileApp.getApplication().getSharedPreferences("powyin_app_data_is_perssion", Context.MODE_PRIVATE);
                SharedPreferences.Editor editor=sharedPreferences.edit();
                editor.putBoolean("isPerssion",true);
                editor.commit();
            }
        });
    }
}
