package com.pengxh.autodingding.fragment

import android.app.AlarmManager
import android.app.PendingIntent
import android.content.Intent
import android.os.Build
import android.os.Bundle
import android.os.CountDownTimer
import android.os.Handler
import android.os.Message
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.lifecycle.ViewModelProvider
import com.pengxh.autodingding.BaseApplication
import com.pengxh.autodingding.R
import com.pengxh.autodingding.adapter.TaskTimeAdapter
import com.pengxh.autodingding.bean.TaskTimeBean
import com.pengxh.autodingding.databinding.FragmentAutoDingdingBinding
import com.pengxh.autodingding.extensions.diffCurrentMillis
import com.pengxh.autodingding.extensions.initImmersionBar
import com.pengxh.autodingding.extensions.isEarlierThenCurrent
import com.pengxh.autodingding.greendao.TaskTimeBeanDao
import com.pengxh.autodingding.utils.DateChangeReceiver
import com.pengxh.autodingding.vm.DateDayViewModel
import com.pengxh.autodingding.widget.BottomSelectTimeSheet
import com.pengxh.kt.lite.base.KotlinBaseFragment
import com.pengxh.kt.lite.divider.RecyclerViewItemOffsets
import com.pengxh.kt.lite.extensions.createLogFile
import com.pengxh.kt.lite.extensions.dp2px
import com.pengxh.kt.lite.extensions.getSystemService
import com.pengxh.kt.lite.extensions.show
import com.pengxh.kt.lite.extensions.timestampToCompleteDate
import com.pengxh.kt.lite.extensions.timestampToDate
import com.pengxh.kt.lite.extensions.writeToFile
import com.pengxh.kt.lite.utils.WeakReferenceHandler
import com.pengxh.kt.lite.widget.dialog.AlertControlDialog
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Date
import java.util.LinkedList
import java.util.Locale
import java.util.Queue
import java.util.Random
import java.util.UUID
import kotlin.math.abs


/**
 * 支持每天上下班半小时随机时间自动打卡
 * */
class AutoDingDingFragment : KotlinBaseFragment<FragmentAutoDingdingBinding>(), Handler.Callback {

    companion object {
        lateinit var weakReferenceHandler: WeakReferenceHandler
    }

    private val kTag = "AutoDingDingFragment"
    private val taskTimeBeanDao by lazy { BaseApplication.get().daoSession.taskTimeBeanDao }
    private val marginOffset by lazy { 10.dp2px(requireContext()) }
    private val format by lazy { SimpleDateFormat("HH:mm", Locale.CHINA) }
    private val random by lazy { Random() }
    private val alarmManager by lazy { requireContext().getSystemService<AlarmManager>() }
    private lateinit var taskTimeAdapter: TaskTimeAdapter
    private lateinit var dateDayViewModel: DateDayViewModel
    private lateinit var taskQueue: Queue<TaskTimeBean>
    private lateinit var pendingIntent: PendingIntent
    private var taskBeans: MutableList<TaskTimeBean> = ArrayList()
    private var clickedPosition = 0
    private var isTaskStarted = false
    private var countDownTimer: CountDownTimer? = null

    override fun handleMessage(msg: Message): Boolean {
        if (msg.what == 2024070801) {
            val completeDate = System.currentTimeMillis().timestampToCompleteDate()
            Log.d(kTag, "handleMessage: $completeDate")
            "handleMessage: $completeDate".writeToFile(requireContext().createLogFile())
            executeTaskByDay()
        }
        return true
    }

    override fun initEvent() {
        binding.executeTaskButton.setOnClickListener {
            if (taskBeans.isEmpty()) {
                "请先添加任务".show(requireContext())
                return@setOnClickListener
            }

            if (isTaskStarted) {
                alarmManager?.cancel(pendingIntent)
                //清空任务队列
                taskQueue.clear()
                //停止仅在进行的任务
                countDownTimer?.cancel()
                //重置任务状态
                isTaskStarted = false
                binding.executeTaskButton.setImageResource(R.drawable.ic_play_fill)
                binding.nextTaskTimeView.text = "--:--"
            } else {
                isTaskStarted = true
                binding.executeTaskButton.setImageResource(R.drawable.ic_stop_fill)

                //集合转队列
                taskQueue = LinkedList(taskBeans)

                val calendar = Calendar.getInstance()
                calendar.timeInMillis = System.currentTimeMillis()
                calendar.set(Calendar.HOUR_OF_DAY, 0)
                calendar.set(Calendar.MINUTE, 0)
                calendar.set(Calendar.SECOND, 0)
                calendar.set(Calendar.MILLISECOND, 0)

                // 如果当前时间已经过了午夜，则闹钟将在第二天的午夜触发
                if (calendar.before(Calendar.getInstance())) {
                    calendar.add(Calendar.DAY_OF_YEAR, 1)
                }

                alarmManager?.setRepeating(
                    AlarmManager.RTC_WAKEUP,
                    calendar.timeInMillis,
                    AlarmManager.INTERVAL_DAY,
                    pendingIntent
                )

                executeTaskByDay()
            }
        }
    }

