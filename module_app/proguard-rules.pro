-dontshrink #关闭压缩
-dontoptimize
-dontpreverify
-optimizationpasses 1
-keepattributes Signature
-keepattributes Exceptions,InnerClasses,Signature,Deprecated,SourceFile,LineNumberTable,*Annotation*,EnclosingMethod
-ignorewarnings
-keep enum * {*;}

-keep class * extends org.apache.cordova.CordovaPlugin {*;}
-keep class * extends com.supconit.hcmobile.appplugin.ApplicationObserver {*;}
-keep class * extends com.supconit.develop.JSApplicationCreate {*;}
-keep class * extends com.supconit.develop.JSBridge {*;}

-keep class **.R$* {*;}

-keep class * implements android.os.Parcelable {*;}
-keep class * implements java.io.Serializable {*;}
-keep class * extends java.lang.Cloneable {*;}
-keep class * extends android.view.View {*;}
-keep class * extends android.app.Activity {*;}
-keep class * extends android.support.v4.app.Fragment {*;}
-keep class * extends android.app.Dialog {*;}

-keep class com.supconit.develop.** {*;}
-dontwarn com.supconit.develop.**
-keep class com.supconit.hcmobile.plugins.hotupdate.** {*;}
-dontwarn com.supconit.hcmobile.plugins.hotupdate.**
-keep class com.supconit.hcmobile.plugins.map.** {*;}
-dontwarn com.supconit.hcmobile.plugins.map.**

-dontnote android.net.http.*
-dontnote org.apache.commons.codec.**
-dontnote org.apache.http.**

#############################################################################

-keepattributes *Annotation*
-keepclassmembers class * {
    @org.greenrobot.eventbus.Subscribe <methods>;
}

#############################################################################

#GreenDao
-keep class org.greenrobot.greendao.**{*;}
-keepclassmembers class * extends org.greenrobot.greendao.AbstractDao {
    public static java.lang.String TABLENAME;
}
-keep class **$Properties{*;}








-keep class MTT.** {*;}
-dontwarn MTT.**
-keep class android.** {*;}
-dontwarn android.**
-keep class anet.** {*;}
-dontwarn anet.**
-keep class anetwork.** {*;}
-dontwarn anetwork.**
-keep class bolts.** {*;}
-dontwarn bolts.**
-keep class cn.** {*;}
-dontwarn cn.**
-keep class com.alibaba.** {*;}
-dontwarn com.alibaba.**
-keep class com.alipay.** {*;}
-dontwarn com.alipay.**
-keep class com.amap.** {*;}
-dontwarn com.amap.**
-keep class com.android.** {*;}
-dontwarn com.android.**
-keep class com.androidquery.** {*;}
-dontwarn com.androidquery.**
-keep class com.arcsoft.** {*;}
-dontwarn com.arcsoft.**
-keep class com.asus.** {*;}
-dontwarn com.asus.**
-keep class com.autonavi.** {*;}
-dontwarn com.autonavi.**
-keep class com.baidu.** {*;}
-dontwarn com.baidu.**
-keep class com.bm.** {*;}
-dontwarn com.bm.**
-keep class com.bumptech.** {*;}
-dontwarn com.bumptech.**
-keep class com.bun.** {*;}
-dontwarn com.bun.**
-keep class com.bytedance.** {*;}
-dontwarn com.bytedance.**
-keep class com.coremedia.** {*;}
-dontwarn com.coremedia.**
-keep class com.facebook.** {*;}
-dontwarn com.facebook.**
-keep class com.fasterxml.** {*;}
-dontwarn com.fasterxml.**
-keep class com.google.** {*;}
-dontwarn com.google.**
-keep class com.googlecode.** {*;}
-dontwarn com.googlecode.**
-keep class com.gyf.** {*;}
-dontwarn com.gyf.**
-keep class com.heytap.** {*;}
-dontwarn com.heytap.**
-keep class com.hik.** {*;}
-dontwarn com.hik.**
-keep class com.hikvision.** {*;}
-dontwarn com.hikvision.**
-keep class com.huawei.** {*;}
-dontwarn com.huawei.**
-keep class com.iflytek.** {*;}
-dontwarn com.iflytek.**
-keep class com.kilo.** {*;}
-dontwarn com.kilo.**
-keep class com.koushikdutta.** {*;}
-dontwarn com.koushikdutta.**
-keep class com.kuaiyou.** {*;}
-dontwarn com.kuaiyou.**
-keep class com.kyview.** {*;}
-dontwarn com.kyview.**
-keep class com.lidroid.** {*;}
-dontwarn com.lidroid.**
-keep class com.mcs.** {*;}
-dontwarn com.mcs.**
-keep class com.meizu.** {*;}
-dontwarn com.meizu.**
-keep class com.mobile.** {*;}
-dontwarn com.mobile.**
-keep class com.mp4parser.** {*;}
-dontwarn com.mp4parser.**
-keep class com.my.** {*;}
-dontwarn com.my.**
-keep class com.nlspeech.** {*;}
-dontwarn com.nlspeech.**
-keep class com.olc.** {*;}
-dontwarn com.olc.**
-keep class com.pgl.** {*;}
-dontwarn com.pgl.**
-keep class com.powyin.** {*;}
-dontwarn com.powyin.**
-keep class com.qq.** {*;}
-dontwarn com.qq.**
-keep class com.samsung.** {*;}
-dontwarn com.samsung.**
-keep class com.sangfor.** {*;}
-dontwarn com.sangfor.**
-keep class com.sina.** {*;}
-dontwarn com.sina.**
-keep class com.squareup.** {*;}
-dontwarn com.squareup.**
-keep class com.ss.** {*;}
-dontwarn com.ss.**
-keep class com.sun.** {*;}
-dontwarn com.sun.**
-keep class com.supconit.inner_hcmobile.** {*;}
-dontwarn com.supconit.inner_hcmobile.**
-keep class com.ta.** {*;}
-dontwarn com.ta.**
-keep class com.taobao.** {*;}
-dontwarn com.taobao.**
-keep class com.tencent.** {*;}
-dontwarn com.tencent.**
-keep class com.umeng.** {*;}
-dontwarn com.umeng.**
-keep class com.ut.** {*;}
-dontwarn com.ut.**
-keep class com.vincent.** {*;}
-dontwarn com.vincent.**
-keep class com.vivo.** {*;}
-dontwarn com.vivo.**
-keep class com.xiaomi.** {*;}
-dontwarn com.xiaomi.**
-keep class com.xm.** {*;}
-dontwarn com.xm.**
-keep class com.yanzhenjie.** {*;}
-dontwarn com.yanzhenjie.**
-keep class com.zxy.** {*;}
-dontwarn com.zxy.**
-keep class io.** {*;}
-dontwarn io.**
-keep class javax.** {*;}
-dontwarn javax.**
-keep class okhttp3.** {*;}
-dontwarn okhttp3.**
-keep class okio.** {*;}
-dontwarn okio.**
-keep class org.** {*;}
-dontwarn org.**
-keep class pl.** {*;}
-dontwarn pl.**
-keep class pub.** {*;}
-dontwarn pub.**
-keep class tv.** {*;}
-dontwarn tv.**
-keep class uk.** {*;}
-dontwarn uk.**









