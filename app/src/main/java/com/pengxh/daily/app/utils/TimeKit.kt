package com.pengxh.daily.app.utils

import android.content.Context
import android.util.Log
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import com.pengxh.daily.app.model.HolidayModel
import com.pengxh.kt.lite.extensions.readAssetsFile
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

    /**
     * 先判断今天是否是法定节假日，再判断是否是周末
     * */
    fun todayIsHoliday(context: Context): Boolean {
        val assetsFile = context.readAssetsFile("Holiday.json")
        val holidays = gson.fromJson<MutableList<HolidayModel>>(
            assetsFile, object : TypeToken<MutableList<HolidayModel>>() {}.type
        )
        val result = holidays.find { x -> x.date == getTodayDate() }
        val isHoliday = if (result == null) {
            //没在法定节假日找到当前日期，说明是工作日（包含周末），接下来判断周末
            val calendar = Calendar.getInstance()
            calendar[Calendar.DAY_OF_WEEK] == Calendar.SATURDAY || calendar[Calendar.DAY_OF_WEEK] == Calendar.SUNDAY
        } else {
            //在法定节假日找到当前日期，说明是节假日
            true
        }
        Log.d(kTag, "today's date: ${getTodayDate()}, isHoliday: $isHoliday")
        return isHoliday
    }
}