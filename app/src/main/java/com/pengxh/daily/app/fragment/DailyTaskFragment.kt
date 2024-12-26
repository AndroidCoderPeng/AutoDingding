package com.pengxh.daily.app.fragment

import android.annotation.SuppressLint
import android.content.res.ColorStateList
import android.os.Bundle
import android.os.CountDownTimer
import android.os.Handler
import android.os.Looper
import android.os.Message
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.pengxh.daily.app.BaseApplication
import com.pengxh.daily.app.R
import com.pengxh.daily.app.adapter.DailyTaskAdapter
import com.pengxh.daily.app.bean.DailyTaskBean
import com.pengxh.daily.app.databinding.FragmentDailyTaskBinding
import com.pengxh.daily.app.extensions.backToMainActivity
import com.pengxh.daily.app.extensions.formatTime
import com.pengxh.daily.app.extensions.getTaskIndex
import com.pengxh.daily.app.extensions.openApplication
import com.pengxh.daily.app.extensions.random
import com.pengxh.daily.app.extensions.sendEmail
import com.pengxh.daily.app.extensions.showTimePicker
import com.pengxh.daily.app.greendao.DailyTaskBeanDao
import com.pengxh.daily.app.service.FloatingWindowService
import com.pengxh.daily.app.utils.Constant
import com.pengxh.daily.app.utils.CountDownTimerKit
import com.pengxh.daily.app.utils.MessageEvent
import com.pengxh.daily.app.utils.OnTimeCountDownCallback
import com.pengxh.daily.app.utils.OnTimeSelectedCallback
import com.pengxh.daily.app.utils.TimeKit
import com.pengxh.kt.lite.base.KotlinBaseFragment
import com.pengxh.kt.lite.divider.RecyclerViewItemOffsets
import com.pengxh.kt.lite.extensions.convertColor
import com.pengxh.kt.lite.extensions.dp2px
import com.pengxh.kt.lite.extensions.show
import com.pengxh.kt.lite.utils.SaveKeyValues
import com.pengxh.kt.lite.utils.WeakReferenceHandler
import com.pengxh.kt.lite.widget.dialog.AlertControlDialog
import org.greenrobot.eventbus.EventBus
import org.greenrobot.eventbus.Subscribe
import org.greenrobot.eventbus.ThreadMode
import java.util.UUID
import java.util.concurrent.atomic.AtomicInteger


@SuppressLint("NotifyDataSetChanged", "SetTextI18n")
class DailyTaskFragment : KotlinBaseFragment<FragmentDailyTaskBinding>(), Handler.Callback {

    private val kTag = "DailyTaskFragment"
    private val dailyTaskBeanDao by lazy { BaseApplication.get().daoSession.dailyTaskBeanDao }
    private val marginOffset by lazy { 10.dp2px(requireContext()) }
    private val weakReferenceHandler = WeakReferenceHandler(this)
    private val repeatTaskHandler = Handler(Looper.getMainLooper())
    private val dailyTaskHandler = Handler(Looper.getMainLooper())
    private lateinit var dailyTaskAdapter: DailyTaskAdapter
    private var taskBeans: MutableList<DailyTaskBean> = ArrayList()
    private var diffSeconds = AtomicInteger(0)
    private var isTaskStarted = false
    private var timerKit: CountDownTimerKit? = null
    private var timeoutTimer: CountDownTimer? = null

    override fun setupTopBarLayout() {

    }

    override fun observeRequestState() {

    }

    override fun initViewBinding(
        inflater: LayoutInflater, container: ViewGroup?
    ): FragmentDailyTaskBinding {
        return FragmentDailyTaskBinding.inflate(inflater, container, false)
    }

