package com.pengxh.autodingding.extensions

import android.content.ComponentName
import android.content.Context
import android.content.Intent
import android.content.pm.ApplicationInfo
import android.content.pm.ResolveInfo
import androidx.core.app.NotificationManagerCompat
import com.pengxh.autodingding.utils.Constant
import com.pengxh.autodingding.utils.CountDownTimerManager
import com.pengxh.kt.lite.utils.SaveKeyValues

/**
 * 检测通知监听服务是否被授权
 * */
fun Context.notificationEnable(): Boolean {
    val packages = NotificationManagerCompat.getEnabledListenerPackages(this)
    return packages.contains(this.packageName)
}

/**
 * 检查手机上是否安装了指定的软件
 */
fun Context.isAppAvailable(packageName: String): Boolean {
    val packageManager = this.packageManager
    //获取所有已安装程序的包信息
    val packages = packageManager.getInstalledPackages(0)
    val packageNames = ArrayList<String>()
    packages.forEach {
        if (it.applicationInfo.flags and ApplicationInfo.FLAG_SYSTEM == 0) {
            //非系统应用
            packageNames.add(it.packageName)
        }
    }
    return packageNames.contains(packageName)
}

/**
 * 打开指定包名的apk
 */
fun Context.openApplication(packageName: String) {
    val packageManager = this.packageManager
    val resolveIntent = Intent(Intent.ACTION_MAIN, null)
    resolveIntent.addCategory(Intent.CATEGORY_LAUNCHER)
    resolveIntent.setPackage(packageName)
    val apps = packageManager.queryIntentActivities(resolveIntent, 0)
    val iterator: Iterator<ResolveInfo> = apps.iterator()
    if (!iterator.hasNext()) {
        return
    }
    val resolveInfo = iterator.next()
    val className = resolveInfo.activityInfo.name
    val intent = Intent(Intent.ACTION_MAIN)
    intent.addCategory(Intent.CATEGORY_LAUNCHER)
    intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
    val cn = ComponentName(packageName, className)
    intent.component = cn
    this.startActivity(intent)
    //倒计时，记录在钉钉界面停留的时间，超过设定的超时时间，自动回到打卡工具，并记录异常日志
    val time = SaveKeyValues.getValue(Constant.TIMEOUT, "15s") as String
    //去掉时间的s
    val timeValue = time.dropLast(1).toInt()
    CountDownTimerManager.get.startTimer(this, timeValue * 1000L, 1000)
}