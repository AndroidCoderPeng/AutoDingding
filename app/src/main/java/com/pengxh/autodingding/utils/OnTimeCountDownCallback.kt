package com.pengxh.autodingding.utils

interface OnTimeCountDownCallback {
    fun updateCountDownSeconds(seconds: Int)

    fun onFinish()
}