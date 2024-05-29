package com.pengxh.autodingding.service

import android.app.Notification
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.Service
import android.content.ComponentName
import android.content.Intent
import android.content.ServiceConnection
import android.os.Build
import android.os.IBinder
import android.util.Log
import androidx.core.app.NotificationCompat
import com.pengxh.autodingding.IKeepAliveAidlInterface
import com.pengxh.autodingding.R

class RemoteForegroundService : Service() {

    private val kTag = "RemoteForegroundService"

    private var binder = object : IKeepAliveAidlInterface.Stub() {
        override fun basicTypes(
            anInt: Int, aLong: Long, aBoolean: Boolean, aFloat: Float,
            aDouble: Double, aString: String?
        ) {
            //通信内容
        }
    }

    override fun onBind(intent: Intent?): IBinder {
        return binder
    }

    override fun onCreate() {
        super.onCreate()
        startForegroundService()
    }

    private fun startForegroundService() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val channel = NotificationChannel(
                "service", "service", NotificationManager.IMPORTANCE_NONE
            )
            channel.lockscreenVisibility = Notification.VISIBILITY_PRIVATE
            val manager = getSystemService(NOTIFICATION_SERVICE) as NotificationManager
            manager.createNotificationChannel(channel)
            val builder = NotificationCompat.Builder(this, "service")
            val notification = builder.setOngoing(true)
                .setContentTitle(resources.getString(R.string.app_name))
                .setContentText("后台保活服务运行中，请勿关闭此通知")
                .setSmallIcon(R.mipmap.logo)
                .setPriority(Notification.PRIORITY_MIN)
                .setCategory(Notification.CATEGORY_SERVICE)
                .build()
            startForeground(10, notification)
        } else {
            startForeground(10, Notification())
        }
    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        bindLocalService()
        return super.onStartCommand(intent, flags, startId)
    }

    private fun bindLocalService() {
        // 绑定另外一个服务
        val bindIntent = Intent(this, LocalForegroundService::class.java)
        // 绑定进程
        bindService(bindIntent, LocalServiceConnection(), BIND_AUTO_CREATE)
    }

    inner class LocalServiceConnection : ServiceConnection {
        override fun onServiceConnected(name: ComponentName?, service: IBinder?) {
            Log.d(kTag, "onServiceConnected: $name")
        }

        override fun onServiceDisconnected(name: ComponentName?) {
            Log.d(kTag, "onServiceDisconnected: $name")
            startForegroundService()
            bindLocalService()
        }
    }
}