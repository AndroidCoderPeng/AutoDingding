package com.pengxh.autodingding.service;

import android.app.Notification;
import android.content.Intent;
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
    public int onStartCommand(Intent intent, int flags, int startId) {
        Log.d(TAG, "onStartCommand: 状态栏监听服务已启动");
        return super.onStartCommand(intent, flags, startId);
    }

    /**
     * 有可用的并且和通知管理器连接成功时回调
     */
    @Override
    public void onListenerConnected() {

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
        if (packageName.equals("com.alibaba.android.rimet")) {
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