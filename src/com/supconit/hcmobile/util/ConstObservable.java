package com.supconit.hcmobile.util;

import com.supconit.hcmobile.center.model.EventExcuteJSString;

import io.reactivex.subjects.PublishSubject;

public class ConstObservable {

    // weex环境运行外部js cordova环境运行外部js 事件源
    public static PublishSubject<EventExcuteJSString> jsBridge = PublishSubject.create();



}
