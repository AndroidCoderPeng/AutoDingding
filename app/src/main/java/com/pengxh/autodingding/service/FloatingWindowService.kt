package com.pengxh.autodingding.service

import android.annotation.SuppressLint
import android.app.ActivityManager
import android.app.Service
import android.content.Intent
import android.graphics.PixelFormat
import android.os.Build
import android.os.Handler
import android.os.IBinder
import android.os.Message
import android.util.Log
import android.view.LayoutInflater
import android.view.MotionEvent
import android.view.WindowManager
import android.widget.TextView
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleOwner
import androidx.lifecycle.LifecycleRegistry
import androidx.lifecycle.lifecycleScope
import com.pengxh.autodingding.R
import com.pengxh.autodingding.extensions.createTextMail
import com.pengxh.autodingding.extensions.sendTextMail
import com.pengxh.autodingding.utils.Constant
import com.pengxh.kt.lite.extensions.formatFileSize
import com.pengxh.kt.lite.extensions.getSystemService
import com.pengxh.kt.lite.utils.SaveKeyValues
import com.pengxh.kt.lite.utils.WeakReferenceHandler
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch


class FloatingWindowService : Service(), LifecycleOwner, Handler.Callback {

    companion object {
        var weakReferenceHandler: WeakReferenceHandler? = null
    }

    private val kTag = "FloatingWindowService"
    private val registry = LifecycleRegistry(this)

    override fun getLifecycle(): Lifecycle {
        return registry
    }

    private val windowManager by lazy { getSystemService<WindowManager>() }
    private val activityManager by lazy { getSystemService<ActivityManager>() }
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

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        val time = SaveKeyValues.getValue(Constant.TIMEOUT, "15s") as String
        textView.text = time

        val memoryInfo = ActivityManager.MemoryInfo()
        activityManager?.getMemoryInfo(memoryInfo)
        if (memoryInfo.availMem < memoryInfo.threshold) {
            val emailAddress = SaveKeyValues.getValue(Constant.EMAIL_ADDRESS, "") as String
            if (emailAddress.isNotEmpty()) {
                //应用已被杀死
                lifecycleScope.launch(Dispatchers.IO) {
                    val app = resources.getString(R.string.app_name)
                    "内存已满，${app}应用可能已被系统杀死，请提前手动打卡".createTextMail(
                        "${app}状态异常通知", emailAddress
                    ).sendTextMail()
                }
            }
        } else {
            Log.d(kTag, "可用内存: ${memoryInfo.availMem.formatFileSize()}")
        }
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
        }
        return true
    }
}