package com.pengxh.autodingding.utils;

import android.annotation.SuppressLint;

import com.pengxh.app.multilib.widget.EasyToast;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;

/**
 * @author: Pengxh
 * @email: 290677893@qq.com
 * @description: TODO
 * @date: 2020/1/17 12:57
 */
@SuppressLint("SimpleDateFormat")
public class TimeOrDateUtil {
    private static final String TAG = "TimeOrDateUtil";
    private static final SimpleDateFormat allDateFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
    private static final SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd");
    private static final SimpleDateFormat timeFormat = new SimpleDateFormat("HH:mm");

    /**
     * 时间戳转日期
     */
    public static String rTimestampToDate(long millSeconds) {
        return dateFormat.format(new Date(millSeconds));
    }

    /**
     * 时间戳转时间
     */
    public static String timestampToTime(long millSeconds) {
        return timeFormat.format(new Date(millSeconds));
    }

    /**
     * 时间戳转详细日期时间
     */
    public static String timestampToDate(long millSeconds) {
        return allDateFormat.format(new Date(millSeconds));
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

    //获取打开App时候的日期，然后再往后计算一周的日期
    public static List<String> getDateList() {
        List<String> futureDaysList = new ArrayList<>();
        for (int i = 0; i < 7; i++) {
            futureDaysList.add(getFutureDate(i));
        }
        return futureDaysList;
    }

    private static String getFutureDate(int future) {
        Calendar calendar = Calendar.getInstance();
        calendar.set(Calendar.DAY_OF_YEAR, calendar.get(Calendar.DAY_OF_YEAR) + future);
        long today = calendar.getTimeInMillis();
        return rTimestampToDate(today);
    }

    /**
     * 时间转时间戳
     */
    public static long DateToTimestamp(String date) throws ParseException {
        return allDateFormat.parse(date).getTime();
    }
}