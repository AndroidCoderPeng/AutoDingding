package com.pengxh.autodingding.service;

import android.app.Notification;
import android.os.Bundle;
import android.service.notification.NotificationListenerService;
import android.service.notification.StatusBarNotification;
import android.util.Log;

import com.pengxh.app.multilib.utils.BroadcastManager;
import com.pengxh.autodingding.utils.Constant;

/**
 * @description: TODO 状态栏监听服务
 * @author: Pengxh
 * @email: 290677893@qq.com
 * @date: 2019/12/25 23:17
 */
public class NotificationMonitorService extends NotificationListenerService {

    private static final String TAG = "NotificationService";

    @Override
    public void onCreate() {
        super.onCreate();
        Log.d(TAG, "onCreate: 状态栏监听服务已启动");
    }

    /**
     * 有可用的并且和通知管理器连接成功时回调
     */
    @Override
    public void onListenerConnected() {
        Log.d(TAG, "onListenerConnected: ");
    }

    /**
     * 当有新通知到来时会回调
     */
    @Override
    public void onNotificationPosted(StatusBarNotification sbn) {
        Bundle extras = sbn.getNotification().extras;
        // 获取接收消息APP的包名
        String packageName = sbn.getPackageName();
        // 获取接收消息的抬头
        String notificationTitle = extras.getString(Notification.EXTRA_TITLE);
        // 获取接收消息的内容
        String notificationText = extras.getString(Notification.EXTRA_TEXT);
        Log.d(TAG, packageName + "-" + notificationTitle + "-" + notificationText);
        if (packageName.equals("com.alibaba.android.rimet")) {
            String dingding = notificationTitle + "-" + notificationText;
            BroadcastManager.getInstance(this).sendBroadcast(Constant.DINGDING_ACTION, dingding);
        } else {
            Log.d(TAG, "onNotificationPosted: 不是打卡通知，不处理");
        }
    }

    /**
     * 当有通知移除时会回调
     */
    @Override
    public void onNotificationRemoved(StatusBarNotification sbn) {
        Log.d(TAG, "onNotificationRemoved: " + sbn.getPackageName());
    }
}