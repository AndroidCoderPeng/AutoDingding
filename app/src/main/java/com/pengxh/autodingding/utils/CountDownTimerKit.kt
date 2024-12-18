package com.pengxh.autodingding.utils

import android.os.Handler
import android.os.Looper
import android.util.Log
import java.lang.ref.WeakReference

class CountDownTimerKit(private val secondsInFuture: Int, callback: OnTimeCountDownCallback) {
    private val kTag = "CountDownTimerKit"
    private val handler = Handler(Looper.getMainLooper())
    private var isTimerRunning = false
    private val weakCallback = WeakReference(callback)

    private val runnable = object : Runnable {
        var remainingSeconds = secondsInFuture
        override fun run() {
            remainingSeconds--
            if (remainingSeconds > 0) {
                weakCallback.get()?.updateCountDownSeconds(remainingSeconds)
                handler.postDelayed(this, 1000)
            } else {
                weakCallback.get()?.onFinish()
                isTimerRunning = false
                Log.d(kTag, "Countdown finished")
            }
        }
    }

    fun start() {
        if (isTimerRunning) {
            cancel()
        }
        handler.post(runnable)
        isTimerRunning = true
        Log.d(kTag, "Countdown started")
    }

    fun cancel() {
        handler.removeCallbacks(runnable)
        isTimerRunning = false
        Log.d(kTag, "Countdown cancelled")
    }
}