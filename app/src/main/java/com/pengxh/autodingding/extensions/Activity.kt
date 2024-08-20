package com.pengxh.autodingding.extensions

import android.app.Activity
import com.github.gzuliyujiang.wheelpicker.DatePicker
import com.github.gzuliyujiang.wheelpicker.DatimePicker
import com.github.gzuliyujiang.wheelpicker.annotation.DateMode
import com.github.gzuliyujiang.wheelpicker.annotation.TimeMode
import com.github.gzuliyujiang.wheelpicker.entity.DatimeEntity
import com.pengxh.autodingding.R
import com.pengxh.autodingding.bean.DateTimeBean
import com.pengxh.autodingding.utils.OnDateSelectedCallback
import com.pengxh.kt.lite.extensions.appendZero
import com.pengxh.kt.lite.extensions.convertColor

fun Activity.showDateTimePicker(dateTimeBean: DateTimeBean?, callback: OnDateSelectedCallback) {
    val datePicker = DatimePicker(this)
    val layout = datePicker.wheelLayout
    if (dateTimeBean != null) {
        layout.setRange(dateTimeBean.convertToDateTimeEntity(), DatimeEntity.monthOnFuture(1))
    }
    layout.setDateMode(DateMode.YEAR_MONTH_DAY)
    layout.setDateLabel("年", "月", "日")
    layout.setTimeMode(TimeMode.HOUR_24_NO_SECOND)
    layout.setTimeLabel("时", "分", "秒")
    layout.setSelectedTextColor(R.color.colorAppThemeLight.convertColor(this))
    layout.setSelectedTextBold(true)

    datePicker.setOnDatimePickedListener { year, month, day, hour, minute, second ->
        callback.onTimePicked(
            year.toString(), month.appendZero(), day.appendZero(),
            hour.appendZero(), minute.appendZero(), second.appendZero()
        )
    }
    datePicker.show()
}

fun Activity.showDatePicker(dateTimeBean: DateTimeBean, callback: OnDateSelectedCallback) {
    val datePicker = DatePicker(this)
    val layout = datePicker.wheelLayout
    layout.setRange(dateTimeBean.convertToDateEntity(), null)
    layout.setDateMode(DateMode.YEAR_MONTH_DAY)
    layout.setDateLabel("年", "月", "日")
    layout.setSelectedTextColor(R.color.colorAppThemeLight.convertColor(this))
    layout.setSelectedTextBold(true)

    datePicker.setOnDatePickedListener { year, month, day ->
        callback.onTimePicked(year.toString(), month.appendZero(), day.appendZero())
    }
    datePicker.show()
}