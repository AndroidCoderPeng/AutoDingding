package com.pengxh.autodingding.extensions

fun Long.formatTime(): String {
    val hours = this / 3600
    val minutes = (this % 3600) / 60
    val secs = this % 60
    return String.format("%02d小时%02d分钟%02d秒", hours, minutes, secs)
}