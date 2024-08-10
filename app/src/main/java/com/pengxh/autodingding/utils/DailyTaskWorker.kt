package com.pengxh.autodingding.utils

import android.content.Context
import android.os.CountDownTimer
import android.util.Log
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleOwner
import androidx.lifecycle.LifecycleRegistry
import androidx.lifecycle.lifecycleScope
import androidx.work.Worker
import androidx.work.WorkerParameters
import com.pengxh.autodingding.BaseApplication
import com.pengxh.autodingding.bean.TaskTimeBean
import com.pengxh.autodingding.extensions.diffCurrentMillis
import com.pengxh.autodingding.extensions.isEarlierThenCurrent
import com.pengxh.autodingding.fragment.AutoDingDingFragment
import com.pengxh.autodingding.greendao.TaskTimeBeanDao
import com.pengxh.kt.lite.extensions.timestampToDate
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.Date
import java.util.LinkedList
import java.util.Locale
import java.util.Queue
import java.util.Random
import kotlin.math.abs

class DailyTaskWorker(context: Context, workerParams: WorkerParameters) :
    Worker(context, workerParams), LifecycleOwner {

    private val kTag = "DailyTaskWorker"
    private val taskTimeBeanDao by lazy { BaseApplication.get().daoSession.taskTimeBeanDao }
    private val format by lazy { SimpleDateFormat("HH:mm", Locale.CHINA) }
    private val random by lazy { Random() }
    private val registry = LifecycleRegistry(this)
    private lateinit var taskQueue: Queue<TaskTimeBean>
    private var countDownTimer: CountDownTimer? = null

    override fun getLifecycle(): Lifecycle {
        return registry
    }

    override fun doWork(): Result {
        taskQueue = LinkedList(
            taskTimeBeanDao.queryBuilder().orderAsc(TaskTimeBeanDao.Properties.StartTime).list()
        )

        //执行任务
        executeTaskByDay()
        return Result.success()
    }

    private fun executeTaskByDay() {
        val task = taskQueue.poll() ?: return
        val taskRealTime = calculateTaskRealTime(task)
        val currentDateTime = "${System.currentTimeMillis().timestampToDate()} $taskRealTime"
        if (currentDateTime.isEarlierThenCurrent()) {
            Log.d(kTag, "${currentDateTime}已过时")
            executeTaskByDay()
            return
        }

        //消息传递
        val handler = AutoDingDingFragment.weakReferenceHandler ?: return
        val message = handler.obtainMessage()
        message.what = 2024070801
        message.obj = currentDateTime
        handler.sendMessage(message)

        val diffCurrentMillis = currentDateTime.diffCurrentMillis()
        lifecycleScope.launch(Dispatchers.Main) {
            object : CountDownTimer(diffCurrentMillis, 1000) {
                override fun onTick(millisUntilFinished: Long) {
                    val msg = handler.obtainMessage()
                    msg.what = 2024070802
                    msg.obj = "${millisUntilFinished / 1000}"
                    handler.sendMessage(msg)
                }

                override fun onFinish() {
                    Log.d(kTag, "onFinish: $currentDateTime")
                    taskQueue = LinkedList(
                        taskTimeBeanDao.queryBuilder().orderAsc(
                            TaskTimeBeanDao.Properties.StartTime
                        ).list()
                    )
                    executeTaskByDay()
                }
            }.start()
        }
    }

    /**
     * 在任务时间区间内随机生成一个任务时间
     * */
    private fun calculateTaskRealTime(bean: TaskTimeBean): String {
        val startTime = format.parse(bean.startTime)!!
        val endTime = format.parse(bean.endTime)!!

        val diff = abs(endTime.time - startTime.time)
        val interval = (diff / (60 * 1000)).toInt()

        //计算任务真实分钟
        val realMinute = random.nextInt(interval)

        //将开始时间偏移计算出来的任务真实分钟
        val dateFormat = SimpleDateFormat("HH:mm:ss", Locale.CHINA)
        return dateFormat.format(Date(startTime.time + realMinute * 60 * 1000 + (0 until 60).random() * 1000))
    }
}