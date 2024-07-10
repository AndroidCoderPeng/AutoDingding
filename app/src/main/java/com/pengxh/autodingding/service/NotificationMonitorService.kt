package com.pengxh.autodingding.service

import android.app.Notification
import android.content.ComponentName
import android.os.Build
import android.service.notification.NotificationListenerService
import android.service.notification.StatusBarNotification
import android.util.Log
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleOwner
import androidx.lifecycle.LifecycleRegistry
import com.pengxh.autodingding.BaseApplication
import com.pengxh.autodingding.bean.NotificationBean
import com.pengxh.autodingding.extensions.openApplication
import com.pengxh.autodingding.fragment.SettingsFragment
import com.pengxh.autodingding.utils.Constant
import com.pengxh.kt.lite.extensions.timestampToCompleteDate
import java.util.UUID

/**
 * @description: 状态栏监听服务
 * @author: Pengxh
 * @email: 290677893@qq.com
 * @date: 2019/12/25 23:17
 */
class NotificationMonitorService : NotificationListenerService(), LifecycleOwner {

    private val kTag = "MonitorService"
    private val registry = LifecycleRegistry(this)

    override fun getLifecycle(): Lifecycle {
        return registry
    }

    private val notificationBeanDao by lazy { BaseApplication.get().daoSession.notificationBeanDao }

    /**
     * 有可用的并且和通知管理器连接成功时回调
     */
    override fun onListenerConnected() {
        Log.d(kTag, "onListenerConnected: ")
        SettingsFragment.weakReferenceHandler?.sendEmptyMessage(2024060601)
    }

    /**
     * 当有新通知到来时会回调
     */
    override fun onNotificationPosted(sbn: StatusBarNotification) {
        //能收到通知，说明通知监听服务是处于活跃状态的
        SettingsFragment.weakReferenceHandler?.sendEmptyMessage(2024060601)

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
        Log.d(kTag, "onNotificationPosted: $notice")

        val notificationBean = NotificationBean()
        notificationBean.uuid = UUID.randomUUID().toString()
        notificationBean.packageName = packageName
        notificationBean.notificationTitle = title
        notificationBean.notificationMsg = notice
        notificationBean.postTime = System.currentTimeMillis().timestampToCompleteDate()
        notificationBeanDao.save(notificationBean)

        if (packageName == Constant.WECHAT || packageName == Constant.QQ) {
            openApplication(Constant.DING_DING)
        }
    }

    /**
     * 当有通知移除时会回调
     */
    override fun onNotificationRemoved(sbn: StatusBarNotification) {}

    override fun onListenerDisconnected() {
        Log.d(kTag, "onListenerDisconnected: ")
        SettingsFragment.weakReferenceHandler?.sendEmptyMessage(2024060602)
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
            // 通知侦听器断开连接 - 请求重新绑定
            requestRebind(ComponentName(this, NotificationListenerService::class.java))
        }
    }
}