    override fun initOnCreate(savedInstanceState: Bundle?) {
        taskBeans = dailyTaskBeanDao.queryBuilder().orderAsc(
            DailyTaskBeanDao.Properties.Time
        ).list()

        updateEmptyViewVisibility()

        dailyTaskAdapter = DailyTaskAdapter(requireContext(), taskBeans)
        binding.recyclerView.adapter = dailyTaskAdapter
        binding.recyclerView.addItemDecoration(
            RecyclerViewItemOffsets(
                marginOffset, marginOffset shr 1, marginOffset, marginOffset shr 1
            )
        )
        dailyTaskAdapter.setOnItemClickListener(object :
            DailyTaskAdapter.OnItemClickListener<DailyTaskBean> {
            override fun onItemClick(item: DailyTaskBean, position: Int) {
                if (isTaskStarted) {
                    "任务进行中，无法修改，请先取消当前任务".show(requireContext())
                    return
                }
                AlertControlDialog.Builder().setContext(requireContext()).setTitle("修改打卡任务")
                    .setMessage("是否需要调整打卡时间？").setNegativeButton("取消")
                    .setPositiveButton("确定").setOnDialogButtonClickListener(object :
                        AlertControlDialog.OnDialogButtonClickListener {
                        override fun onConfirmClick() {
                            requireActivity().showTimePicker(item, object : OnTimeSelectedCallback {
                                override fun onTimePicked(time: String) {
                                    item.time = time
                                    dailyTaskBeanDao.update(item)
                                    taskBeans.sortBy { x -> x.time }
                                    dailyTaskAdapter.notifyDataSetChanged()
                                }
                            })
                        }

                        override fun onCancelClick() {

                        }
                    }).build().show()
            }

            override fun onItemLongClick(item: DailyTaskBean, position: Int) {
                if (isTaskStarted) {
                    "任务进行中，无法删除，请先取消当前任务".show(requireContext())
                    return
                }
                AlertControlDialog.Builder().setContext(requireContext()).setTitle("删除提示")
                    .setMessage("确定要删除这个任务吗").setNegativeButton("取消")
                    .setPositiveButton("确定").setOnDialogButtonClickListener(object :
                        AlertControlDialog.OnDialogButtonClickListener {
                        override fun onConfirmClick() {
                            dailyTaskBeanDao.delete(item)
                            if (taskBeans.remove(item)) {
                                dailyTaskAdapter.notifyDataSetChanged()
                                updateEmptyViewVisibility()
                            } else {
                                "任务已不在列表中".show(requireContext())
                            }
                        }

                        override fun onCancelClick() {

                        }
                    }).build().show()
            }
        })
    }

    private fun updateEmptyViewVisibility() {
        binding.emptyView.visibility = if (taskBeans.isEmpty()) View.VISIBLE else View.GONE
    }

