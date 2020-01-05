package com.pengxh.autodingding.service;

import android.app.Notification;
import android.content.Intent;
import android.os.Bundle;
import android.service.notification.NotificationListenerService;
import android.service.notification.StatusBarNotification;
import android.util.Log;

import com.pengxh.autodingding.utils.LiveDataBus;

/**
 * @description: TODO
 * @author: Pengxh
 * @email: 290677893@qq.com
 * @date: 2020/1/5 21:13
 */
public class NotificationService extends NotificationListenerService {

    public static String TAG = "NotificationService";
    public static String NOTIFICATION_PACKAGE_MOBILEQQ = "com.tencent.mobileqq";
    public static String NOTIFICATION_PACKAGE_WECHAT = "com.tencent.mm";

    @Override
    public void onCreate() {
        super.onCreate();
        Log.d(TAG, "onCreate: 通知监听服务已启动");
    }


    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        Log.d(TAG, "----------------onStartCommand--------->");
        return super.onStartCommand(intent, flags, startId);
    }

    @Override
    public void onListenerConnected() {
        //当连接成功时调用，一般在开启监听后会回调一次该方法
        Log.d(TAG, "----------------onListenerConnected--------->");
    }

    @Override
    public void onNotificationPosted(StatusBarNotification sbn) {
        super.onNotificationPosted(sbn);
        //当收到一条消息时回调，sbn里面带有这条消息的具体信息
        Log.d(TAG, "----------------onNotificationPosted--------->" + sbn.getPackageName());
        String packageName = sbn.getPackageName();//获取发送通知的应用程序包名

        Bundle extras = sbn.getNotification().extras;
        String title = extras.getString(Notification.EXTRA_TITLE); //通知title
        String content = extras.getString(Notification.EXTRA_TEXT); //通知内容

        //只处理QQ和微信的通知
        if (NOTIFICATION_PACKAGE_MOBILEQQ.equals(packageName) || NOTIFICATION_PACKAGE_WECHAT.equals(packageName)) {
            String msg = title + "," + content;
            Log.d(TAG, "onNotificationPosted: " + msg);
            LiveDataBus.get().with("notifyMessage").setValue(msg);
        } else {
            Log.i(TAG, "onNotificationPosted: " + packageName);
        }
    }

    @Override
    public void onNotificationRemoved(StatusBarNotification sbn) {
        //当移除一条消息的时候回调，sbn是被移除的消息
        Log.d(TAG, "----------------onNotificationRemoved--------->");
    }
}
