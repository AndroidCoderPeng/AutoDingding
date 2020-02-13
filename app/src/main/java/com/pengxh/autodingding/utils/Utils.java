package com.pengxh.autodingding.utils;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.app.ActivityManager;
import android.app.KeyguardManager;
import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.ProgressDialog;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.content.pm.ResolveInfo;
import android.os.Build;
import android.os.Environment;
import android.os.PowerManager;
import android.provider.Settings;
import android.util.Log;

import com.pengxh.autodingding.R;
import com.squareup.okhttp.Call;
import com.squareup.okhttp.Callback;
import com.squareup.okhttp.OkHttpClient;
import com.squareup.okhttp.Request;
import com.squareup.okhttp.Response;

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

import io.reactivex.Observable;
import io.reactivex.ObservableOnSubscribe;
import io.reactivex.Observer;
import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.disposables.CompositeDisposable;
import io.reactivex.disposables.Disposable;
import io.reactivex.schedulers.Schedulers;

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
        if (packageInfos != null) {
            for (int i = 0; i < packageInfos.size(); i++) {
                String packName = packageInfos.get(i).packageName;
                packageNames.add(packName);
            }
        }
        return packageNames.contains(packageName);
    }

    /**
     * 查询某个服务是否在运行中
     */
    public static boolean isServiceAlive(String serviceName) {
        ActivityManager manager = (ActivityManager) mContext.getSystemService(mContext.ACTIVITY_SERVICE);
        List<ActivityManager.RunningServiceInfo> services = manager.getRunningServices(Integer.MAX_VALUE);
        for (ActivityManager.RunningServiceInfo service : services) {
            String className = service.service.getClassName();
            Log.d(TAG, "isServiceAlive: " + className);
            if (serviceName.equals(className)) {
                //打开常住通知栏
                createNotification();
                return true;
            }
        }
        return false;
    }

    private static void createNotification() {
        Log.d(TAG, "createNotification");
        //Android8.0以上必须添加 渠道 才能显示通知栏
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            //创建渠道
            String id = "notification_999";
            String name = "钉钉打卡通知专用监听";
            NotificationChannel mChannel = new NotificationChannel(id, name, NotificationManager.IMPORTANCE_LOW);
            notificationManager.createNotificationChannel(mChannel);

            Notification.Builder builder = new Notification.Builder(mContext, id);
            builder.setContentTitle("通知栏监听")
                    .setContentText("钉钉打卡通知专用监听已打开")
                    .setSmallIcon(R.mipmap.logo_round)
                    .setAutoCancel(false);
            Notification notification = builder.build();
            notification.flags = Notification.FLAG_ONGOING_EVENT;
            notificationManager.notify(1, notification);
        } else {
            //设置图片,通知标题,发送时间,提示方式等属性
            Notification.Builder builder = new Notification.Builder(mContext);
            builder.setContentTitle("通知栏监听")
                    .setContentText("钉钉打卡通知专用监听已打开")
                    .setSmallIcon(R.mipmap.logo_round)
                    .setAutoCancel(false);
            Notification notification = builder.build();
            notification.flags = Notification.FLAG_ONGOING_EVENT;
            notificationManager.notify(1, notification);
        }
    }

    /**
     * 打开指定包名的apk
     *
     * @param packageName 应用包名
     */

    public static void openDingDing(String packageName) {
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
     * 打开通知设置
     */
    public static void openNotificationSettings() {
        try {
            Intent intent;
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP_MR1) {
                intent = new Intent(Settings.ACTION_NOTIFICATION_LISTENER_SETTINGS);
            } else {
                intent = new Intent("android.settings.ACTION_NOTIFICATION_LISTENER_SETTINGS");
            }
            intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
            mContext.startActivity(intent);
        } catch (Exception e) {
            e.printStackTrace();
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

    private static CompositeDisposable compositeDisposable = new CompositeDisposable();

    public static void doHttpRequest(String url, HttpCallbackListener listener) {
        Observable.create((ObservableOnSubscribe<Response>) emitter -> {
            Call call = new OkHttpClient().newCall(new Request.Builder().url(url).get().build());
            call.enqueue(new Callback() {
                @Override
                public void onFailure(Request request, IOException e) {
                    Log.e(TAG, "onFailure: ", e);
                }

                @Override
                public void onResponse(Response response) throws IOException {
                    emitter.onNext(response);
                }
            });
        }).subscribeOn(Schedulers.io()).observeOn(AndroidSchedulers.mainThread()).subscribe(new Observer<Response>() {
            Disposable mDisposable;

            @Override
            public void onSubscribe(Disposable d) {
                Log.d(TAG, "onSubscribe: 添加订阅" + d);
                //订阅
                mDisposable = d;
                //添加到容器中
                compositeDisposable.add(d);
            }

            @Override
            public void onNext(Response response) {
                if (mDisposable.isDisposed()) {
                    return;
                }
                try {
                    listener.onSuccess(response.body().string());
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }

            @Override
            public void onError(Throwable e) {
                listener.onError(new Exception(e));
            }

            @Override
            public void onComplete() {
                //取消订阅
                Log.d(TAG, "onComplete: 取消订阅");
                mDisposable.dispose();
                compositeDisposable.clear();
            }
        });
    }

    private static ProgressDialog progressDialog;

    public static void showProgress(Activity activity, String msg) {
        if (progressDialog == null) {
            progressDialog = new ProgressDialog(activity);
        }
        progressDialog.setMessage(msg);
        progressDialog.setCanceledOnTouchOutside(false);
        progressDialog.show();
    }

    public static void hideProgress() {
        if (progressDialog != null && progressDialog.isShowing()) {
            progressDialog.dismiss();
        }
    }
}