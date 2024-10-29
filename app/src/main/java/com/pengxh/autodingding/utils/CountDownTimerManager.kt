package com.pengxh.autodingding.utils

import android.content.Context
import android.content.Intent
import android.os.CountDownTimer
import android.util.Log
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleOwner
import androidx.lifecycle.LifecycleRegistry
import androidx.lifecycle.lifecycleScope
import com.pengxh.autodingding.extensions.createTextMail
import com.pengxh.autodingding.extensions.sendTextMail
import com.pengxh.autodingding.service.FloatingWindowService
import com.pengxh.autodingding.ui.MainActivity
import com.pengxh.kt.lite.extensions.show
import com.pengxh.kt.lite.utils.SaveKeyValues
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch


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

    fun startTimer(context: Context, millisInFuture: Long, countDownInterval: Long) {
        Log.d(kTag, "startTimer: 开始倒计时")
        timer = object : CountDownTimer(millisInFuture, countDownInterval) {
            override fun onTick(millisUntilFinished: Long) {
                val tick = millisUntilFinished / 1000
                val handler = FloatingWindowService.weakReferenceHandler ?: return
                val message = handler.obtainMessage()
                message.what = 2024071701
                message.obj = tick
                handler.sendMessage(message)
            }

            override fun onFinish() {
                //如果倒计时结束，那么表明没有收到打卡成功的通知，需要将异常日志保存
                if (SaveKeyValues.getValue(Constant.BACK_TO_HOME, false) as Boolean) {
                    //模拟点击Home键
                    val home = Intent(Intent.ACTION_MAIN)
                    home.flags = Intent.FLAG_ACTIVITY_NEW_TASK
                    home.addCategory(Intent.CATEGORY_HOME)
                    context.startActivity(home)
                    Log.d(kTag, "onFinish: 模拟点击Home键")
                    Thread.sleep(2000)
                }

                val intent = Intent(context, MainActivity::class.java)
                intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK
                context.startActivity(intent)

                val emailAddress = SaveKeyValues.getValue(Constant.EMAIL_ADDRESS, "") as String
                if (emailAddress.isEmpty()) {
                    "邮箱地址为空".show(context)
                    return
                }

                "未监听到打卡通知，即将发送异常日志邮件，请注意查收".show(context)
                lifecycleScope.launch(Dispatchers.IO) {
                    val subject = SaveKeyValues.getValue(
                        Constant.EMAIL_TITLE, "打卡结果通知"
                    ) as String
                    "".createTextMail(subject, emailAddress).sendTextMail()
                }
            }
        }.start()
    }

    fun cancelTimer() {
        timer?.cancel()
        Log.d(kTag, "cancelTimer: 取消超时定时器")
    }
}