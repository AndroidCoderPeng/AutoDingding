package com.pengxh.autodingding.ui

import android.app.Activity
import android.content.ComponentName
import android.content.Context
import android.content.Intent
import android.content.ServiceConnection
import android.media.projection.MediaProjectionManager
import android.os.Bundle
import android.os.IBinder
import android.view.Gravity
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import com.pengxh.autodingding.service.ScreenShotService
import com.pengxh.kt.lite.extensions.createImageFileDir
import com.pengxh.kt.lite.extensions.getSystemService
import com.pengxh.kt.lite.extensions.show
import com.pengxh.kt.lite.utils.ActivityStackManager
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

class OnePixelActivity : AppCompatActivity() {

    private val timeFormat by lazy { SimpleDateFormat("yyyyMMddHHmmss", Locale.CHINA) }
    private var screenShortService: ScreenShotService? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        ActivityStackManager.addActivity(this)

        val window = window
        window.setGravity(Gravity.START or Gravity.TOP)
        val params = window.attributes
        params.x = 0
        params.y = 0
        params.height = 1
        params.width = 1
        window.attributes = params

        val mpm = getSystemService<MediaProjectionManager>()
        val captureIntent = mpm?.createScreenCaptureIntent()
        captureIntentLauncher.launch(captureIntent)
    }

    override fun onStart() {
        super.onStart()
        Intent(this, ScreenShotService::class.java).also { intent ->
            bindService(intent, serviceConnection, Context.BIND_AUTO_CREATE)
        }
    }

    private val serviceConnection = object : ServiceConnection {
        override fun onServiceConnected(name: ComponentName?, iBinder: IBinder?) {
            if (iBinder is ScreenShotService.ServiceBinder) {
                //截屏
                screenShortService = iBinder.getScreenShotService()
            }
        }

        override fun onServiceDisconnected(name: ComponentName?) {
            "截屏服务已断开".show(this@OnePixelActivity)
        }
    }

    private val captureIntentLauncher = registerForActivityResult(
        ActivityResultContracts.StartActivityForResult()
    ) { result ->
        if (result.resultCode == Activity.RESULT_OK) {
            val imagePath = "${createImageFileDir()}/${timeFormat.format(Date())}.png"
            result.data?.let {
                screenShortService?.startCaptureScreen(imagePath, it)
            }
        }
    }

    override fun onStop() {
        super.onStop()
        unbindService(serviceConnection)
    }
}