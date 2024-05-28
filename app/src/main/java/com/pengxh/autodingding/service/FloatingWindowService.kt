package com.pengxh.autodingding.service

import android.app.Service
import android.content.Intent
import android.graphics.PixelFormat
import android.os.Build
import android.os.IBinder
import android.view.Gravity
import android.view.LayoutInflater
import android.view.View
import android.view.WindowManager
import com.pengxh.autodingding.R
import com.pengxh.kt.lite.extensions.getSystemService
import com.pengxh.kt.lite.extensions.show


class FloatingWindowService : Service() {

    private val kTag = "FloatingWindowService"
    private val windowManager by lazy { getSystemService<WindowManager>() }
    private val layoutInflater by lazy { LayoutInflater.from(this) }
    private var floatView: View? = null

    override fun onBind(intent: Intent?): IBinder? {
        return null
    }

    override fun onCreate() {
        super.onCreate()
        floatView = layoutInflater.inflate(R.layout.window_floating, null)
    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        if (floatView == null) {
            floatView = layoutInflater.inflate(R.layout.window_floating, null)
        }
        initFloatingView(floatView)
        return START_STICKY
    }

    private fun initFloatingView(view: View?) {
        val layoutType = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            WindowManager.LayoutParams.TYPE_APPLICATION_OVERLAY
        } else {
            WindowManager.LayoutParams.TYPE_TOAST
        }
        val floatLayoutParams = WindowManager.LayoutParams(
            WindowManager.LayoutParams.WRAP_CONTENT,
            WindowManager.LayoutParams.WRAP_CONTENT,
            layoutType,
            WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE,
            PixelFormat.TRANSLUCENT
        )
        floatLayoutParams.gravity = Gravity.BOTTOM

        try {
            windowManager?.addView(view, floatLayoutParams)
            view?.setOnClickListener {
                "无实际功能，仅为绕过Android 10+系统打卡之后无法回到桌面的问题".show(this)
            }
        } catch (e: IllegalStateException) {
            e.printStackTrace()
        }
    }
}