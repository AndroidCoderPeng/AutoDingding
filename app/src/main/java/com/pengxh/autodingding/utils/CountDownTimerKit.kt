package com.pengxh.autodingding.utils

import android.os.Handler
import android.os.Looper

class CountDownTimerKit(
    private val secondsInFuture: Long, private val callback: OnTimeCountDownCallback
) {
    private val handler = Handler(Looper.getMainLooper())
    private var isTimerRunning = false

    private val runnable = object : Runnable {
        var remainingSeconds = secondsInFuture
        override fun run() {
            remainingSeconds--
            if (remainingSeconds > 0) {
                callback.updateCountDownSeconds(remainingSeconds)
                handler.postDelayed(this, 1000)
            } else {
                callback.onFinish()
                isTimerRunning = false
            }
        }
    }

    fun start() {
        if (isTimerRunning) {
            cancel()
        }
        handler.post(runnable)
        isTimerRunning = true
    }

    fun cancel() {
        handler.removeCallbacks(runnable)
        isTimerRunning = false
    }
}