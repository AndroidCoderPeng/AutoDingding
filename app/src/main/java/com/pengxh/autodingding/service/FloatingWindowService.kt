package com.pengxh.autodingding.service

import android.annotation.SuppressLint
import android.app.Service
import android.content.Intent
import android.graphics.PixelFormat
import android.os.Build
import android.os.Handler
import android.os.IBinder
import android.os.Message
import android.view.LayoutInflater
import android.view.MotionEvent
import android.view.WindowManager
import android.widget.TextView
import com.pengxh.autodingding.R
import com.pengxh.autodingding.utils.Constant
import com.pengxh.kt.lite.extensions.getSystemService
import com.pengxh.kt.lite.utils.SaveKeyValues
import com.pengxh.kt.lite.utils.WeakReferenceHandler


class FloatingWindowService : Service(), Handler.Callback {

    companion object {
        var weakReferenceHandler: WeakReferenceHandler? = null
    }

    private val kTag = "FloatingWindowService"
    private val windowManager by lazy { getSystemService<WindowManager>() }
    private val floatView by lazy {
        LayoutInflater.from(this).inflate(R.layout.window_floating, null)
    }
    private val textView by lazy { floatView.findViewById<TextView>(R.id.timeView) }
    private lateinit var floatLayoutParams: WindowManager.LayoutParams

    override fun onBind(intent: Intent?): IBinder? {
        return null
    }

    @SuppressLint("ClickableViewAccessibility")
    override fun onCreate() {
        super.onCreate()
        weakReferenceHandler = WeakReferenceHandler(this)
        val layoutType = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N_MR1) {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                //8.0
                WindowManager.LayoutParams.TYPE_APPLICATION_OVERLAY
            } else {
                //7.1.1
                WindowManager.LayoutParams.TYPE_PHONE
            }
        } else {
            //其他版本
            WindowManager.LayoutParams.TYPE_TOAST
        }
        floatLayoutParams = WindowManager.LayoutParams(
            WindowManager.LayoutParams.WRAP_CONTENT,
            WindowManager.LayoutParams.WRAP_CONTENT,
            layoutType,
            WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE,
            PixelFormat.TRANSLUCENT
        )

        try {
            windowManager?.addView(floatView, floatLayoutParams)

            var lastX = 0
            var lastY = 0
            var paramX = 0
            var paramY = 0

            floatView.setOnTouchListener { _, event ->
                when (event.action) {
                    MotionEvent.ACTION_DOWN -> {
                        lastX = event.rawX.toInt()
                        lastY = event.rawY.toInt()
                        paramX = floatLayoutParams.x
                        paramY = floatLayoutParams.y
                    }

                    MotionEvent.ACTION_MOVE -> {
                        val dx = event.rawX.toInt() - lastX
                        val dy = event.rawY.toInt() - lastY
                        floatLayoutParams.x = paramX + dx
                        floatLayoutParams.y = paramY + dy
                        // 更新悬浮窗位置
                        windowManager?.updateViewLayout(floatView, floatLayoutParams)
                    }
                }
                false
            }
        } catch (e: IllegalStateException) {
            e.printStackTrace()
        } catch (e: WindowManager.BadTokenException) {
            e.printStackTrace()
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        windowManager?.removeView(floatView)
    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        val time = SaveKeyValues.getValue(Constant.TIMEOUT, "15s") as String
        textView.text = time
        return START_STICKY
    }

    override fun handleMessage(msg: Message): Boolean {
        when (msg.what) {
            2024071701 -> {
                val time = msg.obj as Long
                textView.text = "${time}s"
            }

            2024071702 -> {
                val time = msg.obj as String
                textView.text = time
            }

            Constant.SHOW_FLOATING_WINDOW_CODE -> {
                floatView.alpha = 1.0f
            }

            Constant.HIDE_FLOATING_WINDOW_CODE -> {
                floatView.alpha = 0.0f
            }
        }
        return true
    }
}