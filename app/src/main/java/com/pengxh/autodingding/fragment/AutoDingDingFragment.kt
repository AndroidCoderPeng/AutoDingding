package com.pengxh.autodingding.fragment

import android.os.Bundle
import android.os.Handler
import android.os.Message
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.lifecycle.ViewModelProvider
import androidx.work.Constraints
import androidx.work.ExistingPeriodicWorkPolicy
import androidx.work.NetworkType
import androidx.work.PeriodicWorkRequest
import androidx.work.WorkManager
import com.pengxh.autodingding.BaseApplication
import com.pengxh.autodingding.R
import com.pengxh.autodingding.adapter.TaskTimeAdapter
import com.pengxh.autodingding.bean.TaskTimeBean
import com.pengxh.autodingding.databinding.FragmentAutoDingdingBinding
import com.pengxh.autodingding.extensions.initImmersionBar
import com.pengxh.autodingding.greendao.TaskTimeBeanDao
import com.pengxh.autodingding.utils.DailyTaskWorker
import com.pengxh.autodingding.vm.DateDayViewModel
import com.pengxh.autodingding.widget.BottomSelectTimeSheet
import com.pengxh.kt.lite.base.KotlinBaseFragment
import com.pengxh.kt.lite.divider.RecyclerViewItemOffsets
import com.pengxh.kt.lite.extensions.appendZero
import com.pengxh.kt.lite.extensions.dp2px
import com.pengxh.kt.lite.extensions.show
import com.pengxh.kt.lite.utils.WeakReferenceHandler
import com.pengxh.kt.lite.widget.dialog.AlertControlDialog
import java.util.UUID
import java.util.concurrent.TimeUnit

/**
 * 支持每天上下班半小时随机时间自动打卡
 * */
class AutoDingDingFragment : KotlinBaseFragment<FragmentAutoDingdingBinding>(), Handler.Callback {

    companion object {
        var weakReferenceHandler: WeakReferenceHandler? = null
    }

    private val kTag = "AutoDingDingFragment"
    private val taskTimeBeanDao by lazy { BaseApplication.get().daoSession.taskTimeBeanDao }
    private val marginOffset by lazy { 10.dp2px(requireContext()) }
    private val workManager by lazy { WorkManager.getInstance(requireContext()) }
    private lateinit var taskTimeAdapter: TaskTimeAdapter
    private lateinit var dateDayViewModel: DateDayViewModel
    private var taskBeans: MutableList<TaskTimeBean> = ArrayList()
    private var clickedPosition = 0
    private var isTaskStarted = false

    override fun initEvent() {
        binding.executeTaskButton.setOnClickListener {
            if (isTaskStarted) {
                workManager.cancelAllWork()
                binding.executeTaskButton.setImageResource(R.drawable.ic_play_fill)
                //重置任务状态
                binding.nextTaskTimeView.text = "--:--"
                isTaskStarted = false
            } else {
                isTaskStarted = true
                binding.executeTaskButton.setImageResource(R.drawable.ic_stop_fill)

                val constraints = Constraints.Builder()
                    .setRequiredNetworkType(NetworkType.CONNECTED)
                    .build()
                val workRequest = PeriodicWorkRequest.Builder(
                    DailyTaskWorker::class.java, 1, TimeUnit.DAYS
                ).setConstraints(constraints).build()
                workManager.enqueueUniquePeriodicWork(
                    "DailyTaskWorker", ExistingPeriodicWorkPolicy.REPLACE, workRequest
                )
            }
        }
    }

    override fun handleMessage(msg: Message): Boolean {
        if (msg.what == 2024070801) {
            binding.nextTaskTimeView.text = msg.obj as String
        } else if (msg.what == 2024070802) {
            binding.countDownTextView.text = "${msg.obj as String}秒后执行定时任务"
        }
        return true
    }

    override fun initOnCreate(savedInstanceState: Bundle?) {
        binding.marqueeView.requestFocus()

        weakReferenceHandler = WeakReferenceHandler(this)
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
                                //保存数据
                                val bean = TaskTimeBean()
                                bean.uuid = UUID.randomUUID().toString()
                                bean.startTime = "$startTime:${randomSeconds()}"
                                bean.endTime = "$endTime:${randomSeconds()}"

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
                                //修改数据
                                val bean = taskBeans[position]
                                bean.startTime = "$startTime:${randomSeconds()}"
                                bean.endTime = "$endTime:${randomSeconds()}"

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

    /**
     * 产生随机秒数
     * */
    private fun randomSeconds(): String {
        return (0 until 60).random().appendZero()
    }
}