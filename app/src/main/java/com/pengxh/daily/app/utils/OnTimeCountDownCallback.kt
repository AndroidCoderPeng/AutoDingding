package com.pengxh.daily.app.utils

interface OnTimeCountDownCallback {
    fun updateCountDownSeconds(seconds: Int)

    fun onFinish()
}