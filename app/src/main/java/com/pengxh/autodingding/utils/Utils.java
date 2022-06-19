package com.pengxh.autodingding.utils;

import android.annotation.SuppressLint;
import android.app.KeyguardManager;
import android.app.NotificationManager;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.content.pm.ResolveInfo;
import android.os.CountDownTimer;
import android.os.PowerManager;
import android.util.Log;
import android.view.WindowManager;

import java.util.ArrayList;
import java.util.List;

/**
 * @author: Pengxh
 * @email: 290677893@qq.com
 * @date: 2019/12/25 13:13
 */
public class Utils {
    private static final String TAG = "Utils";
    @SuppressLint("StaticFieldLeak")
    private static Context mContext;
    private static NotificationManager notificationManager;

    public static void init(Context context) {
        Utils.mContext = context.getApplicationContext();//获取全局上下文，最长生命周期
        notificationManager = (NotificationManager) mContext.getSystemService(Context.NOTIFICATION_SERVICE);
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
        for (int i = 0; i < packageInfos.size(); i++) {
            String packName = packageInfos.get(i).packageName;
            packageNames.add(packName);
        }
        return packageNames.contains(packageName);
    }

    /**
     * 打开指定包名的apk
     *
     * @param packageName 应用包名
     */

    public static void openDingDing(String packageName) {
        wakeUpAndUnlock();
        Log.d(TAG, "openDingDing: 已亮屏，1s后启动钉钉");
        new CountDownTimer(1000, 1000) {
            @Override
            public void onTick(long millisUntilFinished) {

            }

            @Override
            public void onFinish() {
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
        }.start();
    }

    /**
     * 唤醒屏幕并解锁
     */
    private static void wakeUpAndUnlock() {
        Log.d(TAG, "wakeUpAndUnlock: 亮屏解锁");
        PowerManager powerManager = (PowerManager) mContext.getSystemService(Context.POWER_SERVICE);
        boolean screenOn = powerManager.isInteractive();
        if (!screenOn) {
            //唤醒屏幕
            PowerManager.WakeLock wakeLock = powerManager.newWakeLock(
                    PowerManager.ACQUIRE_CAUSES_WAKEUP | WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON,
                    "dTag:screenOn"
            );
            wakeLock.acquire(10000);
            wakeLock.release();
        }
        //解锁屏幕
        KeyguardManager keyguardManager = (KeyguardManager) mContext.getSystemService(Context.KEYGUARD_SERVICE);
        KeyguardManager.KeyguardLock keyguardLock = keyguardManager.newKeyguardLock("unLock");
        keyguardLock.disableKeyguard();
    }
}