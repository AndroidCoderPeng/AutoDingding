package com.pengxh.autodingding.utils;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.app.KeyguardManager;
import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.content.ComponentName;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.content.pm.ResolveInfo;
import android.os.Build;
import android.os.CountDownTimer;
import android.os.Environment;
import android.os.PowerManager;
import android.util.Log;

import com.pengxh.autodingding.R;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;

import androidx.appcompat.app.AlertDialog;

import static android.content.Context.KEYGUARD_SERVICE;

/**
 * @author: Pengxh
 * @email: 290677893@qq.com
 * @description: TODO
 * @date: 2019/12/25 13:13
 */
public class Utils {
    private static final String TAG = "Utils";
    private static String filePath = Environment.getExternalStorageDirectory().getAbsolutePath() + "/AutoDingding/";
    private static String fileName = "emailAddress.txt";
    @SuppressLint("StaticFieldLeak")
    private static Context mContext;
    private static NotificationManager notificationManager;

    public static void init(Context context) {
        Utils.mContext = context.getApplicationContext();//获取全局上下文，最长生命周期
        File file = new File(filePath + fileName);
        if (!file.exists()) {
            try {
                file.createNewFile();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
        notificationManager = (NotificationManager) mContext.getSystemService(Context.NOTIFICATION_SERVICE);
        Log.d(TAG, "init: " + file);
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

    public static void createNotification() {
        //Android8.0以上必须添加 渠道 才能显示通知栏
        Notification.Builder builder;
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            //创建渠道
            String name = mContext.getResources().getString(R.string.app_name);
            String id = name + "_DefaultChannel";
            NotificationChannel mChannel = new NotificationChannel(id, name, NotificationManager.IMPORTANCE_DEFAULT);
            notificationManager.createNotificationChannel(mChannel);
            builder = new Notification.Builder(mContext, id);
        } else {
            builder = new Notification.Builder(mContext);
        }
        builder.setContentTitle("钉钉打卡通知监听已打开")
                .setContentText("如果通知消失，请重新开启应用")
                .setSmallIcon(R.mipmap.logo_round)
                .setAutoCancel(false);
        Notification notification = builder.build();
        notification.flags = Notification.FLAG_NO_CLEAR;
        notificationManager.notify(111, notification);
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

    public static String uuid() {
        String str = "abcdefghijklmnopqrstuvwxyzABCDEFGHIJKLMNOPQRSTUVWXYZ0123456789";
        Random random = new Random();
        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < 11; i++) {
            int number = random.nextInt(62);
            sb.append(str.charAt(number));
        }
        return sb.toString();
    }

    /**
     * 将数据写入文件
     */
    public static void saveEmailAddress(String email) {
        //准备写入
        FileOutputStream outputStream;
        BufferedWriter bufferedWriter = null;
        try {
            outputStream = mContext.openFileOutput(fileName, Context.MODE_PRIVATE);
            bufferedWriter = new BufferedWriter(new OutputStreamWriter(outputStream));
            bufferedWriter.write(email);
        } catch (IOException e) {
            e.printStackTrace();
        } finally {
            if (bufferedWriter != null) {
                try {
                    bufferedWriter.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }
    }

    /**
     * 读取文件存储内容
     */
    public static String readEmailAddress() {
        FileInputStream inputStream;
        BufferedReader bufferedReader = null;
        StringBuilder content = new StringBuilder();
        try {
            inputStream = mContext.openFileInput(fileName);
            bufferedReader = new BufferedReader(new InputStreamReader(inputStream));
            String line;
            while ((line = bufferedReader.readLine()) != null) {
                content.append(line);
            }
        } catch (IOException e) {
            e.printStackTrace();
        } finally {
            if (bufferedReader != null) {
                try {
                    bufferedReader.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }
        return content.toString();
    }

    public static void showAlertDialog(Activity activity, String title, String message, String positiveButton, boolean cancelable) {
        createBuilder(activity, title, message)
                .setCancelable(cancelable)
                .setPositiveButton(positiveButton, null)
                .create()
                .show();
    }

    public static void showAlertDialog(Activity activity, String title, String message, String positiveButton, boolean cancelable, DialogInterface.OnClickListener listener) {
        createBuilder(activity, title, message)
                .setCancelable(cancelable)
                .setPositiveButton(positiveButton, listener)
                .create()
                .show();
    }

    private static AlertDialog.Builder createBuilder(Activity activity, String title, String message) {
        return new AlertDialog.Builder(activity)
                .setIcon(R.mipmap.logo)
                .setTitle(title)
                .setMessage(message);
    }
}