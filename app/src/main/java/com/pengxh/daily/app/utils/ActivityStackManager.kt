package com.pengxh.daily.app.utils

import android.app.Activity
import java.util.ArrayDeque
import java.util.concurrent.locks.ReentrantLock

object ActivityStackManager {
    private val activityStack = ArrayDeque<Activity>()
    private val lock = ReentrantLock()

    /**
     * 添加Activity到堆栈
     */
    fun addActivity(activity: Activity?) {
        if (activity == null) {
            return
        }
        lock.lock()
        try {
            activityStack.addLast(activity)
        } finally {
            lock.unlock()
        }
    }

    /**
     * 获取当前Activity（堆栈中最后一个压入的）
     */
    fun currentActivity(): Activity? {
        lock.lock()
        try {
            return activityStack.lastOrNull()
        } finally {
            lock.unlock()
        }
    }

    /**
     * 结束当前Activity（堆栈中最后一个压入的）
     */
    fun finishCurrentActivity() {
        lock.lock()
        try {
            val activity = activityStack.pollLast() ?: return
            finishActivityInternal(activity)
        } finally {
            lock.unlock()
        }
    }

    /**
     * 结束指定的Activity
     */
    fun finishActivity(activity: Activity?) {
        if (activity == null) {
            return
        }
        lock.lock()
        try {
            activityStack.remove(activity)
            finishActivityInternal(activity)
        } finally {
            lock.unlock()
        }
    }

    /**
     * 结束指定类名的Activity
     */
    fun <T> finishActivity(clazz: Class<T>) {
        lock.lock()
        try {
            val activitiesToRemove = activityStack.filter { it.javaClass == clazz }.toMutableList()
            for (activity in activitiesToRemove) {
                finishActivityInternal(activity)
            }
        } finally {
            lock.unlock()
        }
    }

    /**
     * 结束所有Activity
     */
    fun finishAllActivity() {
        lock.lock()
        try {
            for (activity in activityStack) {
                finishActivityInternal(activity)
            }
            activityStack.clear()
        } finally {
            lock.unlock()
        }
    }

    /**
     * 内部方法，结束Activity并记录日志
     */
    private fun finishActivityInternal(activity: Activity) {
        if (!activity.isFinishing) {
            activity.finish()
        }
    }
}