    override fun initEvent() {
        binding.executeTaskButton.setOnClickListener {
            if (dailyTaskBeanDao.loadAll().isEmpty()) {
                "请先添加任务时间点".show(requireContext())
                return@setOnClickListener
            }

            if (!isTaskStarted) {
                //计算当前时间距离0点的时间差
                diffSeconds.set(TimeKit.getNextMidnightSeconds())
                repeatTaskHandler.post(repeatTaskRunnable)
                Log.d(kTag, "initEvent: 开启周期任务Runnable")
                executeDailyTask()
                isTaskStarted = true
                binding.executeTaskButton.setImageResource(R.mipmap.ic_stop)
            } else {
                repeatTaskHandler.removeCallbacks(repeatTaskRunnable)
                Log.d(kTag, "initEvent: 取消周期任务Runnable")
                timerKit?.cancel()
                isTaskStarted = false
                binding.actualTimeView.text = "--:--:--"
                binding.repeatTimeView.text = "0秒后刷新每日任务"
                binding.repeatTimeView.visibility = View.INVISIBLE
                binding.executeTaskButton.setImageResource(R.mipmap.ic_start)
                binding.taskDayView.backgroundTintList = ColorStateList.valueOf(
                    R.color.lib_hint_color.convertColor(requireContext())
                )
                binding.taskDayTextView.text = "未执行"
                binding.tipsView.text = ""
                binding.countDownTimeView.text = "0秒后执行任务"
                binding.countDownPgr.progress = 0
                dailyTaskAdapter.updateCurrentTaskState(-1)
            }
        }

        binding.addTimerButton.setOnClickListener {
            if (isTaskStarted) {
                "任务进行中，无法添加，请先取消当前任务".show(requireContext())
                return@setOnClickListener
            }
            requireActivity().showTimePicker(object : OnTimeSelectedCallback {
                override fun onTimePicked(time: String) {
                    val bean = DailyTaskBean()
                    bean.uuid = UUID.randomUUID().toString()
                    bean.time = time

                    val count = dailyTaskBeanDao.queryBuilder().where(
                        DailyTaskBeanDao.Properties.Time.eq(time)
                    ).count()
                    if (count > 1) {
                        "任务时间点已存在".show(requireContext())
                        return
                    }

                    dailyTaskBeanDao.insert(bean)
                    taskBeans.add(bean)
                    taskBeans.sortBy { x -> x.time }
                    dailyTaskAdapter.notifyDataSetChanged()

                    binding.emptyView.visibility = View.GONE
                }
            })
        }
    }

    /**
     * 循环任务Runnable
     * */
    private val repeatTaskRunnable = object : Runnable {
        override fun run() {
            val currentDiffSeconds = diffSeconds.decrementAndGet()
            if (currentDiffSeconds > 0) {
                val activity = requireActivity()
                if (!activity.isFinishing && !activity.isDestroyed) {
                    activity.runOnUiThread {
                        binding.repeatTimeView.visibility = View.VISIBLE
                        binding.repeatTimeView.text =
                            "${currentDiffSeconds.formatTime()}后刷新每日任务"
                    }
                }
                repeatTaskHandler.postDelayed(this, 1000)
            } else {
                //零点，刷新任务，并重启repeatTaskRunnable
                diffSeconds.set(TimeKit.getNextMidnightSeconds())
                repeatTaskHandler.post(this)
                Log.d(kTag, "run: 零点，刷新任务，并重新执行repeatTaskRunnable")
                executeDailyTask()
            }
        }
    }

    private fun executeDailyTask() {
        if (TimeKit.todayIsHoliday(requireContext())) {
            binding.taskDayView.backgroundTintList = ColorStateList.valueOf(
                R.color.iOSGreen.convertColor(requireContext())
            )
            binding.taskDayTextView.text = "休息日"
        } else {
            binding.taskDayView.backgroundTintList = ColorStateList.valueOf(
                R.color.colorAppThemeLight.convertColor(requireContext())
            )
            binding.taskDayTextView.text = "工作日"
        }
        Log.d(kTag, "executeDailyTask: 执行周期任务")
        dailyTaskHandler.post(dailyTaskRunnable)
    }

    /**
     * 当日串行任务Runnable
     * */
    private val dailyTaskRunnable = Runnable {
        val taskIndex = taskBeans.getTaskIndex()
        Log.d(kTag, "任务index是: $taskIndex")
        if (taskIndex == -1) {
            weakReferenceHandler.sendEmptyMessage(Constant.COMPLETED_ALL_TASK_CODE)
        } else {
            val message = weakReferenceHandler.obtainMessage()
            message.what = Constant.START_TASK_CODE
            message.obj = taskIndex
            weakReferenceHandler.sendMessage(message)
        }
    }

