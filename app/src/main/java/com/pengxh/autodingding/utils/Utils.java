package com.pengxh.autodingding.utils;

import android.annotation.SuppressLint;
import android.app.KeyguardManager;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.content.pm.ResolveInfo;
import android.os.Environment;
import android.os.PowerManager;
import android.util.Log;

import com.pengxh.app.multilib.widget.EasyToast;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Random;

import static android.content.Context.KEYGUARD_SERVICE;

/**
 * @author: Pengxh
 * @email: 290677893@qq.com
 * @description: TODO
 * @date: 2019/12/25 13:13
 */
public class Utils {
    private static final String TAG = "Utils";
    @SuppressLint("SimpleDateFormat")
    private static final SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
    private static String sdCardDir = Environment.getExternalStorageDirectory() + "/DingDingLogs/";
    @SuppressLint("StaticFieldLeak")
    private static Context mContext;

    public static void init(Context context) {
        Utils.mContext = context.getApplicationContext();//获取全局上下文，最长生命周期
    }

    /**
     * 检查手机上是否安装了指定的软件
     *
     * @param packageName 应用包名
     */
    public static boolean isAppAvailable(String packageName) {
        PackageManager packageManager = mContext.getPackageManager();
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
     * 打开指定包名的apk
     *
     * @param packageName 应用包名
     */

    public static void openDingding(String packageName) {
        wakeUpAndUnlock();
        PackageManager packageManager = mContext.getPackageManager();
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
            mContext.startActivity(intent);
        }
    }

    /**
     * 唤醒屏幕并解锁
     */
    @SuppressLint("InvalidWakeLockTag")
    private static void wakeUpAndUnlock() {
        Log.d(TAG, "wakeUpAndUnlock: 亮屏解锁");
        PowerManager powerManager = (PowerManager) mContext.getSystemService(Context.POWER_SERVICE);
        boolean screenOn = powerManager.isScreenOn();
        if (!screenOn) {
            //唤醒屏幕
            PowerManager.WakeLock wakeLock = powerManager.newWakeLock(PowerManager.ACQUIRE_CAUSES_WAKEUP | PowerManager.SCREEN_BRIGHT_WAKE_LOCK, "bright");
            wakeLock.acquire(10000);
            wakeLock.release();
        }
        //解锁屏幕
        KeyguardManager keyguardManager = (KeyguardManager) mContext.getSystemService(KEYGUARD_SERVICE);
        KeyguardManager.KeyguardLock keyguardLock = keyguardManager.newKeyguardLock("unLock");
        keyguardLock.disableKeyguard();
    }

    /**
     * 时间转时间戳
     */
    public static long DateToTimestamp(String date) throws ParseException {
        return dateFormat.parse(date).getTime();
    }

    public static String uuid() {
        String str = "abcdefghijklmnopqrstuvwxyzABCDEFGHIJKLMNOPQRSTUVWXYZ0123456789";
        Random random = new Random();
        StringBuffer sb = new StringBuffer();
        for (int i = 0; i < 11; i++) {
            int number = random.nextInt(62);
            sb.append(str.charAt(number));
        }
        return sb.toString();
    }
}