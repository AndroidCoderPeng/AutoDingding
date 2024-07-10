package com.pengxh.autodingding.service

import android.annotation.SuppressLint
import android.app.Activity
import android.app.Notification
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.Service
import android.content.Intent
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.graphics.PixelFormat
import android.hardware.display.DisplayManager
import android.media.ImageReader
import android.media.projection.MediaProjectionManager
import android.os.Binder
import android.os.Build
import android.os.IBinder
import android.util.Log
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleOwner
import androidx.lifecycle.LifecycleRegistry
import androidx.lifecycle.lifecycleScope
import com.pengxh.autodingding.R
import com.pengxh.autodingding.extensions.createTextMail
import com.pengxh.autodingding.extensions.sendTextMail
import com.pengxh.autodingding.utils.Constant
import com.pengxh.autodingding.utils.CountDownTimerManager
import com.pengxh.kt.lite.extensions.getSystemService
import com.pengxh.kt.lite.extensions.saveImage
import com.pengxh.kt.lite.extensions.show
import com.pengxh.kt.lite.extensions.timestampToCompleteDate
import com.pengxh.kt.lite.utils.SaveKeyValues
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext


class ScreenShotService : Service(), LifecycleOwner {

    private val kTag = "ScreenShortRecordService"
    private val mpm by lazy { getSystemService<MediaProjectionManager>() }

    private val registry = LifecycleRegistry(this)

    override fun getLifecycle(): Lifecycle {
        return registry
    }

    inner class ServiceBinder : Binder() {
        fun getScreenShotService(): ScreenShotService {
            return this@ScreenShotService
        }
    }

    override fun onBind(intent: Intent?): IBinder {
        return ServiceBinder()
    }

    @SuppressLint("WrongConstant")
    fun startCaptureScreen(imagePath: String, intent: Intent) {
        lifecycleScope.launch(Dispatchers.Main) {
            val emailAddress = SaveKeyValues.getValue(Constant.EMAIL_ADDRESS, "") as String
            if (emailAddress.isEmpty()) {
                Log.d(kTag, "startCaptureScreen: emailAddress is empty")
                return@launch
            }
            Log.d(kTag, "startCaptureScreen: 开始截屏 $imagePath")
            //开启通知，并申请成为前台服务
            createForegroundNotification()

            val dm = resources.displayMetrics
            //获得令牌
            val mpj = mpm?.getMediaProjection(Activity.RESULT_OK, intent)
            if (mpj == null) {
                withContext(Dispatchers.IO) {
                    "打卡截屏失败，请手动确认打卡结果。时间：${
                        System.currentTimeMillis().timestampToCompleteDate()
                    }".createTextMail(emailAddress).sendTextMail()
                }
                return@launch
            }
            val imageReader = ImageReader.newInstance(
                dm.widthPixels, dm.heightPixels, PixelFormat.RGBA_8888, 1
            )
            mpj.createVirtualDisplay(
                "CaptureScreen",
                dm.widthPixels, dm.heightPixels, dm.densityDpi,
                DisplayManager.VIRTUAL_DISPLAY_FLAG_AUTO_MIRROR,
                imageReader.surface, null, null
            )
            //必须延迟一下，因为生出图片需要时间缓冲，不能秒得
            delay(1000)
            withContext(Dispatchers.IO) {
                val image = imageReader.acquireNextImage()
                if (image == null) {
                    "打卡截屏失败，请手动确认打卡结果。时间：${
                        System.currentTimeMillis().timestampToCompleteDate()
                    }".createTextMail(emailAddress).sendTextMail()
                    return@withContext
                }
                val width = image.width
                val height = image.height
                val planes = image.planes
                val buffer = planes[0].buffer
                val pixelStride = planes[0].pixelStride
                val rowStride = planes[0].rowStride
                val rowPadding = rowStride - pixelStride * width
                val bitmap = Bitmap.createBitmap(
                    width + rowPadding / pixelStride, height, Bitmap.Config.ARGB_8888
                )
                bitmap.copyPixelsFromBuffer(buffer)
                image.close()
                mpj.stop()
                Log.d(kTag, "startCaptureScreen: 完成截屏")
                bitmap.saveImage(imagePath)
                withContext(Dispatchers.Main) {
                    /***倒计时开始，记录在钉钉界面停留的时间，超过设定的超时时间，自动回到打卡工具，并记录异常日志********/
                    val time = SaveKeyValues.getValue(Constant.TIMEOUT, "15s") as String
                    "完成截屏，${time}后回到软件".show(this@ScreenShotService)
                    //去掉时间的s
                    val timeValue = time.dropLast(1).toInt()
                    CountDownTimerManager.get.startTimer(
                        this@ScreenShotService, timeValue * 1000L, 1000, imagePath
                    )
                }
            }
        }
    }

    private fun createForegroundNotification() {
        val notificationManager = getSystemService<NotificationManager>()
        val builder: Notification.Builder
        val name = resources.getString(R.string.app_name)
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            //创建渠道
            val id = "${kTag}Channel"
            val channel = NotificationChannel(id, name, NotificationManager.IMPORTANCE_HIGH)
            channel.setShowBadge(true)
            channel.enableVibration(false)
            channel.lockscreenVisibility = Notification.VISIBILITY_PUBLIC //设置锁屏可见
            notificationManager?.createNotificationChannel(channel)
            builder = Notification.Builder(this, id)
        } else {
            builder = Notification.Builder(this)
        }
        val bitmap = BitmapFactory.decodeResource(resources, R.mipmap.logo_round)
        builder.setContentTitle(name)
            .setContentText("${name}屏幕截取中")
            .setWhen(System.currentTimeMillis())
            .setSmallIcon(R.mipmap.logo_round)
            .setLargeIcon(bitmap)
        val notification = builder.build()
        notification.flags = Notification.FLAG_NO_CLEAR
        startForeground(Int.MAX_VALUE, notification)
    }
}