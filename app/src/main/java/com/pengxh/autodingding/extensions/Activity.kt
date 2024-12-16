package com.pengxh.autodingding.extensions

import android.app.Activity
import com.github.gzuliyujiang.wheelpicker.TimePicker
import com.github.gzuliyujiang.wheelpicker.annotation.TimeMode
import com.pengxh.autodingding.R
import com.pengxh.autodingding.bean.DailyTaskBean
import com.pengxh.autodingding.utils.OnTimeSelectedCallback
import com.pengxh.kt.lite.extensions.appendZero
import com.pengxh.kt.lite.extensions.convertColor

/**
 * 修改时间
 * */
fun Activity.showTimePicker(dailyTaskBean: DailyTaskBean, callback: OnTimeSelectedCallback) {
    val timePicker = TimePicker(this)
    val layout = timePicker.wheelLayout
    layout.setTimeMode(TimeMode.HOUR_24_NO_SECOND)
    layout.setTimeLabel("时", "分", "秒")
    layout.setDefaultValue(dailyTaskBean.convertToTimeEntity())
    layout.setSelectedTextColor(R.color.colorAppThemeLight.convertColor(this))
    layout.setSelectedTextBold(true)

    timePicker.setOnTimePickedListener { hour, minute, second ->
        val time = "${hour.appendZero()}:${minute.appendZero()}:${second.appendZero()}"
        callback.onTimePicked(time)
    }
    timePicker.show()
}

/**
 * 添加时间
 * */
fun Activity.showTimePicker(callback: OnTimeSelectedCallback) {
    val timePicker = TimePicker(this)
    val layout = timePicker.wheelLayout
    layout.setTimeMode(TimeMode.HOUR_24_NO_SECOND)
    layout.setTimeLabel("时", "分", "秒")
    layout.setSelectedTextColor(R.color.colorAppThemeLight.convertColor(this))
    layout.setSelectedTextBold(true)

    timePicker.setOnTimePickedListener { hour, minute, second ->
        val time = "${hour.appendZero()}:${minute.appendZero()}:${second.appendZero()}"
        callback.onTimePicked(time)
    }
    timePicker.show()
}