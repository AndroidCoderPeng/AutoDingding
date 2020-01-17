package com.pengxh.autodingding.utils;

import android.annotation.SuppressLint;

import com.pengxh.app.multilib.widget.EasyToast;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;

/**
 * @author: Pengxh
 * @email: 290677893@qq.com
 * @description: TODO
 * @date: 2020/1/17 12:57
 */
public class TimeOrDateUtil {
    private static final String TAG = "TimeOrDateUtil";
    @SuppressLint("SimpleDateFormat")
    private static final SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");

    /**
     * 时间戳转时间
     */
    public static String timestampToDate(long millSeconds) {
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

    /**
     * 时间转时间戳
     */
    public static long DateToTimestamp(String date) throws ParseException {
        return dateFormat.parse(date).getTime();
    }
}
