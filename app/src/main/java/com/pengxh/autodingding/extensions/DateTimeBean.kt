package com.pengxh.autodingding.extensions

import com.github.gzuliyujiang.wheelpicker.entity.DateEntity
import com.github.gzuliyujiang.wheelpicker.entity.DatimeEntity
import com.github.gzuliyujiang.wheelpicker.entity.TimeEntity
import com.pengxh.autodingding.bean.DateTimeBean
import java.text.SimpleDateFormat
import java.util.Locale

fun DateTimeBean.convertToDateTimeEntity(): DatimeEntity {
    /**
     * {
     *   "date": "2024-08-10",
     *   "id": 1,
     *   "time": "17:13:33",
     *   "uuid": "cc75dda4-e916-46c3-9817-432ca6fbe7d8",
     *   "weekDay": "周六"
     * }
     * */
    val entity = DatimeEntity()
    entity.date = this.convertToDateEntity()
    entity.time = this.convertToTimeEntity()
    return entity
}

fun DateTimeBean.convertToDateEntity(): DateEntity {
    /**
     * {
     *   "date": "2024-08-10",
     *   "id": 1,
     *   "time": "17:13:33",
     *   "uuid": "cc75dda4-e916-46c3-9817-432ca6fbe7d8",
     *   "weekDay": "周六"
     * }
     * */
    val dateFormat = SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.CHINA)
    val date = dateFormat.parse("${this.date} ${this.time}")!!
    return DateEntity.target(date)
}

fun DateTimeBean.convertToTimeEntity(): TimeEntity {
    /**
     * {
     *   "date": "2024-08-10",
     *   "id": 1,
     *   "time": "17:13:33",
     *   "uuid": "cc75dda4-e916-46c3-9817-432ca6fbe7d8",
     *   "weekDay": "周六"
     * }
     * */
    val dateFormat = SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.CHINA)
    val date = dateFormat.parse("${this.date} ${this.time}")!!
    return TimeEntity.target(date)
}

fun DateTimeBean.isLateThenCurrent(): Boolean {
    //获取当前日期，拼给任务时间，不然不好计算时间差
    val simpleDateFormat = SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.getDefault())
    val taskTime = "${this.date} ${this.time}"
    val taskDate = simpleDateFormat.parse(taskTime) ?: return false
    val currentMillis = System.currentTimeMillis()
    return taskDate.time > currentMillis
}

fun DateTimeBean.diffCurrent(): Long {
    //获取当前日期，拼给任务时间，不然不好计算时间差
    val simpleDateFormat = SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.getDefault())
    val taskTime = "${this.date} ${this.time}"
    val taskDate = simpleDateFormat.parse(taskTime) ?: return 0
    val currentMillis = System.currentTimeMillis()
    return (taskDate.time - currentMillis) / 1000
}