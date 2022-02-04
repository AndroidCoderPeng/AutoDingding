package com.pengxh.autodingding.utils;

import com.pengxh.app.multilib.widget.EasyToast;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

/**
 * @author: Pengxh
 * @email: 290677893@qq.com
 * @description: TODO
 * @date: 2020/1/17 12:57
 */
public class TimeOrDateUtil {
    private static SimpleDateFormat dateFormat;

    /**
     * 时间戳转日期
     */
    public static String rTimestampToDate(long millSeconds) {
        dateFormat = new SimpleDateFormat("yyyy-MM-dd", Locale.CHINA);
        return dateFormat.format(new Date(millSeconds));
    }

    /**
     * 时间戳转时间
     */
    public static String timestampToTime(long millSeconds) {
        dateFormat = new SimpleDateFormat("HH:mm:ss", Locale.CHINA);
        return dateFormat.format(new Date(millSeconds));
    }

    /**
     * 时间戳转详细日期时间
     */
    public static String timestampToDate(long millSeconds) {
        dateFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.CHINA);
        return dateFormat.format(new Date(millSeconds));
    }

    /**
     * 计算时间差
     *
     * @param fixedTime 结束时间
     */
    public static long deltaTime(long fixedTime) {
        long currentTime = (System.currentTimeMillis() / 1000);
        if (fixedTime > currentTime) {
            return (fixedTime - currentTime);
        } else {
            EasyToast.showToast("时间设置异常", EasyToast.WARING);
        }
        return 0L;
    }
}