    /**
     * 按顺序执行每天的任务
     * */
    private fun executeTaskByDay() {
        val task = taskQueue.poll() ?: return
        val taskRealTime = calculateTaskRealTime(task)
        val currentDateTime = "${System.currentTimeMillis().timestampToDate()} $taskRealTime"
        if (currentDateTime.isEarlierThenCurrent()) {
            Log.d(kTag, "${currentDateTime}已过时")
            binding.nextTaskTimeView.text = "今天${taskRealTime}已过"
            executeTaskByDay()
            return
        }
        binding.nextTaskTimeView.text = currentDateTime
        val diffCurrentMillis = currentDateTime.diffCurrentMillis()
        countDownTimer = object : CountDownTimer(diffCurrentMillis, 1000) {
            override fun onTick(millisUntilFinished: Long) {
                Log.d(kTag, "onTick: ${millisUntilFinished / 1000}")
            }

            override fun onFinish() {
                Log.d(kTag, "onFinish: $currentDateTime")
                executeTaskByDay()
            }
        }.start()
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
        val dateFormat = SimpleDateFormat("HH:mm", Locale.CHINA)
        return dateFormat.format(Date(startTime.time + realMinute * 60 * 1000))
    }

    override fun initOnCreate(savedInstanceState: Bundle?) {
        binding.marqueeView.requestFocus()

        weakReferenceHandler = WeakReferenceHandler(this)
        dateDayViewModel = ViewModelProvider(this)[DateDayViewModel::class.java]

        getTaskTimes(false)

        val intent = Intent(requireContext(), DateChangeReceiver::class.java)
        pendingIntent = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
            PendingIntent.getActivity(
                requireContext(), 0, intent, PendingIntent.FLAG_IMMUTABLE
            )
        } else {
            PendingIntent.getActivity(
                requireContext(), 0, intent, PendingIntent.FLAG_UPDATE_CURRENT
            )
        }
    }

    private fun getTaskTimes(isRefresh: Boolean) {
        val queryResult = taskTimeBeanDao.queryBuilder().orderAsc(
            TaskTimeBeanDao.Properties.StartTime
        ).list()

        if (queryResult.size == 0) {
            binding.emptyView.visibility = View.VISIBLE
        } else {
            binding.emptyView.visibility = View.GONE
        }

        if (isRefresh) {
            taskTimeAdapter.setRefreshData(queryResult)
        } else {
            taskBeans = queryResult
            taskTimeAdapter = TaskTimeAdapter(requireContext(), taskBeans)
            binding.recyclerView.adapter = taskTimeAdapter
            binding.recyclerView.addItemDecoration(
                RecyclerViewItemOffsets(
                    marginOffset, marginOffset shr 1, marginOffset, marginOffset shr 1
                )
            )
            taskTimeAdapter.setOnItemClickListener(object : TaskTimeAdapter.OnItemClickListener {
                override fun onAddTaskClick() {
                    if (isTaskStarted) {
                        "任务执行中，无法添加新任务".show(requireContext())
                        return
                    }
                    BottomSelectTimeSheet(
                        requireContext(), object : BottomSelectTimeSheet.OnTimeSelectedCallback {
                            override fun onTimePicked(startTime: String, endTime: String) {
                                //onTimePicked: 22:34 ~ 22:34
                                //保存数据
                                val bean = TaskTimeBean()
                                bean.uuid = UUID.randomUUID().toString()
                                bean.startTime = startTime
                                bean.endTime = endTime

                                taskTimeBeanDao.insert(bean)
                                //刷新列表
                                getTaskTimes(true)
                            }
                        }).show()
                }

                override fun onItemClick(position: Int) {
                    if (isTaskStarted) {
                        "任务执行中，无法修改任务".show(requireContext())
                        return
                    }
                    //修改时间
                    BottomSelectTimeSheet(
                        requireContext(), object : BottomSelectTimeSheet.OnTimeSelectedCallback {
                            override fun onTimePicked(startTime: String, endTime: String) {
                                //onTimePicked: 22:34 ~ 22:34
                                //修改数据
                                val bean = taskBeans[position]
                                bean.startTime = startTime
                                bean.endTime = endTime

                                taskTimeBeanDao.update(bean)
                                //刷新列表
                                getTaskTimes(true)
                            }
                        }).show()
                }

                override fun onItemLongClick(position: Int) {
                    if (isTaskStarted) {
                        "任务执行中，无法删除任务".show(requireContext())
                        return
                    }
                    //标记被点击的item位置
                    clickedPosition = position
                    AlertControlDialog.Builder().setContext(requireContext()).setTitle("删除提示")
                        .setMessage("确定要删除这个任务吗").setNegativeButton("取消")
                        .setPositiveButton("确定").setOnDialogButtonClickListener(object :
                            AlertControlDialog.OnDialogButtonClickListener {
                            override fun onConfirmClick() {
                                deleteTask(taskBeans[position])
                            }

                            override fun onCancelClick() {

                            }
                        }).build().show()
                }
            })
        }
    }

    private fun deleteTask(bean: TaskTimeBean) {
        taskTimeBeanDao.delete(bean)
        taskBeans.removeAt(clickedPosition)
        taskTimeAdapter.notifyItemRemoved(clickedPosition)
        taskTimeAdapter.notifyItemRangeChanged(
            clickedPosition, taskBeans.size - clickedPosition
        )
        if (taskBeans.size == 0) {
            binding.emptyView.visibility = View.VISIBLE
        } else {
            binding.emptyView.visibility = View.GONE
        }
    }

    override fun initViewBinding(
        inflater: LayoutInflater, container: ViewGroup?
    ): FragmentAutoDingdingBinding {
        return FragmentAutoDingdingBinding.inflate(inflater, container, false)
    }

    override fun observeRequestState() {

    }

    override fun setupTopBarLayout() {
        binding.rootView.initImmersionBar(this, true, R.color.white)
    }
}