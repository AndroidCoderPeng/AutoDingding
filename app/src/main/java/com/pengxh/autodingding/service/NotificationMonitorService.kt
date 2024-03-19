package com.pengxh.autodingding.service

import android.app.Notification
import android.content.ComponentName
import android.content.Intent
import android.os.Build
import android.service.notification.NotificationListenerService
import android.service.notification.StatusBarNotification
import android.util.Log
import com.pengxh.autodingding.BaseApplication
import com.pengxh.autodingding.bean.NotificationBean
import com.pengxh.autodingding.extensions.createMail
import com.pengxh.autodingding.extensions.openApplication
import com.pengxh.autodingding.extensions.sendTextMail
import com.pengxh.autodingding.ui.MainActivity
import com.pengxh.autodingding.utils.Constant
import com.pengxh.kt.lite.extensions.timestampToCompleteDate
import com.pengxh.kt.lite.extensions.toJson
import com.pengxh.kt.lite.utils.SaveKeyValues
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
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

    /**
     * 有可用的并且和通知管理器连接成功时回调
     */
    override fun onListenerConnected() {
        Log.d(kTag, "onListenerConnected")
    }

    /**
     * 当有新通知到来时会回调
     */
    override fun onNotificationPosted(sbn: StatusBarNotification) {
        val extras = sbn.notification.extras
        // 获取接收消息APP的包名
        val packageName = sbn.packageName
        // 获取接收消息的内容
        val notificationText = extras.getString(Notification.EXTRA_TEXT)

        //保存所有通知信息
        val notificationBean = NotificationBean()
        notificationBean.uuid = UUID.randomUUID().toString()
        notificationBean.packageName = packageName
        notificationBean.notificationTitle = extras.getString(Notification.EXTRA_TITLE)
        notificationBean.notificationMsg = notificationText
        notificationBean.postTime = System.currentTimeMillis().timestampToCompleteDate()
        notificationBeanDao.save(notificationBean)

        Log.d(kTag, notificationBean.toJson())

        if (packageName == Constant.WECHAT || packageName == Constant.QQ) {
            openApplication(Constant.DING_DING)
        } else {
            if (notificationText.isNullOrBlank()) {
                return
            }
            if (notificationText.contains("考勤打卡")) {
                sendMail(notificationText)
            }
        }
    }

    private fun sendMail(message: String) {
        val emailAddress = SaveKeyValues.getValue(Constant.EMAIL_ADDRESS, "") as String
        if (emailAddress.isBlank()) {
            Log.d(kTag, "邮箱地址为空")
            return
        }
        //发送打卡成功的邮件
        CoroutineScope(Dispatchers.Main).launch {
            withContext(Dispatchers.IO) {
                message.createMail(emailAddress).sendTextMail()
            }

            val intent = Intent(
                this@NotificationMonitorService, MainActivity::class.java
            )
            intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK
            startActivity(intent)
        }
    }

    /**
     * 当有通知移除时会回调
     */
    override fun onNotificationRemoved(sbn: StatusBarNotification) {}

    override fun onListenerDisconnected() {
        Log.d(kTag, "onListenerDisconnected")
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
            // 通知侦听器断开连接 - 请求重新绑定
            requestRebind(ComponentName(this, NotificationListenerService::class.java))
        }
    }
}