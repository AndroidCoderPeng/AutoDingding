package com.pengxh.autodingding.extensions

import android.content.ComponentName
import android.content.Context
import android.content.Intent
import android.content.pm.ApplicationInfo
import android.content.pm.ResolveInfo
import androidx.core.app.NotificationManagerCompat
import com.pengxh.autodingding.fragment.DailyTaskFragment
import com.pengxh.autodingding.service.FloatingWindowService
import com.pengxh.autodingding.ui.MainActivity
import com.pengxh.autodingding.utils.Constant
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
    FloatingWindowService.weakReferenceHandler?.sendEmptyMessage(Constant.SHOW_FLOATING_WINDOW_CODE)
    /***跳转钉钉开始*****************************************/
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
    val intent = Intent(Intent.ACTION_MAIN)
    intent.addCategory(Intent.CATEGORY_LAUNCHER)
    intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
    intent.component = ComponentName(
        resolveInfo.activityInfo.packageName, resolveInfo.activityInfo.name
    )
    this.startActivity(intent)
    DailyTaskFragment.weakReferenceHandler?.sendEmptyMessage(Constant.START_COUNT_DOWN_TIMER_CODE)
}

fun Context.backToMainActivity() {
    DailyTaskFragment.weakReferenceHandler?.sendEmptyMessage(Constant.CANCEL_COUNT_DOWN_TIMER_CODE)
    if (SaveKeyValues.getValue(Constant.BACK_TO_HOME, false) as Boolean) {
        //模拟点击Home键
        val home = Intent(Intent.ACTION_MAIN)
        home.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_REORDER_TO_FRONT
        home.addCategory(Intent.CATEGORY_HOME)
        this.startActivity(home)
        Thread.sleep(2000)
    }

    val intent = Intent(this, MainActivity::class.java)
    intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_REORDER_TO_FRONT
    this.startActivity(intent)
}