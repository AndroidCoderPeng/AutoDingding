package com.pengxh.autodingding.extensions

import android.content.ComponentName
import android.content.Intent
import android.content.pm.ResolveInfo
import android.os.CountDownTimer
import com.pengxh.autodingding.BaseApplication
import com.pengxh.autodingding.utils.DingDingUtil
import java.text.ParseException
import java.text.SimpleDateFormat
import java.util.*
import kotlin.math.abs

fun String.convertToWeek(): String {
    val format = SimpleDateFormat("yyyy-MM-dd", Locale.CHINA)
    val calendar = Calendar.getInstance()
    try {
        calendar.time = format.parse(this)!!
    } catch (e: ParseException) {
        e.printStackTrace()
    }
    return when (calendar.get(Calendar.DAY_OF_WEEK)) {
        1 -> return "周日"
        2 -> return "周一"
        3 -> return "周二"
        4 -> return "周三"
        5 -> return "周四"
        6 -> return "周五"
        7 -> return "周六"
        else -> "错误"
    }
}

fun String.isEarlierThenCurrent(): Boolean {
    try {
        val dateFormat = SimpleDateFormat("yyyy-MM-dd HH:mm", Locale.CHINA)
        val date = dateFormat.parse(this)!!
        val t1 = date.time
        val t2 = System.currentTimeMillis()
        return (t1 - t2) < 0
    } catch (e: ParseException) {
        e.printStackTrace()
    }
    return false
}

/**
 * 时间差-秒
 * */
fun String.diffCurrentMillis(): Long {
    if (this.isBlank()) {
        return 0
    }
    val simpleDateFormat = SimpleDateFormat("yyyy-MM-dd HH:mm", Locale.CHINA)
    val date = simpleDateFormat.parse(this)!!
    return abs(System.currentTimeMillis() - date.time)
}

/**
 * 检查手机上是否安装了指定的软件
 * TODO
 */
fun String.isAppAvailable(): Boolean {
    val packageManager = BaseApplication.get().packageManager
    //获取所有已安装程序的包信息
    val packages = packageManager.getInstalledPackages(0)
    val packageNames: MutableList<String> = ArrayList()
    for (i in packages.indices) {
        val packName = packages[i].packageName
        packageNames.add(packName)
    }
    return packageNames.contains(this)
}

/**
 * 打开指定包名的apk
 */
fun String.openDingDing() {
    DingDingUtil.wakeUpAndUnlock()
    object : CountDownTimer(1000, 1000) {
        override fun onTick(millisUntilFinished: Long) {}
        override fun onFinish() {
            val packageManager = BaseApplication.get().packageManager
            val resolveIntent = Intent(Intent.ACTION_MAIN, null)
            resolveIntent.addCategory(Intent.CATEGORY_LAUNCHER)
            resolveIntent.setPackage(this@openDingDing)
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
            val cn = ComponentName(this@openDingDing, className)
            intent.component = cn
            BaseApplication.get().startActivity(intent)
        }
    }.start()
}