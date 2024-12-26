package com.pengxh.daily.app.extensions

import com.github.gzuliyujiang.wheelpicker.entity.TimeEntity
import com.pengxh.daily.app.bean.DailyTaskBean
import com.pengxh.daily.app.utils.TimeKit
import com.pengxh.kt.lite.extensions.appendZero
import java.text.SimpleDateFormat
import java.util.Locale

fun DailyTaskBean.convertToTimeEntity(): TimeEntity {
    val dateFormat = SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.CHINA)
    val date = dateFormat.parse("${TimeKit.getTodayDate()} ${this.time}")!!
    return TimeEntity.target(date)
}

fun DailyTaskBean.random(): Pair<String, Int> {
    //18:00:59
    val array = this.time.split(":")

    //随机[0,5]分钟内随机
    val seedMinute = (0 until 5).random()
    val tempMinute = array[1].toInt() + seedMinute
    val minute = if (tempMinute >= 60) {
        array[1]
    } else {
        tempMinute.appendZero()
    }

    //随机[0,60]秒内随机
    val seedSeconds = (0 until 60).random()
    val tempSeconds = array[1].toInt() + seedSeconds
    val seconds = if (tempSeconds >= 60) {
        array[2]
    } else {
        tempSeconds.appendZero()
    }
    val newTime = "${array[0]}:${minute}:${seconds}"

    //获取当前日期，拼给任务时间，不然不好计算时间差
    val simpleDateFormat = SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.CHINA)
    val taskTime = "${TimeKit.getTodayDate()} $newTime"
    val taskDate = simpleDateFormat.parse(taskTime) ?: return Pair(newTime, 0)
    val currentMillis = System.currentTimeMillis()
    val diffSeconds = (taskDate.time - currentMillis) / 1000
    return Pair(newTime, diffSeconds.toInt())
}