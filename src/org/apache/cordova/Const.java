package org.apache.cordova;

import java.util.Collections;
import java.util.HashSet;

public class Const {
    // 用于记录错误页面url 重新加载, 浏览器返回应该跳过这种错误页面;
    public static final HashSet<String> errorUrls = new HashSet<>(Collections.singleton("data:text/html;charset=utf-8;base64,"));
    public static String keySdk;


}
