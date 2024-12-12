package com.pengxh.autodingding.extensions

import com.pengxh.autodingding.bean.DateTimeBean
import java.text.SimpleDateFormat
import java.util.Locale

/**
 * 找出任务中，第一个时间晚于当前时间的任务Index
 * */
fun List<DateTimeBean>.getTaskIndex(): Int {
    if (this.count() == 1) {
        return 0
    }
    val timeFormat = SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.getDefault())
    val currentMillis = System.currentTimeMillis()
    for ((index, task) in this.withIndex()) {
        val taskTime = "${task.date} ${task.time}"
        val taskDate = timeFormat.parse(taskTime) ?: continue
        // 如果任务时间晚于当前时间，则返回该任务的索引
        if (taskDate.time > currentMillis) {
            return index
        }
    }
    return -1
}