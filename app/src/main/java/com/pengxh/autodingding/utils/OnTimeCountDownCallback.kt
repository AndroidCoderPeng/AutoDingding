package com.pengxh.autodingding.utils

interface OnTimeCountDownCallback {
    fun updateCountDownSeconds(remainingSeconds: Int)

    fun onFinish()
}