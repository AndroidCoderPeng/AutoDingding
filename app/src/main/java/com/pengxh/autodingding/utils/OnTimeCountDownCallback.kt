package com.pengxh.autodingding.utils

interface OnTimeCountDownCallback {
    fun updateCountDownSeconds(remainingSeconds: Long)

    fun onFinish()
}