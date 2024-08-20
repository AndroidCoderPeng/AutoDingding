package com.pengxh.autodingding.service

import android.app.Notification
import android.content.Intent
import android.service.notification.NotificationListenerService
import android.service.notification.StatusBarNotification
import android.util.Log
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleOwner
import androidx.lifecycle.LifecycleRegistry
import androidx.lifecycle.lifecycleScope
import com.pengxh.autodingding.BaseApplication
import com.pengxh.autodingding.bean.NotificationBean
import com.pengxh.autodingding.extensions.createTextMail
import com.pengxh.autodingding.extensions.openApplication
import com.pengxh.autodingding.extensions.sendTextMail
import com.pengxh.autodingding.extensions.show
import com.pengxh.autodingding.ui.MainActivity
import com.pengxh.autodingding.utils.Constant
import com.pengxh.autodingding.utils.CountDownTimerManager
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
        Log.d(kTag, "onNotificationPosted: $notice")

        val notificationBean = NotificationBean()
        notificationBean.uuid = UUID.randomUUID().toString()
        notificationBean.packageName = packageName
        notificationBean.notificationTitle = title
        notificationBean.notificationMsg = notice
        notificationBean.postTime = System.currentTimeMillis().timestampToCompleteDate()
        notificationBeanDao.save(notificationBean)

        if (packageName == Constant.DING_DING) {
            if (notice.contains("成功")) {
                lifecycleScope.launch(Dispatchers.Main) {
                    backToMainActivity()
                }

                val emailAddress = SaveKeyValues.getValue(Constant.EMAIL_ADDRESS, "") as String
                if (emailAddress.isEmpty()) {
                    "邮箱地址为空".show(this)
                    return
                }
                //发送打卡成功的邮件
                lifecycleScope.launch(Dispatchers.Main) {
                    "即将发送通知邮件，请注意查收".show(this@NotificationMonitorService)
                    withContext(Dispatchers.IO) {
                        notice.createTextMail(emailAddress).sendTextMail()
                    }
                }
            }
        } else if (packageName == Constant.WECHAT || packageName == Constant.QQ || packageName == Constant.TIM) {
            openApplication(Constant.DING_DING)
        }
    }

    private suspend fun backToMainActivity() {
        CountDownTimerManager.get.cancelTimer()

        //模拟点击Home键
        val home = Intent(Intent.ACTION_MAIN)
        home.flags = Intent.FLAG_ACTIVITY_NEW_TASK
        home.addCategory(Intent.CATEGORY_HOME)
        startActivity(home)
        Log.d(kTag, "onFinish: 模拟点击Home键")

        delay(1000)

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
    }
}