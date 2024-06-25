package com.pengxh.autodingding.service

import android.app.Notification
import android.content.ComponentName
import android.content.Intent
import android.os.Build
import android.service.notification.NotificationListenerService
import android.service.notification.StatusBarNotification
import android.util.Log
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleOwner
import androidx.lifecycle.LifecycleRegistry
import androidx.lifecycle.lifecycleScope
import com.pengxh.autodingding.BaseApplication
import com.pengxh.autodingding.bean.NotificationBean
import com.pengxh.autodingding.extensions.createMail
import com.pengxh.autodingding.extensions.openApplication
import com.pengxh.autodingding.extensions.sendTextMail
import com.pengxh.autodingding.fragment.SettingsFragment
import com.pengxh.autodingding.ui.MainActivity
import com.pengxh.autodingding.utils.Constant
import com.pengxh.autodingding.utils.CountDownTimerManager
import com.pengxh.kt.lite.extensions.show
import com.pengxh.kt.lite.extensions.timestampToCompleteDate
import com.pengxh.kt.lite.utils.SaveKeyValues
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
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
        val notificationText = extras.getString(Notification.EXTRA_TEXT)
        Log.d(kTag, "onNotificationPosted: $notificationText")

        if (notificationText.isNullOrBlank()) {
            return
        }

        val notificationBean = NotificationBean()
        notificationBean.uuid = UUID.randomUUID().toString()
        notificationBean.packageName = packageName
        notificationBean.notificationTitle = title
        notificationBean.notificationMsg = notificationText
        notificationBean.postTime = System.currentTimeMillis().timestampToCompleteDate()
        notificationBeanDao.save(notificationBean)

        if (packageName == Constant.DING_DING) {
            if (notificationText.contains("成功")) {
                backToMainActivity()

                val emailAddress = SaveKeyValues.getValue(Constant.EMAIL_ADDRESS, "") as String
                if (emailAddress.isEmpty()) {
                    "邮箱地址为空".show(this)
                    return
                }
                //发送打卡成功的邮件
                lifecycleScope.launch(Dispatchers.Main) {
                    "即将发送打卡邮件，请注意查收".show(this@NotificationMonitorService)
                    delay(3000)
                    withContext(Dispatchers.IO) {
                        notificationText.createMail(emailAddress).sendTextMail()
                    }
                }
            }
        } else if (packageName == Constant.WECHAT || packageName == Constant.QQ) {
            openApplication(Constant.DING_DING)
        }
    }

    private fun backToMainActivity() {
        CountDownTimerManager.get.cancelTimer()
        val intent = Intent(this, MainActivity::class.java)
        intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK
        startActivity(intent)
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