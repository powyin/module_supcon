package com.supconit.hcmobile.util;

import android.text.TextUtils;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.Locale;

/**
 * 在此写用途
 *
 * @author yinhaoxiang
 * @version V1.0 <描述当前版本功能>
 * @value TimeUtil.java
 * @link
 * @since 2017-03-17 17:11:11
 */
public class TimeUtil {


    // milliseconds 时间长度 12:34:12
    public static String getTime(long milliseconds, int minTimeLenth) {

        StringBuilder stringBuffer = new StringBuilder();
        int value;
        int timeLen = 0;
        milliseconds = milliseconds / 1000;


        // 秒
        value = (int) (milliseconds % 60);
        milliseconds = milliseconds / 60;
        stringBuffer.insert(0, value < 10 ? "0" + String.valueOf(value) : String.valueOf(value));
        timeLen++;
        if (milliseconds == 0) {
            for (; timeLen < minTimeLenth; timeLen++) {
                stringBuffer.insert(0, "00:");
            }
            return stringBuffer.toString();
        }

        // 分
        value = (int) (milliseconds % 60);
        milliseconds = milliseconds / 60;
        stringBuffer.insert(0, value < 10 ? "0" + String.valueOf(value) + ":" : String.valueOf(value) + ":");
        timeLen++;
        if (milliseconds == 0) {
            for (; timeLen < minTimeLenth; timeLen++) {
                stringBuffer.insert(0, "00:");
            }
            return stringBuffer.toString();
        }


        // 小时
        value = (int) (milliseconds % 24);
        milliseconds = milliseconds % 24;
        stringBuffer.insert(0, value < 10 ? "0" + String.valueOf(value) + ":" : String.valueOf(value) + ":");
        timeLen++;
        if (milliseconds == 0) {
            for (; timeLen < minTimeLenth; timeLen++) {
                stringBuffer.insert(0, "00:");
            }
            return stringBuffer.toString();
        }


        return stringBuffer.toString();
    }

    // 1:1分钟内   2:1小时以内   3:1天以内   4:1个月以内   5:1年以内  6:一年以上
    public static int getTimeLenth(long milliseconds) {
        milliseconds = milliseconds / 1000;
        if (milliseconds < 60) return 1;

        milliseconds = milliseconds / 60;
        if (milliseconds < 60) return 2;

        milliseconds = milliseconds / 24;
        if (milliseconds < 24) return 3;

        milliseconds = milliseconds / 30;
        if (milliseconds < 30) return 4;

        milliseconds = milliseconds / 12;
        if (milliseconds < 12) return 5;

        return 6;
    }

    private static SimpleDateFormat simpleDateFormat = new SimpleDateFormat("HH:mm", Locale.getDefault());
    private static SimpleDateFormat monthDateFormat = new SimpleDateFormat("MM-dd HH:mm", Locale.getDefault());

    public static String formatTime(long formatTime) {

        int timeDiff = (int) (System.currentTimeMillis() - formatTime);

        int len = getTimeLenth(timeDiff);

        timeDiff = timeDiff / 1000;
        switch (len) {
            case 1:
                return "right now";
            case 2:
                return (timeDiff / 60) + "minute" + (timeDiff % 60) + "s ago";
            case 3:
                Calendar calendar = Calendar.getInstance();
                calendar.setTimeInMillis(formatTime);
                int T1 = calendar.get(Calendar.DAY_OF_MONTH);
                calendar.setTimeInMillis(System.currentTimeMillis());
                int T2 = calendar.get(Calendar.DAY_OF_MONTH);
                if (T1 == T2) {
                    return simpleDateFormat.format(formatTime);
                } else {
                    return "Yesterday:" + simpleDateFormat.format(formatTime);
                }
            default:
                return monthDateFormat.format(formatTime);
        }
    }


    //

    /**
     * 设置时间 15015155615156
     * monotonewang
     *
     * @param timeString
     */
    public static String setTimeStamp(String timeString) {
        //获取时间
        long time = Long.valueOf(timeString);
        Date date = new Date(time);

        Calendar cal = Calendar.getInstance();
        cal.setTime(date);
        int year = cal.get(cal.YEAR);
        int month = cal.get(cal.MONTH) + 1;
        int day = cal.get(cal.DATE);
        int hour = cal.get(cal.HOUR_OF_DAY);
        int minute = cal.get(cal.MINUTE);

        long nowTime = System.currentTimeMillis();
        Date dateNow = new Date(nowTime);
        Calendar calNow = Calendar.getInstance();
        calNow.setTime(dateNow);
        int yearNow = calNow.get(calNow.YEAR);
        int monthNow = calNow.get(calNow.MONTH) + 1;
        int dayNow = calNow.get(calNow.DATE);
        int hourNow = calNow.get(calNow.HOUR_OF_DAY);
        int minuteNow = calNow.get(calNow.MINUTE);
        if ((yearNow - year) > 0) {
            int yearX = yearNow - year;
            return String.valueOf(yearX) + "years ago";
        } else if ((monthNow - month) > 0) {
            int monthX = monthNow - month;
            return String.valueOf(monthX) + "months ago";
        } else if ((dayNow - day) > 0) {
            int dayX = dayNow - day;
            return String.valueOf(dayX) + "days ago";
        } else if ((hourNow - hour) > 0) {
            int hourX = hourNow - hour;
            return String.valueOf(hourX) + "hours ago";
        } else if ((minuteNow - minute) > 0) {
            int minuteX = minuteNow - minute;
            return String.valueOf(minuteX) + "minutes ago";
        } else {
            return "right now";
        }

    }

    /**
     * 设置时间
     * 1925-12-12 12:12:12
     *
     * @param time
     */
    public static String setTime(String time) {

        if (TextUtils.isEmpty(time))
            return "";
        SimpleDateFormat format = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
        Date date = null;
        try {
            date = format.parse(time);
        } catch (ParseException e) {
            e.printStackTrace();
        }

        Calendar cal = Calendar.getInstance();
        cal.setTime(date);
        int year = cal.get(cal.YEAR);
        int month = cal.get(cal.MONTH) + 1;
        int day = cal.get(cal.DATE);
        int hour = cal.get(cal.HOUR_OF_DAY);
        int minute = cal.get(cal.MINUTE);

        long nowTime = System.currentTimeMillis();
        Date dateNow = new Date(nowTime);
        Calendar calNow = Calendar.getInstance();
        calNow.setTime(dateNow);
        int yearNow = calNow.get(calNow.YEAR);
        int monthNow = calNow.get(calNow.MONTH) + 1;
        int dayNow = calNow.get(calNow.DATE);
        int hourNow = calNow.get(calNow.HOUR_OF_DAY);
        int minuteNow = calNow.get(calNow.MINUTE);

        if ((yearNow - year) > 0) {
            int yearX = yearNow - year;
            return String.valueOf(yearX) + "years ago";
        } else if ((monthNow - month) > 0) {
            int monthX = monthNow - month;
            return String.valueOf(monthX) + "months ago";
        } else if ((dayNow - day) > 0) {
            int dayX = dayNow - day;
            return String.valueOf(dayX) + "days ago";
        } else if ((hourNow - hour) > 0) {
            int hourX = hourNow - hour;
            return String.valueOf(hourX) + "hours ago";
        } else if ((minuteNow - minute) > 0) {
            int minuteX = minuteNow - minute;
            return String.valueOf(minuteX) + "minutes ago";
        } else {
            return "right now";
        }

    }


}






















