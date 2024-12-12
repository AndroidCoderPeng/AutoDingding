package com.pengxh.autodingding.utils

import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Date
import java.util.Locale

object TimeKit {
    fun getTodayDate(): String {
        val dateFormat = SimpleDateFormat("yyyy-MM-dd", Locale.CHINA)
        return dateFormat.format(Date())
    }

    fun getCurrentTime(): String {
        val dateFormat = SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.CHINA)
        return dateFormat.format(Date())
    }

    fun getNextMidnightSeconds(): Long {
        val nextMidnightMillis = Calendar.getInstance().apply {
            set(Calendar.HOUR_OF_DAY, 0)
            set(Calendar.MINUTE, 0)
            set(Calendar.SECOND, 0)
            set(Calendar.MILLISECOND, 0)
            add(Calendar.DATE, 1)
        }.timeInMillis
        return (nextMidnightMillis - System.currentTimeMillis()) / 1000
    }
}