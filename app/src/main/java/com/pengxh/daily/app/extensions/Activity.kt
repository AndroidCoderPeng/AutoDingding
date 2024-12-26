package com.pengxh.daily.app.extensions

import android.app.Activity
import com.github.gzuliyujiang.wheelpicker.TimePicker
import com.github.gzuliyujiang.wheelpicker.annotation.TimeMode
import com.pengxh.daily.app.R
import com.pengxh.daily.app.bean.DailyTaskBean
import com.pengxh.daily.app.utils.OnTimeSelectedCallback
import com.pengxh.kt.lite.extensions.convertColor

/**
 * 修改时间
 * */
fun Activity.showTimePicker(dailyTaskBean: DailyTaskBean, callback: OnTimeSelectedCallback) {
    val timePicker = TimePicker(this)
    val layout = timePicker.wheelLayout
    layout.setTimeMode(TimeMode.HOUR_24_HAS_SECOND)
    layout.setTimeLabel("时", "分", "秒")
    layout.setDefaultValue(dailyTaskBean.convertToTimeEntity())
    layout.setSelectedTextColor(R.color.colorAppThemeLight.convertColor(this))
    layout.setSelectedTextBold(true)

    timePicker.setOnTimePickedListener { hour, minute, seconds ->
        callback.onTimePicked(String.format("%02d:%02d:%02d", hour, minute, seconds))
    }
    timePicker.show()
}

/**
 * 添加时间
 * */
fun Activity.showTimePicker(callback: OnTimeSelectedCallback) {
    val timePicker = TimePicker(this)
    val layout = timePicker.wheelLayout
    layout.setTimeMode(TimeMode.HOUR_24_HAS_SECOND)
    layout.setTimeLabel("时", "分", "秒")
    layout.setSelectedTextColor(R.color.colorAppThemeLight.convertColor(this))
    layout.setSelectedTextBold(true)

    timePicker.setOnTimePickedListener { hour, minute, seconds ->
        callback.onTimePicked(String.format("%02d:%02d:%02d", hour, minute, seconds))
    }
    timePicker.show()
}