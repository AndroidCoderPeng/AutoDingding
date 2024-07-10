package com.pengxh.autodingding.utils

import android.content.Context
import android.content.Intent
import android.os.CountDownTimer
import android.util.Log
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleOwner
import androidx.lifecycle.LifecycleRegistry
import androidx.lifecycle.lifecycleScope
import com.pengxh.autodingding.extensions.createAttachFileMail
import com.pengxh.autodingding.extensions.sendAttachFileMail
import com.pengxh.autodingding.ui.MainActivity
import com.pengxh.kt.lite.extensions.show
import com.pengxh.kt.lite.extensions.timestampToCompleteDate
import com.pengxh.kt.lite.utils.ActivityStackManager
import com.pengxh.kt.lite.utils.SaveKeyValues
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

class CountDownTimerManager private constructor() : LifecycleOwner {

    private val kTag = "CountDownTimerManager"
    private val registry = LifecycleRegistry(this)

    override fun getLifecycle(): Lifecycle {
        return registry
    }

    companion object {
        val get by lazy(mode = LazyThreadSafetyMode.SYNCHRONIZED) {
            CountDownTimerManager()
        }
    }

    private var timer: CountDownTimer? = null

    fun startTimer(
        context: Context, millisInFuture: Long, countDownInterval: Long, imagePath: String
    ) {
        Log.d(kTag, "startTimer: 开始倒计时")
        timer = object : CountDownTimer(millisInFuture, countDownInterval) {
            override fun onTick(millisUntilFinished: Long) {
                Log.d(kTag, "onTick: ${millisUntilFinished / 1000}")
            }

            override fun onFinish() {
                ActivityStackManager.finishAllActivity()
                val intent = Intent(context, MainActivity::class.java)
                intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK
                context.startActivity(intent)

                val emailAddress = SaveKeyValues.getValue(Constant.EMAIL_ADDRESS, "") as String
                if (emailAddress.isEmpty()) {
                    "邮箱地址为空".show(context)
                    return
                }

                //如果倒计时结束，那么表明没有收到打卡成功的通知，需要将异常日志保存
                lifecycleScope.launch(Dispatchers.Main) {
                    "发送打卡结果邮件，请注意查收".show(context)
                    withContext(Dispatchers.IO) {
                        "打卡结果如附件，${System.currentTimeMillis().timestampToCompleteDate()}"
                            .createAttachFileMail(emailAddress, imagePath).sendAttachFileMail()
                    }
                }
            }
        }.start()
    }

    fun cancelTimer() {
        timer?.cancel()
        Log.d(kTag, "cancelTimer: 取消超时定时器")
    }
}