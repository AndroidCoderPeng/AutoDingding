package com.pengxh.autodingding.service

import android.app.Service
import android.content.Intent
import android.graphics.PixelFormat
import android.os.Build
import android.os.IBinder
import android.provider.Settings
import android.util.Log
import android.view.Gravity
import android.view.LayoutInflater
import android.view.View
import android.view.WindowManager
import com.pengxh.autodingding.R
import com.pengxh.autodingding.fragment.SettingsFragment
import com.pengxh.kt.lite.extensions.getSystemService
import com.pengxh.kt.lite.extensions.show


class FloatingWindowService : Service() {

    private val kTag = "FloatingWindowService"
    private var floatView: View? = null
    private val windowManager by lazy { getSystemService<WindowManager>() }
    private val layoutInflater by lazy { getSystemService<LayoutInflater>() }

    override fun onBind(intent: Intent?): IBinder? {
        return null
    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        Log.d(kTag, "onStartCommand => $startId")
        if (floatView == null) {
            if (!Settings.canDrawOverlays(this)) {
                SettingsFragment.weakReferenceHandler.sendEmptyMessage(20230831)
                return super.onStartCommand(intent, flags, startId)
            }
            initFloatingView()
        }
        return super.onStartCommand(intent, flags, startId)
    }

    private fun initFloatingView() {
        floatView = layoutInflater?.inflate(R.layout.window_floating, null)
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
        floatLayoutParams.gravity = Gravity.END
        floatLayoutParams.x = 0
        floatLayoutParams.y = 0

        windowManager?.addView(floatView, floatLayoutParams)

        floatView?.setOnClickListener {
            "此悬浮图标无实际功能，仅为绕过Android 10以上系统打卡之后无法回到桌面的问题".show(this)
        }
    }
}