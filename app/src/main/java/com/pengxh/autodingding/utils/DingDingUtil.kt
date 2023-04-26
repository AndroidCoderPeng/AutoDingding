package com.pengxh.autodingding.utils

import android.app.KeyguardManager
import android.content.Context
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

    /**
     * 唤醒屏幕并解锁
     */
    fun wakeUpAndUnlock() {
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