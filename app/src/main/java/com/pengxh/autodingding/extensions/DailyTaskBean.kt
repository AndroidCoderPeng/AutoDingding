package com.pengxh.autodingding.extensions

import com.github.gzuliyujiang.wheelpicker.entity.TimeEntity
import com.pengxh.autodingding.bean.DailyTaskBean
import com.pengxh.autodingding.utils.TimeKit
import java.text.SimpleDateFormat
import java.util.Locale

fun DailyTaskBean.convertToTimeEntity(): TimeEntity {
    val dateFormat = SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.CHINA)
    val date = dateFormat.parse("${TimeKit.getTodayDate()} ${this.time}")!!
    return TimeEntity.target(date)
}

fun DailyTaskBean.isLateThenCurrent(): Boolean {
    //获取当前日期，拼给任务时间，不然不好计算时间差
    val simpleDateFormat = SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.CHINA)
    val taskTime = "${TimeKit.getTodayDate()} ${this.time}"
    val taskDate = simpleDateFormat.parse(taskTime) ?: return false
    val currentMillis = System.currentTimeMillis()
    return taskDate.time > currentMillis
}

fun DailyTaskBean.diffCurrent(): Long {
    //获取当前日期，拼给任务时间，不然不好计算时间差
    val simpleDateFormat = SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.CHINA)
    val taskTime = "${TimeKit.getTodayDate()} ${this.time}"
    val taskDate = simpleDateFormat.parse(taskTime) ?: return 0
    val currentMillis = System.currentTimeMillis()
    return (taskDate.time - currentMillis) / 1000
}