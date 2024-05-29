package com.pengxh.autodingding.utils

import android.content.Context
import android.os.PowerManager
import android.util.Log
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleOwner
import androidx.lifecycle.LifecycleRegistry


class WakeLockManager private constructor() : LifecycleOwner {

    private val kTag = "WakeLockManager"
    private val registry = LifecycleRegistry(this)

    override fun getLifecycle(): Lifecycle {
        return registry
    }

    companion object {
        //Kotlin委托模式双重锁单例
        val get by lazy(mode = LazyThreadSafetyMode.SYNCHRONIZED) {
            WakeLockManager()
        }
    }

    fun wakeUpScreen(context: Context) {
        Log.d(kTag, "wakeUpScreen: ")
        val wakeLock: PowerManager.WakeLock =
            (context.getSystemService(Context.POWER_SERVICE) as PowerManager).run {
                newWakeLock(PowerManager.PARTIAL_WAKE_LOCK, "WakeLockManager::WakelockTag").apply {
                    acquire(60 * 1000L)
                }
            }
        wakeLock.release()
    }
}