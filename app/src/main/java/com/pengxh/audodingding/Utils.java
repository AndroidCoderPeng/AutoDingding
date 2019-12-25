package com.pengxh.audodingding;

import android.annotation.SuppressLint;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.content.pm.ResolveInfo;
import android.util.Log;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

/**
 * @author: Pengxh
 * @email: 290677893@qq.com
 * @description: TODO
 * @date: 2019/12/25 13:13
 */
public class Utils {
    private static final String TAG = "Utils";

    /**
     * 检查手机上是否安装了指定的软件
     *
     * @param context     上下文
     * @param packageName 应用包名
     */
    public static boolean isAppAvilible(Context context, String packageName) {
        PackageManager packageManager = context.getPackageManager();
        //获取所有已安装程序的包信息
        List<PackageInfo> packageInfos = packageManager.getInstalledPackages(0);
        List<String> packageNames = new ArrayList<>();
        if (packageInfos != null) {
            for (int i = 0; i < packageInfos.size(); i++) {
                String packName = packageInfos.get(i).packageName;
                packageNames.add(packName);
            }
        }
        return packageNames.contains(packageName);
    }

    /**
     * 打开指定包名的apk
     *
     * @param context     上下文
     * @param packageName 应用包名
     */

    public static void openDingding(Context context, String packageName) {
        PackageManager packageManager = context.getPackageManager();
        Intent resolveIntent = new Intent(Intent.ACTION_MAIN, null);
        resolveIntent.addCategory(Intent.CATEGORY_LAUNCHER);
        resolveIntent.setPackage(packageName);
        List<ResolveInfo> apps = packageManager.queryIntentActivities(resolveIntent, 0);
        ResolveInfo resolveInfo = apps.iterator().next();
        if (resolveInfo != null) {
            String className = resolveInfo.activityInfo.name;
            Intent intent = new Intent(Intent.ACTION_MAIN);
            intent.addCategory(Intent.CATEGORY_LAUNCHER);
            intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
            ComponentName cn = new ComponentName(packageName, className);
            intent.setComponent(cn);
            context.startActivity(intent);
        }
    }

    /**
     * 时间戳转时间
     */
    @SuppressLint("SimpleDateFormat")
    public static String timestampToDate(long millSeconds) {
        Log.d(TAG, "timestampToDate: " + millSeconds);
        SimpleDateFormat sf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
        Date d = new Date(millSeconds);
        return sf.format(d);
    }

    /**
     * 计算时间差
     *
     * @param current 开始时间
     * @param end     结束时间
     */
    public static long deltaTime(long current, String end) {
        Log.d(TAG, "current: " + current);
        SimpleDateFormat format = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
        long l = 0;
        try {
            long endTime = format.parse(end).getTime();
            Log.d(TAG, "endTime: " + endTime);
            l = endTime - current;
        } catch (ParseException e) {
            e.printStackTrace();
        }
        Log.d(TAG, "deltaTime: " + l);
        return l;
    }
}
