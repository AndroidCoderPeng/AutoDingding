package com.pengxh.daily.app.utils

import com.google.gson.Gson
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Date
import java.util.Locale

object TimeKit {

    private const val kTag = "TimeKit"
    private val gson by lazy { Gson() }

    fun getTodayDate(): String {
        val dateFormat = SimpleDateFormat("yyyy-MM-dd", Locale.CHINA)
        return dateFormat.format(Date())
    }

    fun getNextMidnightSeconds(): Int {
        val nextMidnightMillis = Calendar.getInstance().apply {
            set(Calendar.HOUR_OF_DAY, 0)
            set(Calendar.MINUTE, 0)
            set(Calendar.SECOND, 0)
            set(Calendar.MILLISECOND, 0)
            add(Calendar.DATE, 1)
        }.timeInMillis
        val delta = (nextMidnightMillis - System.currentTimeMillis()) / 1000
        return delta.toInt()
    }
}