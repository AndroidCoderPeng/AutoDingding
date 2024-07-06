package com.pengxh.autodingding.fragment

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.lifecycle.ViewModelProvider
import com.pengxh.autodingding.BaseApplication
import com.pengxh.autodingding.R
import com.pengxh.autodingding.adapter.TaskTimeAdapter
import com.pengxh.autodingding.bean.TaskTimeBean
import com.pengxh.autodingding.databinding.FragmentAutoDingdingBinding
import com.pengxh.autodingding.extensions.initImmersionBar
import com.pengxh.autodingding.greendao.TaskTimeBeanDao
import com.pengxh.autodingding.vm.DateDayViewModel
import com.pengxh.autodingding.widget.BottomSelectTimeSheet
import com.pengxh.kt.lite.base.KotlinBaseFragment
import com.pengxh.kt.lite.divider.RecyclerViewItemOffsets
import com.pengxh.kt.lite.extensions.appendZero
import com.pengxh.kt.lite.extensions.dp2px
import com.pengxh.kt.lite.extensions.show
import com.pengxh.kt.lite.widget.dialog.AlertControlDialog
import java.text.SimpleDateFormat
import java.util.Locale
import java.util.Random
import java.util.UUID
import kotlin.math.abs

/**
 * 支持每天上下班半小时随机时间自动打卡
 * */
class AutoDingDingFragment : KotlinBaseFragment<FragmentAutoDingdingBinding>() {

    private val kTag = "AutoDingDingFragment"
    private val taskTimeBeanDao by lazy { BaseApplication.get().daoSession.taskTimeBeanDao }
    private val marginOffset by lazy { 10.dp2px(requireContext()) }
    private val format by lazy { SimpleDateFormat("HH:mm", Locale.CHINA) }
    private val random by lazy { Random() }
    private lateinit var taskTimeAdapter: TaskTimeAdapter
    private lateinit var dateDayViewModel: DateDayViewModel
    private var taskBeans: MutableList<TaskTimeBean> = ArrayList()
    private var clickedPosition = 0
    private var isTaskStarted = false
    private var isSingleTaskStart = false
    private var currentTask = 0

    override fun initEvent() {
        binding.executeTaskButton.setOnClickListener {
            if (taskBeans.isEmpty()) {
                "请先添加任务".show(requireContext())
                return@setOnClickListener
            }

            if (isTaskStarted) {
                //重置任务位
                currentTask = 0
                //重置任务状态
                isTaskStarted = false
                binding.executeTaskButton.setImageResource(R.drawable.ic_play_fill)
            } else {
                isTaskStarted = true
                binding.executeTaskButton.setImageResource(R.drawable.ic_stop_fill)

//                val taskRealTime = calculateTaskRealTime(null)
//                Log.d(kTag, "initEvent: $taskRealTime")
//                isSingleTaskStart = true
//                object : CountDownTimer(10 * 1000, 1000) {
//                    override fun onTick(millisUntilFinished: Long) {
//                        Log.d(kTag, "onTick: ${millisUntilFinished / 1000}")
//                    }
//
//                    override fun onFinish() {
//                        isSingleTaskStart = false
//                    }
//                }.start()
            }

//            taskBeans.forEachIndexed { index, task ->
//                val date = if (index == taskBeans.lastIndex) {
//                    val dateFormat = SimpleDateFormat("yyyy-MM-dd", Locale.CHINA)
//                    dateFormat.format(Date().time + 86400 * 1000L)
//                } else {
//                    System.currentTimeMillis().timestampToDate()
//                }
//                Log.d(kTag, "date: $date")
//            }


//            while (executeNextTask) {
//                executeNextTask = false
//                val taskRealTime = calculateTaskRealTime(taskBeans.first())
//                Log.d(kTag, "initEvent: $taskRealTime")
//
//                //将时间整理为 年-月-日 时:分
//
//                //yyyy-MM-dd HH:mm
//                val diffCurrentMillis = time.diffCurrentMillis()
//                object : CountDownTimer(diffCurrentMillis, 1) {
//                    override fun onTick(millisUntilFinished: Long) {
//                        Log.d(kTag, "onTick: ${millisUntilFinished / 1000}秒后执行定时任务")
//                    }
//
//                    override fun onFinish() {
//
//                    }
//                }.start()
//            }
//            val emailAddress = SaveKeyValues.getValue(Constant.EMAIL_ADDRESS, "") as String
//            if (emailAddress.isNotEmpty()) {
//                //发送一封通知邮件，告诉用户打卡时间
//                lifecycleScope.launch(Dispatchers.IO) {
//                    "打卡时间：${taskRealTime}".createMail(emailAddress).sendTextMail()
//                }
//            }
        }
    }

    /**
     * 在任务时间区间内随机生成一个任务时间
     * */
    private fun calculateTaskRealTime(bean: TaskTimeBean): String {
        val startTime = format.parse(bean.startTime)!!
        val startHours = startTime.hours

        val endTime = format.parse(bean.endTime)!!

        val diff = abs(endTime.time - startTime.time)
        val interval = (diff / (60 * 1000)).toInt()

        //计算任务真实分钟
        val realMinute = random.nextInt(interval)
        return "$startHours:${realMinute.appendZero()}"
    }

    override fun initOnCreate(savedInstanceState: Bundle?) {
        binding.marqueeView.requestFocus()

        dateDayViewModel = ViewModelProvider(this)[DateDayViewModel::class.java]

        getTaskTimes(false)
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