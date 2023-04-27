package com.pengxh.autodingding.extensions

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