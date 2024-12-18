package com.pengxh.autodingding.fragment

import android.annotation.SuppressLint
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.os.Message
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.pengxh.autodingding.BaseApplication
import com.pengxh.autodingding.R
import com.pengxh.autodingding.adapter.DailyTaskAdapter
import com.pengxh.autodingding.bean.DailyTaskBean
import com.pengxh.autodingding.databinding.FragmentDailyTaskBinding
import com.pengxh.autodingding.extensions.formatTime
import com.pengxh.autodingding.extensions.getTaskIndex
import com.pengxh.autodingding.extensions.isLateThenCurrent
import com.pengxh.autodingding.extensions.openApplication
import com.pengxh.autodingding.extensions.random
import com.pengxh.autodingding.extensions.showTimePicker
import com.pengxh.autodingding.greendao.DailyTaskBeanDao
import com.pengxh.autodingding.utils.Constant
import com.pengxh.autodingding.utils.CountDownTimerKit
import com.pengxh.autodingding.utils.OnTimeCountDownCallback
import com.pengxh.autodingding.utils.OnTimeSelectedCallback
import com.pengxh.autodingding.utils.TimeKit
import com.pengxh.kt.lite.base.KotlinBaseFragment
import com.pengxh.kt.lite.divider.RecyclerViewItemOffsets
import com.pengxh.kt.lite.extensions.convertColor
import com.pengxh.kt.lite.extensions.createLogFile
import com.pengxh.kt.lite.extensions.dp2px
import com.pengxh.kt.lite.extensions.show
import com.pengxh.kt.lite.extensions.toJson
import com.pengxh.kt.lite.extensions.writeToFile
import com.pengxh.kt.lite.utils.WeakReferenceHandler
import com.pengxh.kt.lite.widget.dialog.AlertControlDialog
import java.util.UUID
import java.util.concurrent.atomic.AtomicInteger

@SuppressLint("SetTextI18n")
class DailyTaskFragment : KotlinBaseFragment<FragmentDailyTaskBinding>(), Handler.Callback {

    companion object {
        var weakReferenceHandler: WeakReferenceHandler? = null
    }

    private val kTag = "DailyTaskFragment"
    private val dailyTaskBeanDao by lazy { BaseApplication.get().daoSession.dailyTaskBeanDao }
    private val marginOffset by lazy { 10.dp2px(requireContext()) }
    private val repeatTaskHandler = Handler(Looper.getMainLooper())
    private val dailyTaskHandler = Handler(Looper.getMainLooper())
    private lateinit var dailyTaskAdapter: DailyTaskAdapter
    private var taskBeans: MutableList<DailyTaskBean> = ArrayList()
    private var diffSeconds = AtomicInteger(0)
    private var repeatTimes = AtomicInteger(0)
    private var isTaskStarted = false
    private var timerKit: CountDownTimerKit? = null

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
        weakReferenceHandler = WeakReferenceHandler(this)
        taskBeans = dailyTaskBeanDao.queryBuilder().orderAsc(
            DailyTaskBeanDao.Properties.Time
        ).list()

        if (taskBeans.size == 0) {
            binding.emptyView.visibility = View.VISIBLE
        } else {
            binding.emptyView.visibility = View.GONE
        }

