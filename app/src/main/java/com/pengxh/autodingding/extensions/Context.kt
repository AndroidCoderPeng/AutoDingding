package com.pengxh.autodingding.extensions

import android.content.Context
import androidx.core.app.NotificationManagerCompat

/**
 * 检测通知监听服务是否被授权
 * */
fun Context.notificationEnable(): Boolean {
    val packages = NotificationManagerCompat.getEnabledListenerPackages(this)
    return packages.contains(this.packageName)
}