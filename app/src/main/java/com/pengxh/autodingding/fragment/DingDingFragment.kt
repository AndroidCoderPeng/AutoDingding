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
import com.pengxh.autodingding.adapter.DateTimeAdapter
import com.pengxh.autodingding.bean.DateTimeBean
import com.pengxh.autodingding.databinding.FragmentDingdingBinding
import com.pengxh.autodingding.extensions.convertToWeek
import com.pengxh.autodingding.extensions.diffCurrent
import com.pengxh.autodingding.extensions.getTaskIndex
import com.pengxh.autodingding.extensions.isLateThenCurrent
import com.pengxh.autodingding.extensions.openApplication
import com.pengxh.autodingding.extensions.showDatePicker
import com.pengxh.autodingding.extensions.showDateTimePicker
import com.pengxh.autodingding.greendao.DateTimeBeanDao
import com.pengxh.autodingding.utils.Constant
import com.pengxh.autodingding.utils.CountDownTimerKit
import com.pengxh.autodingding.utils.OnDateSelectedCallback
import com.pengxh.autodingding.utils.OnTimeCountDownCallback
import com.pengxh.autodingding.utils.TimeKit
import com.pengxh.kt.lite.base.KotlinBaseFragment
import com.pengxh.kt.lite.divider.RecyclerViewItemOffsets
import com.pengxh.kt.lite.extensions.appendZero
import com.pengxh.kt.lite.extensions.convertColor
import com.pengxh.kt.lite.extensions.createLogFile
import com.pengxh.kt.lite.extensions.dp2px
import com.pengxh.kt.lite.extensions.show
import com.pengxh.kt.lite.extensions.toJson
import com.pengxh.kt.lite.extensions.writeToFile
import com.pengxh.kt.lite.utils.WeakReferenceHandler
import com.pengxh.kt.lite.widget.dialog.AlertControlDialog
import java.util.UUID

@SuppressLint("NotifyDataSetChanged", "SetTextI18n")
class DingDingFragment : KotlinBaseFragment<FragmentDingdingBinding>(), Handler.Callback {

    companion object {
        var weakReferenceHandler: WeakReferenceHandler? = null
    }

    private val kTag = "DingDingFragment"
    private val dateTimeBeanDao by lazy { BaseApplication.get().daoSession.dateTimeBeanDao }
    private val marginOffset by lazy { 10.dp2px(requireContext()) }
    private val dailyTaskHandler = Handler(Looper.getMainLooper())
    private lateinit var dateTimeAdapter: DateTimeAdapter
    private var dataBeans: MutableList<DateTimeBean> = ArrayList()
    private var isTaskStarted = false
    private var timerKit: CountDownTimerKit? = null

    override fun setupTopBarLayout() {

    }

    override fun observeRequestState() {

    }

    override fun initViewBinding(
        inflater: LayoutInflater, container: ViewGroup?
    ): FragmentDingdingBinding {
        return FragmentDingdingBinding.inflate(inflater, container, false)
    }

