package com.pengxh.autodingding.service;

import android.app.Notification;
import android.content.ComponentName;
import android.content.Intent;
import android.os.Build;
import android.os.Bundle;
import android.service.notification.NotificationListenerService;
import android.service.notification.StatusBarNotification;
import android.text.TextUtils;
import android.util.Log;

import com.pengxh.androidx.lite.utils.SaveKeyValues;
import com.pengxh.androidx.lite.utils.TimeOrDateUtil;
import com.pengxh.autodingding.BaseApplication;
import com.pengxh.autodingding.bean.HistoryRecordBean;
import com.pengxh.autodingding.bean.MailInfo;
import com.pengxh.autodingding.bean.NotificationBean;
import com.pengxh.autodingding.greendao.HistoryRecordBeanDao;
import com.pengxh.autodingding.greendao.NotificationBeanDao;
import com.pengxh.autodingding.ui.WelcomeActivity;
import com.pengxh.autodingding.utils.Constant;
import com.pengxh.autodingding.utils.MailInfoUtil;
import com.pengxh.autodingding.utils.MailSender;

import java.util.UUID;

/**
 * @description: 状态栏监听服务
 * @author: Pengxh
 * @email: 290677893@qq.com
 * @date: 2019/12/25 23:17
 */
public class NotificationMonitorService extends NotificationListenerService {

    private static final String TAG = "MonitorService";
    private HistoryRecordBeanDao recordBeanDao;
    private NotificationBeanDao notificationBeanDao;

    /**
     * 有可用的并且和通知管理器连接成功时回调
     */
    @Override
    public void onListenerConnected() {
        Log.d(TAG, "onListenerConnected");
        recordBeanDao = BaseApplication.getDaoSession().getHistoryRecordBeanDao();
        notificationBeanDao = BaseApplication.getDaoSession().getNotificationBeanDao();
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

        //保存所有通知信息
        NotificationBean notificationBean = new NotificationBean();
        notificationBean.setUuid(UUID.randomUUID().toString());
        notificationBean.setPackageName(packageName);
        notificationBean.setNotificationTitle(extras.getString(Notification.EXTRA_TITLE));
        notificationBean.setNotificationMsg(notificationText);
        notificationBean.setPostTime(TimeOrDateUtil.timestampToCompleteDate(System.currentTimeMillis()));
        notificationBeanDao.save(notificationBean);

//        if (packageName.equals("com.tencent.mobileqq")) {
        if (packageName.equals("com.alibaba.android.rimet")) {
            if (notificationText == null || notificationText.equals("")) {
                return;
            }
            Log.d(TAG, "onNotificationPosted ===> " + notificationText);
            if (notificationText.contains("考勤打卡")) {
                //保存打卡记录
                HistoryRecordBean bean = new HistoryRecordBean();
                bean.setUuid(UUID.randomUUID().toString());
                bean.setDate(TimeOrDateUtil.timestampToCompleteDate(System.currentTimeMillis()));
                bean.setMessage(notificationText);
                recordBeanDao.save(bean);

                String emailAddress = (String) SaveKeyValues.getValue(Constant.EMAIL_ADDRESS, "");
                if (TextUtils.isEmpty(emailAddress)) {
                    Log.d(TAG, "邮箱地址为空");
                } else {
                    //发送打卡成功的邮件
                    new Thread(() -> {
                        MailInfo mailInfo = MailInfoUtil.createMail(emailAddress, notificationText);
                        MailSender.getSender().sendTextMail(mailInfo);
                    }).start();

                    Intent intent = new Intent(this, WelcomeActivity.class);
                    intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                    startActivity(intent);
                }
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
        Log.d(TAG, "onListenerDisconnected");
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
            // 通知侦听器断开连接 - 请求重新绑定
            requestRebind(new ComponentName(this, NotificationListenerService.class));
        }
    }
}