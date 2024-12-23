package com.pengxh.autodingding.utils

import android.content.Context
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import com.pengxh.autodingding.model.HolidayModel
import com.pengxh.kt.lite.extensions.readAssetsFile
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Date
import java.util.Locale

object TimeKit {

    private val gson by lazy { Gson() }

    fun getTodayDate(): String {
        val dateFormat = SimpleDateFormat("yyyy-MM-dd", Locale.CHINA)
        return dateFormat.format(Date())
    }

    fun getCurrentTime(): String {
        val dateFormat = SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.CHINA)
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

    fun todayIsWorkDay(context: Context): Boolean {
        val assetsFile = context.readAssetsFile("Holiday.json")
        val holidays = gson.fromJson<MutableList<HolidayModel>>(
            assetsFile, object : TypeToken<MutableList<HolidayModel>>() {}.type
        )
        val result = holidays.find { x -> x.date == getTodayDate() }
        return if (result == null) {
            val calendar = Calendar.getInstance()
            calendar[Calendar.DAY_OF_WEEK] == Calendar.SATURDAY || calendar[Calendar.DAY_OF_WEEK] == Calendar.SUNDAY
        } else {
            // 节假日
            false
        }
    }
}