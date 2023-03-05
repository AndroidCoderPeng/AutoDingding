package com.pengxh.autodingding.utils

import android.app.KeyguardManager
import android.app.NotificationManager
import android.content.ComponentName
import android.content.Context
import android.content.Intent
import android.content.pm.ResolveInfo
import android.os.CountDownTimer
import android.os.PowerManager
import android.util.Log
import android.view.WindowManager
import com.pengxh.autodingding.BaseApplication

/**
 * @author: Pengxh
 * @email: 290677893@qq.com
 * @date: 2019/12/25 13:13
 */
object DingDingUtil {
    private const val kTag = "DingDingUtil"

    private lateinit var manager: NotificationManager

    fun init() {
        manager = BaseApplication.get()
            .getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
    }

    /**
     * TODO
     * 检查手机上是否安装了指定的软件
     *
     * @param packageName 应用包名
     */
    fun isAppAvailable(packageName: String): Boolean {
        val packageManager = BaseApplication.get().packageManager
        //获取所有已安装程序的包信息
        val packageInfos = packageManager.getInstalledPackages(0)
        val packageNames: MutableList<String> = ArrayList()
        for (i in packageInfos.indices) {
            val packName = packageInfos[i].packageName
            packageNames.add(packName)
        }
//        return packageNames.contains(packageName)
        return true
    }

    /**
     * 打开指定包名的apk
     *
     * @param packageName 应用包名
     */
    fun openDingDing(packageName: String?) {
        wakeUpAndUnlock()
        Log.d(kTag, "openDingDing: 已亮屏，1s后启动钉钉")
        object : CountDownTimer(1000, 1000) {
            override fun onTick(millisUntilFinished: Long) {}
            override fun onFinish() {
                val packageManager = BaseApplication.get().packageManager
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
                val cn = ComponentName(packageName!!, className)
                intent.component = cn
                BaseApplication.get().startActivity(intent)
            }
        }.start()
    }

    /**
     * 唤醒屏幕并解锁
     */
    private fun wakeUpAndUnlock() {
        Log.d(kTag, "wakeUpAndUnlock: 亮屏解锁")
        val manager = BaseApplication.get().getSystemService(Context.POWER_SERVICE) as PowerManager
        val screenOn = manager.isInteractive
        if (!screenOn) {
            //唤醒屏幕
            val wakeLock = manager.newWakeLock(
                PowerManager.ACQUIRE_CAUSES_WAKEUP or WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON,
                "dTag:screenOn"
            )
            wakeLock.acquire(10000)
            wakeLock.release()
        }
        //解锁屏幕
        val keyguardManager =
            BaseApplication.get().getSystemService(Context.KEYGUARD_SERVICE) as KeyguardManager
        val keyguardLock = keyguardManager.newKeyguardLock("unLock")
        keyguardLock.disableKeyguard()
    }
}