    override fun handleMessage(msg: Message): Boolean {
        when (msg.what) {
            Constant.START_TASK_CODE -> {
                val index = msg.obj as Int
                val task = taskBeans[index]
                binding.tipsView.text = "即将执行第 ${index + 1} 个任务"
                binding.tipsView.setTextColor(R.color.colorAppThemeLight.convertColor(requireContext()))

                val pair = task.random()
                dailyTaskAdapter.updateCurrentTaskState(index, pair.first)
                binding.actualTimeView.text = pair.first
                val diff = pair.second
                binding.countDownPgr.max = diff
                timerKit = CountDownTimerKit(diff, object : OnTimeCountDownCallback {
                    override fun updateCountDownSeconds(seconds: Int) {
                        binding.countDownTimeView.text = "${seconds.formatTime()}后执行任务"
                        binding.countDownPgr.progress = diff - seconds
                    }

                    override fun onFinish() {
                        val isSkipHoliday = SaveKeyValues.getValue(
                            Constant.SKIP_HOLIDAY_KEY, true
                        ) as Boolean
                        if (isSkipHoliday) {
                            if (TimeKit.todayIsHoliday(requireContext())) {
                                //休息
                                "今天休息哦~，已经帮你跳过打卡任务".sendEmail(
                                    requireContext(), "放假通知", false
                                )
                                dailyTaskHandler.post(dailyTaskRunnable)
                            } else {
                                binding.countDownTimeView.text = "0秒后执行任务"
                                binding.countDownPgr.progress = 0
                                requireContext().openApplication(Constant.DING_DING, true)
                            }
                        } else {
                            binding.countDownTimeView.text = "0秒后执行任务"
                            binding.countDownPgr.progress = 0
                            requireContext().openApplication(Constant.DING_DING, true)
                        }
                    }
                })
                timerKit?.start()
            }

            Constant.EXECUTE_NEXT_TASK_CODE -> {
                dailyTaskHandler.post(dailyTaskRunnable)
            }

            Constant.COMPLETED_ALL_TASK_CODE -> {
                binding.tipsView.text = "当天所有任务已执行完毕"
                binding.tipsView.setTextColor(R.color.iOSGreen.convertColor(requireContext()))
                dailyTaskAdapter.updateCurrentTaskState(-1)
                dailyTaskHandler.removeCallbacks(dailyTaskRunnable)
            }
        }
        return true
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        Log.d(kTag, "onCreate: 注册EventBus")
        EventBus.getDefault().register(this)
    }

    @Subscribe(threadMode = ThreadMode.MAIN)
    fun onMessageEvent(event: MessageEvent) {
        when (event.code) {
            Constant.START_COUNT_DOWN_TIMER_CODE -> {
                Log.d(kTag, "onMessageEvent: 开始超时倒计时")
                val time = SaveKeyValues.getValue(Constant.STAY_DD_TIMEOUT_KEY, "45s") as String
                //去掉时间的s
                val timeValue = time.dropLast(1).toInt()
                timeoutTimer = object : CountDownTimer(timeValue * 1000L, 1000) {
                    override fun onTick(millisUntilFinished: Long) {
                        val tick = millisUntilFinished / 1000
                        FloatingWindowService.weakReferenceHandler?.apply {
                            val message = obtainMessage()
                            message.what = Constant.TICK_TIME_CODE
                            message.obj = tick
                            sendMessage(message)
                        }
                    }

                    override fun onFinish() {
                        //如果倒计时结束，那么表明没有收到打卡成功的通知
                        requireContext().backToMainActivity()
                        "未监听到打卡通知，即将发送异常日志邮件，请注意查收".show(requireContext())
                        "".sendEmail(requireContext(), null, false)
                    }
                }
                timeoutTimer?.start()
            }

            Constant.CANCEL_COUNT_DOWN_TIMER_CODE -> {
                timeoutTimer?.cancel()
                timeoutTimer = null
                Log.d(kTag, "onMessageEvent: 取消超时定时器，执行下一个任务")
                weakReferenceHandler.sendEmptyMessage(Constant.EXECUTE_NEXT_TASK_CODE)
            }
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        EventBus.getDefault().unregister(this)
        Log.d(kTag, "onDestroyView: 解注册EventBus")
    }
}