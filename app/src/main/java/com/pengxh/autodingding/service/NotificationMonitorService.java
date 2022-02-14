package com.pengxh.autodingding.service;

import android.app.Notification;
import android.content.ComponentName;
import android.os.Build;
import android.os.Bundle;
import android.service.notification.NotificationListenerService;
import android.service.notification.StatusBarNotification;

import com.pengxh.autodingding.BaseApplication;
import com.pengxh.autodingding.bean.HistoryRecordBean;
import com.pengxh.autodingding.greendao.HistoryRecordBeanDao;
import com.pengxh.autodingding.ui.fragment.AutoDingDingFragment;
import com.pengxh.autodingding.ui.fragment.SettingsFragment;
import com.pengxh.autodingding.utils.TimeOrDateUtil;

import java.util.UUID;

/**
 * @description: TODO 状态栏监听服务
 * @author: Pengxh
 * @email: 290677893@qq.com
 * @date: 2019/12/25 23:17
 */
public class NotificationMonitorService extends NotificationListenerService {

    private static final String TAG = "MonitorService";
    private HistoryRecordBeanDao recordBeanDao;

    /**
     * 有可用的并且和通知管理器连接成功时回调
     */
    @Override
    public void onListenerConnected() {
        recordBeanDao = BaseApplication.getDaoSession().getHistoryRecordBeanDao();
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
            if (notificationText == null || notificationText.equals("")) {
                return;
            }
            if (notificationText.contains("考勤打卡")) {
                //保存打卡记录
                HistoryRecordBean bean = new HistoryRecordBean();
                bean.setUuid(UUID.randomUUID().toString());
                bean.setDate(TimeOrDateUtil.timestampToDate(System.currentTimeMillis()));
                bean.setMessage(notificationText);
                recordBeanDao.save(bean);
                //通知发送邮件和更新界面
                AutoDingDingFragment.sendMessage(notificationText);
                SettingsFragment.sendEmptyMessage();
            }
        }
    }

    /**
     * 当有通知移除时会回调
     */
    @Override
    public void onNotificationRemoved(StatusBarNotification sbn) {

    }

    @Override
    public void onListenerDisconnected() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
            // 通知侦听器断开连接 - 请求重新绑定
            requestRebind(new ComponentName(this, NotificationListenerService.class));
        }
    }
}