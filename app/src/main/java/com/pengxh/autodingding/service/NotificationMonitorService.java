package com.pengxh.autodingding.service;

import android.app.Notification;
import android.content.Intent;
import android.os.Bundle;
import android.service.notification.NotificationListenerService;
import android.service.notification.StatusBarNotification;
import android.util.Log;

import com.pengxh.app.multilib.utils.BroadcastManager;
import com.pengxh.autodingding.utils.Constant;
import com.pengxh.autodingding.utils.LogToFile;

/**
 * @description: TODO 状态栏监听服务
 * @author: Pengxh
 * @email: 290677893@qq.com
 * @date: 2019/12/25 23:17
 */
public class NotificationMonitorService extends NotificationListenerService {

    private static final String TAG = "NotificationService";

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        Log.d(TAG, "onStartCommand: 状态栏监听服务已启动");
        return super.onStartCommand(intent, flags, startId);
    }

    /**
     * 有可用的并且和通知管理器连接成功时回调
     */
    @Override
    public void onListenerConnected() {
        Log.d(TAG, "onListenerConnected: 通知管理器连接成功");
    }

    /**
     * 当有新通知到来时会回调
     */
    @Override
    public void onNotificationPosted(StatusBarNotification sbn) {
        Bundle extras = sbn.getNotification().extras;
        // 获取接收消息APP的包名
        String packageName = sbn.getPackageName();
        // 获取接收消息的内容
        String notificationText = extras.getString(Notification.EXTRA_TEXT);
        Log.d(TAG, "推送通知包名: [" + packageName + "], 通知内容: " + notificationText);
        LogToFile.d(TAG, "推送通知包名: [" + packageName + "], 通知内容: " + notificationText);
        if (packageName.contains("rimet")) {
            BroadcastManager.getInstance(this).sendBroadcast(Constant.DINGDING_ACTION, notificationText);
        } else {
            Log.d(TAG, "onNotificationPosted: 不是打卡通知，不处理");
        }
    }

    /**
     * 当有通知移除时会回调
     */
    @Override
    public void onNotificationRemoved(StatusBarNotification sbn) {

    }
}