    override fun initOnCreate(savedInstanceState: Bundle?) {
        weakReferenceHandler = WeakReferenceHandler(this)
        dataBeans = dateTimeBeanDao.queryBuilder().orderDesc(
            DateTimeBeanDao.Properties.Date
        ).list()

        if (dataBeans.size == 0) {
            binding.emptyView.visibility = View.VISIBLE
        } else {
            binding.emptyView.visibility = View.GONE
        }

        dateTimeAdapter = DateTimeAdapter(requireContext(), dataBeans)
        binding.recyclerView.adapter = dateTimeAdapter
        binding.recyclerView.addItemDecoration(
            RecyclerViewItemOffsets(
                marginOffset, marginOffset shr 1, marginOffset, marginOffset shr 1
            )
        )
        dateTimeAdapter.setOnItemClickListener(object : DateTimeAdapter.OnItemClickListener {
            override fun onItemClick(position: Int) {
                if (isTaskStarted) {
                    "任务进行中，无法修改，请先取消当前任务".show(requireContext())
                    return
                }
                AlertControlDialog.Builder()
                    .setContext(requireContext())
                    .setTitle("修改打卡任务")
                    .setMessage("是否需要调整打卡时间？")
                    .setNegativeButton("取消")
                    .setPositiveButton("确定")
                    .setOnDialogButtonClickListener(object :
                        AlertControlDialog.OnDialogButtonClickListener {
                        override fun onConfirmClick() {
                            val dateTimeBean = dataBeans[position]
                            requireActivity().showDateTimePicker(dateTimeBean,
                                object : OnDateSelectedCallback {
                                    override fun onTimePicked(vararg args: String) {
                                        dateTimeBean.date = "${args[0]}-${args[1]}-${args[2]}"
                                        dateTimeBean.time =
                                            "${args[3]}:${args[4]}:${randomSeconds()}"
                                        dateTimeBean.weekDay = dateTimeBean.date.convertToWeek()

                                        dateTimeBeanDao.update(dateTimeBean)
                                        dataBeans.sortWith { x, y ->
                                            compareBy<DateTimeBean> {
                                                it.date
                                            }.thenBy {
                                                it.time
                                            }.compare(x, y)
                                        }
                                        dateTimeAdapter.notifyDataSetChanged()
                                    }
                                }
                            )
                        }

                        override fun onCancelClick() {
                            val dateTimeBean = dataBeans[position]
                            requireActivity().showDatePicker(dateTimeBean,
                                object : OnDateSelectedCallback {
                                    override fun onTimePicked(vararg args: String) {
                                        dateTimeBean.date = "${args[0]}-${args[1]}-${args[2]}"
                                        dateTimeBean.weekDay = dateTimeBean.date.convertToWeek()

                                        dateTimeBeanDao.update(dateTimeBean)
                                        dataBeans.sortWith { x, y ->
                                            compareBy<DateTimeBean> {
                                                it.date
                                            }.thenBy {
                                                it.time
                                            }.compare(x, y)
                                        }
                                        dateTimeAdapter.notifyDataSetChanged()
                                    }
                                })
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
                            dateTimeBeanDao.delete(dataBeans[position])
                            dataBeans.removeAt(position)
                            dateTimeAdapter.notifyDataSetChanged()
                            if (dataBeans.size == 0) {
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
            if (dateTimeBeanDao.loadAll().isEmpty()) {
                "请先添加任务时间点".show(requireContext())
                return@setOnClickListener
            }

            if (!isTaskStarted) {
                dailyTaskHandler.post(dailyTaskRunnable)
                Log.d(kTag, "initEvent: 开启串行任务Runnable")
                isTaskStarted = true
                binding.executeTaskButton.setImageResource(R.drawable.ic_stop)
            } else {
                dailyTaskHandler.removeCallbacks(dailyTaskRunnable)
                Log.d(kTag, "initEvent: 取消串行任务Runnable")
                timerKit?.cancel()
                isTaskStarted = false
                binding.executeTaskButton.setImageResource(R.drawable.ic_start)
                binding.tipsView.text = ""
                binding.countDownTimeView.text = "0秒后执行任务"
                binding.countDownPgr.progress = 0
                dateTimeAdapter.updateCurrentTaskState(-1)
            }
        }

        binding.addTimerButton.setOnClickListener {
            if (isTaskStarted) {
                "任务进行中，无法添加，请先取消当前任务".show(requireContext())
                return@setOnClickListener
            }
            requireActivity().showDateTimePicker(null, object : OnDateSelectedCallback {
                override fun onTimePicked(vararg args: String) {
                    val bean = DateTimeBean()
                    bean.uuid = UUID.randomUUID().toString()
                    bean.date = "${args[0]}-${args[1]}-${args[2]}"
                    bean.time = "${args[3]}:${args[4]}:${randomSeconds()}"
                    bean.weekDay = bean.date.convertToWeek()

                    dateTimeBeanDao.insert(bean)
                    dataBeans.add(bean)
                    dataBeans.sortWith { x, y ->
                        compareBy<DateTimeBean> { it.date }.thenBy { it.time }.compare(x, y)
                    }
                    dateTimeAdapter.notifyDataSetChanged()
                    binding.emptyView.visibility = View.GONE
                }
            })
        }
    }

    /**
     * 当日串行任务Runnable
     * */
    private val dailyTaskRunnable = object : Runnable {
        override fun run() {
            val taskIndex = dataBeans.getTaskIndex()
            Log.d(kTag, "run: taskIndex => $taskIndex")
            val handler = weakReferenceHandler ?: return
            //如果只有一个任务，直接执行，不用考虑顺序
            if (dataBeans.count() == 1) {
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

    override fun handleMessage(msg: Message): Boolean {
        when (msg.what) {
            Constant.EXECUTE_ONLY_ONE_TASK_CODE -> {
                val task = dataBeans.first()
                if (task.isLateThenCurrent()) {
                    "${TimeKit.getCurrentTime()}：只有 1 个任务: ${task.toJson()}，直接按时执行".writeToFile(
                        requireContext().createLogFile()
                    )
                    binding.tipsView.text = "只有 1 个任务"
                    binding.tipsView.setTextColor(R.color.purple_500.convertColor(requireContext()))

                    dateTimeAdapter.updateCurrentTaskState(0)

                    val diffSeconds = task.diffCurrent()
                    binding.countDownPgr.max = diffSeconds.toInt()
                    timerKit = CountDownTimerKit(diffSeconds, object : OnTimeCountDownCallback {
                        override fun updateCountDownSeconds(remainingSeconds: Long) {
                            binding.countDownTimeView.text = "$remainingSeconds 秒后执行任务"
                            binding.countDownPgr.progress = (diffSeconds - remainingSeconds).toInt()
                        }

                        override fun onFinish() {
                            "${TimeKit.getCurrentTime()}：执行任务".writeToFile(requireContext().createLogFile())
                            binding.countDownTimeView.text = "0秒后执行任务"
                            binding.countDownPgr.progress = 0
                            dateTimeAdapter.updateCurrentTaskState(-1)
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
                val task = dataBeans[index]
                "${TimeKit.getCurrentTime()}：即将执行第 ${index + 1} 个任务: ${task.toJson()}".writeToFile(
                    requireContext().createLogFile()
                )
                binding.tipsView.text = "即将执行第 ${index + 1} 个任务"
                binding.tipsView.setTextColor(R.color.purple_500.convertColor(requireContext()))

                dateTimeAdapter.updateCurrentTaskState(index)

                //计算任务时间和当前时间的差值
                val diffSeconds = task.diffCurrent()
                binding.countDownPgr.max = diffSeconds.toInt()
                timerKit = CountDownTimerKit(diffSeconds, object : OnTimeCountDownCallback {
                    override fun updateCountDownSeconds(remainingSeconds: Long) {
                        binding.countDownTimeView.text = "$remainingSeconds 秒后执行任务"
                        binding.countDownPgr.progress = (diffSeconds - remainingSeconds).toInt()
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
                dateTimeAdapter.updateCurrentTaskState(-1)
            }
        }
        return true
    }

    /**
     * 产生随机秒数
     * */
    private fun randomSeconds(): String {
        return (0 until 60).random().appendZero()
    }
}