        dailyTaskAdapter = DailyTaskAdapter(requireContext(), taskBeans)
        binding.recyclerView.adapter = dailyTaskAdapter
        binding.recyclerView.addItemDecoration(
            RecyclerViewItemOffsets(
                marginOffset, marginOffset shr 1, marginOffset, marginOffset shr 1
            )
        )
        dailyTaskAdapter.setOnItemClickListener(object : DailyTaskAdapter.OnItemClickListener {
            override fun onItemClick(position: Int) {
                if (isTaskStarted) {
                    "任务进行中，无法修改，请先取消当前任务".show(requireContext())
                    return
                }
                AlertControlDialog.Builder().setContext(requireContext()).setTitle("修改打卡任务")
                    .setMessage("是否需要调整打卡时间？").setNegativeButton("取消")
                    .setPositiveButton("确定").setOnDialogButtonClickListener(object :
                        AlertControlDialog.OnDialogButtonClickListener {
                        override fun onConfirmClick() {
                            val taskBean = taskBeans[position]
                            requireActivity().showTimePicker(
                                taskBean, object : OnTimeSelectedCallback {
                                    override fun onTimePicked(time: String) {
                                        taskBean.time = time
                                        dailyTaskBeanDao.update(taskBean)

                                        // 移除旧位置的任务
                                        taskBeans.removeAt(position)
                                        dailyTaskAdapter.notifyItemRemoved(position)

                                        // 使用二分查找找到合适的位置插入更新后的任务
                                        val newIndex = taskBeans.binarySearch { it.time.compareTo(taskBean.time) }
                                        if (newIndex < 0) {
                                            val insertionIndex = -(newIndex + 1)
                                            taskBeans.add(insertionIndex, taskBean)
                                            dailyTaskAdapter.notifyItemInserted(insertionIndex)
                                        } else {
                                            taskBeans.add(newIndex, taskBean)
                                            dailyTaskAdapter.notifyItemInserted(newIndex)
                                        }
                                    }
                                })
                        }

                        override fun onCancelClick() {

                        }
                    }).build().show()
            }

            override fun onItemLongClick(position: Int) {
                if (isTaskStarted) {
                    "任务进行中，无法删除，请先取消当前任务".show(requireContext())
                    return
                }
                AlertControlDialog.Builder().setContext(requireContext()).setTitle("删除提示")
                    .setMessage("确定要删除这个任务吗").setNegativeButton("取消")
                    .setPositiveButton("确定").setOnDialogButtonClickListener(object :
                        AlertControlDialog.OnDialogButtonClickListener {
                        override fun onConfirmClick() {
                            dailyTaskBeanDao.delete(taskBeans[position])
                            taskBeans.removeAt(position)
                            dailyTaskAdapter.notifyItemRemoved(position)
                            if (taskBeans.size == 0) {
                                binding.emptyView.visibility = View.VISIBLE
                            } else {
                                binding.emptyView.visibility = View.GONE
                            }
                        }

                        override fun onCancelClick() {

                        }
                    }).build().show()
            }
        })
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
                isTaskStarted = true
                binding.executeTaskButton.setImageResource(R.drawable.ic_stop)
            } else {
                repeatTaskHandler.removeCallbacks(repeatTaskRunnable)
                Log.d(kTag, "initEvent: 取消周期任务Runnable")
                timerKit?.cancel()
                isTaskStarted = false
                repeatTimes.set(0)
                binding.actualTimeView.text = "--:--:--"
                binding.repeatTimeView.text = "0秒后刷新每日任务"
                binding.repeatTimeView.visibility = View.INVISIBLE
                binding.executeTaskButton.setImageResource(R.drawable.ic_start)
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

                    // 使用二分查找找到合适的位置插入新任务
                    val index = taskBeans.binarySearch { it.time.compareTo(bean.time) }
                    if (index < 0) {
                        val insertionIndex = -(index + 1)
                        taskBeans.add(insertionIndex, bean)
                        dailyTaskAdapter.notifyItemInserted(insertionIndex)
                    } else {
                        taskBeans.add(index, bean)
                        dailyTaskAdapter.notifyItemInserted(index)
                    }

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
                repeatTimes.set(0)
                diffSeconds.set(TimeKit.getNextMidnightSeconds())
                repeatTaskHandler.post(this)
                Log.d(kTag, "run: 零点，刷新任务，并重启repeatTaskRunnable")
                "${TimeKit.getCurrentTime()}: 零点，刷新任务，并重启repeatTaskRunnable".writeToFile(
                    requireContext().createLogFile()
                )
            }

            if (repeatTimes.get() == 0) {
                updateDailyTask()
            }
        }
    }

    private fun updateDailyTask() {
        Log.d(kTag, "updateDailyTask: 执行周期任务")
        "${TimeKit.getCurrentTime()}：执行周期任务".writeToFile(requireContext().createLogFile())
        dailyTaskHandler.post(dailyTaskRunnable)
        repeatTimes.incrementAndGet()
    }

    /**
     * 当日串行任务Runnable
     * */
    private val dailyTaskRunnable = object : Runnable {
        override fun run() {
            val taskIndex = taskBeans.getTaskIndex()
            val handler = weakReferenceHandler ?: run {
                "${TimeKit.getCurrentTime()}：Handler is null, cannot send message".writeToFile(
                    requireContext().createLogFile()
                )
                return
            }
            synchronized(taskBeans) {
                //如果只有一个任务，直接执行，不用考虑顺序
                if (taskBeans.count() == 1) {
                    val message = handler.obtainMessage()
                    message.what = Constant.EXECUTE_ONLY_ONE_TASK_CODE
                    message.obj = taskIndex
                    handler.sendMessage(message)
                } else {
                    if (taskIndex == -1) {
                        handler.sendEmptyMessage(Constant.COMPLETED_ALL_TASK_CODE)
                    } else {
                        val message = handler.obtainMessage()
                        message.what = Constant.EXECUTE_MULTIPLE_TASK_CODE
                        message.obj = taskIndex
                        handler.sendMessage(message)
                    }
                }
            }
        }
    }

    override fun handleMessage(msg: Message): Boolean {
        when (msg.what) {
            Constant.EXECUTE_ONLY_ONE_TASK_CODE -> {
                val task = taskBeans.first()
                if (task.isLateThenCurrent()) {
                    "${TimeKit.getCurrentTime()}：只有 1 个任务: ${task.toJson()}，直接按时执行".writeToFile(
                        requireContext().createLogFile()
                    )
                    binding.tipsView.text = "只有 1 个任务"
                    binding.tipsView.setTextColor(
                        R.color.colorAppThemeLight.convertColor(requireContext())
                    )

                    val pair = task.random()
                    dailyTaskAdapter.updateCurrentTaskState(0, pair.first)
                    binding.actualTimeView.text = pair.first
                    val diff = pair.second
                    binding.countDownPgr.max = diff
                    timerKit = CountDownTimerKit(diff, object : OnTimeCountDownCallback {
                        override fun updateCountDownSeconds(remainingSeconds: Int) {
                            binding.countDownTimeView.text =
                                "${remainingSeconds.formatTime()}后执行任务"
                            binding.countDownPgr.progress = diff - remainingSeconds
                        }

                        override fun onFinish() {
                            "${TimeKit.getCurrentTime()}：执行任务".writeToFile(requireContext().createLogFile())
                            binding.countDownTimeView.text = "0秒后执行任务"
                            binding.countDownPgr.progress = 0
                            dailyTaskAdapter.updateCurrentTaskState(-1)
                            requireContext().openApplication(Constant.DING_DING)
                        }
                    })
                    timerKit?.start()
                } else {
                    weakReferenceHandler?.sendEmptyMessage(Constant.COMPLETED_ALL_TASK_CODE)
                }
            }

            Constant.EXECUTE_MULTIPLE_TASK_CODE -> {
                val index = msg.obj as Int
                val task = taskBeans[index]
                "${TimeKit.getCurrentTime()}：即将执行第 ${index + 1} 个任务: ${task.toJson()}".writeToFile(
                    requireContext().createLogFile()
                )
                binding.tipsView.text = "即将执行第 ${index + 1} 个任务"
                binding.tipsView.setTextColor(R.color.colorAppThemeLight.convertColor(requireContext()))

                val pair = task.random()
                dailyTaskAdapter.updateCurrentTaskState(index, pair.first)
                binding.actualTimeView.text = pair.first
                val diff = pair.second
                binding.countDownPgr.max = diff
                timerKit = CountDownTimerKit(diff, object : OnTimeCountDownCallback {
                    override fun updateCountDownSeconds(remainingSeconds: Int) {
                        binding.countDownTimeView.text =
                            "${remainingSeconds.formatTime()}后执行任务"
                        binding.countDownPgr.progress = diff - remainingSeconds
                    }

                    override fun onFinish() {
                        "${TimeKit.getCurrentTime()}：执行任务".writeToFile(requireContext().createLogFile())
                        binding.countDownTimeView.text = "0秒后执行任务"
                        binding.countDownPgr.progress = 0
                        requireContext().openApplication(Constant.DING_DING)
                    }
                })
                timerKit?.start()
            }

            Constant.EXECUTE_NEXT_TASK_CODE -> {
                dailyTaskHandler.post(dailyTaskRunnable)
            }

            Constant.COMPLETED_ALL_TASK_CODE -> {
                "${TimeKit.getCurrentTime()}：当天所有任务已执行完毕".writeToFile(requireContext().createLogFile())
                binding.tipsView.text = "当天所有任务已执行完毕"
                binding.tipsView.setTextColor(R.color.iOSGreen.convertColor(requireContext()))
                dailyTaskAdapter.updateCurrentTaskState(-1)
            }
        }
        return true
    }
}