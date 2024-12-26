package com.pengxh.daily.app.service

import android.app.Notification
import android.os.BatteryManager
import android.service.notification.NotificationListenerService
import android.service.notification.StatusBarNotification
import android.util.Log
import com.pengxh.daily.app.BaseApplication
import com.pengxh.daily.app.bean.NotificationBean
import com.pengxh.daily.app.extensions.backToMainActivity
import com.pengxh.daily.app.extensions.openApplication
import com.pengxh.daily.app.extensions.sendEmail
import com.pengxh.daily.app.fragment.SettingsFragment
import com.pengxh.daily.app.utils.Constant
import com.pengxh.daily.app.utils.TimeKit
import com.pengxh.kt.lite.extensions.getSystemService
import com.pengxh.kt.lite.extensions.show
import com.pengxh.kt.lite.extensions.timestampToCompleteDate
import com.pengxh.kt.lite.utils.SaveKeyValues
import java.util.UUID

/**
 * @description: 状态栏监听服务
 * @author: Pengxh
 * @email: 290677893@qq.com
 * @date: 2019/12/25 23:17
 */
class NotificationMonitorService : NotificationListenerService() {

    private val kTag = "MonitorService"

    private val notificationBeanDao by lazy { BaseApplication.get().daoSession.notificationBeanDao }
    private val batteryManager by lazy { getSystemService<BatteryManager>() }

    /**
     * 有可用的并且和通知管理器连接成功时回调
     */
    override fun onListenerConnected() {
        Log.d(kTag, "onListenerConnected: 通知监听服务运行中")
        SettingsFragment.weakReferenceHandler?.sendEmptyMessage(Constant.NOTICE_LISTENER_CONNECTED_CODE)
    }

    /**
     * 当有新通知到来时会回调
     */
    override fun onNotificationPosted(sbn: StatusBarNotification) {
        val extras = sbn.notification.extras
        // 获取接收消息APP的包名
        val packageName = sbn.packageName
        // 获取接收消息的标题
        val title = extras.getString(Notification.EXTRA_TITLE) ?: ""
        // 获取接收消息的内容
        val notice = extras.getString(Notification.EXTRA_TEXT)
        if (notice.isNullOrBlank()) {
            return
        }
        SettingsFragment.weakReferenceHandler?.sendEmptyMessage(Constant.NOTICE_LISTENER_CONNECTED_CODE)

        if (notice != Constant.FOREGROUND_RUNNING_SERVICE_TITLE) {
            val notificationBean = NotificationBean()
            notificationBean.uuid = UUID.randomUUID().toString()
            notificationBean.packageName = packageName
            notificationBean.notificationTitle = title
            notificationBean.notificationMsg = notice
            notificationBean.postTime = System.currentTimeMillis().timestampToCompleteDate()
            notificationBeanDao.save(notificationBean)
        }

        if (packageName == Constant.DING_DING) {
            if (notice.contains("成功")) {
                backToMainActivity()
                "即将发送通知邮件，请注意查收".show(this)
                notice.sendEmail(this, null, false)
            }
        } else if (packageName == Constant.WECHAT || packageName == Constant.QQ || packageName == Constant.TIM || packageName == Constant.ZFB) {
            if (notice.contains("电量")) {
                val capacity = batteryManager?.getIntProperty(
                    BatteryManager.BATTERY_PROPERTY_CAPACITY
                )
                "当前手机剩余电量为：${capacity}%".sendEmail(this, "查询手机电量通知", false)
            } else {
                val key = SaveKeyValues.getValue(Constant.DING_DING_KEY, "打卡") as String
                if (notice.contains(key)) {
                    val isSkipHoliday = SaveKeyValues.getValue(
                        Constant.SKIP_HOLIDAY_KEY, true
                    ) as Boolean
                    if (isSkipHoliday) {
                        if (TimeKit.todayIsHoliday(this)) {
                            //休息
                            "今天休息哦~，已经帮你跳过打卡任务".sendEmail(this, "放假通知", false)
                        } else {
                            openApplication(Constant.DING_DING, true)
                        }
                    } else {
                        openApplication(Constant.DING_DING, true)
                    }
                }
            }
        }
    }

    /**
     * 当有通知移除时会回调
     */
    override fun onNotificationRemoved(sbn: StatusBarNotification) {}

    override fun onListenerDisconnected() {
        Log.d(kTag, "onListenerDisconnected: 通知监听服务已关闭")
        SettingsFragment.weakReferenceHandler?.sendEmptyMessage(Constant.NOTICE_LISTENER_DISCONNECTED_CODE)
    }
}