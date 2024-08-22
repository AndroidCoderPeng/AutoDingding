package com.pengxh.autodingding.utils

import android.app.Activity
import java.util.Stack

object ActivityStackManager {
    private val activityStack = Stack<Activity>()

    /**
     * 添加Activity到堆栈
     */
    fun addActivity(activity: Activity?) {
        if (activity == null) {
            return
        }
        activityStack.push(activity)
    }

    fun getStackActivityCount(): Int {
        if (activityStack.isEmpty()) {
            return 0
        }
        return activityStack.size
    }
}