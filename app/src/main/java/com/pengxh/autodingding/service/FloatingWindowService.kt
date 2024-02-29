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
import android.widget.Toast
import com.pengxh.autodingding.R
import com.pengxh.autodingding.fragment.SettingsFragment
import com.pengxh.kt.lite.extensions.getSystemService


class FloatingWindowService : Service() {

    private val kTag = "FloatingWindowService"
    private var floatView: View? = null
    private val windowManager by lazy { getSystemService<WindowManager>() }
    private val layoutInflater by lazy { LayoutInflater.from(this) }

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
            floatView = layoutInflater.inflate(R.layout.window_floating, null)

            initFloatingView(floatView!!)
        }
        return super.onStartCommand(intent, flags, startId)
    }

    private fun initFloatingView(view: View) {
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

        windowManager?.addView(view, floatLayoutParams)

        view.setOnClickListener {
            Toast.makeText(
                this,
                "无实际功能，仅为绕过Android 10+系统打卡之后无法回到桌面的问题",
                Toast.LENGTH_SHORT
            ).show()
        }
    }
}