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
import com.pengxh.kt.lite.extensions.dp2px
import com.pengxh.kt.lite.widget.dialog.AlertControlDialog
import java.util.UUID

/**
 * 支持每天上下班半小时随机时间自动打卡
 * */
class AutoDingDingFragment : KotlinBaseFragment<FragmentAutoDingdingBinding>() {

    private val kTag = "AutoDingDingFragment"
    private val taskTimeBeanDao by lazy { BaseApplication.get().daoSession.taskTimeBeanDao }
    private val marginOffset by lazy { 10.dp2px(requireContext()) }
    private lateinit var taskTimeAdapter: TaskTimeAdapter
    private lateinit var dateDayViewModel: DateDayViewModel
    private var dataBeans: MutableList<TaskTimeBean> = ArrayList()
    private var clickedPosition = 0

    override fun initEvent() {

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
            dataBeans = queryResult
            taskTimeAdapter = TaskTimeAdapter(requireContext(), dataBeans)
            binding.recyclerView.adapter = taskTimeAdapter
            binding.recyclerView.addItemDecoration(
                RecyclerViewItemOffsets(
                    marginOffset, marginOffset shr 1, marginOffset, marginOffset shr 1
                )
            )
            taskTimeAdapter.setOnItemClickListener(object : TaskTimeAdapter.OnItemClickListener {
                override fun onAddTaskClick() {
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
                    //修改时间
                    BottomSelectTimeSheet(
                        requireContext(), object : BottomSelectTimeSheet.OnTimeSelectedCallback {
                            override fun onTimePicked(startTime: String, endTime: String) {
                                //onTimePicked: 22:34 ~ 22:34
                                //修改数据
                                val bean = dataBeans[position]
                                bean.startTime = startTime
                                bean.endTime = endTime

                                taskTimeBeanDao.update(bean)
                                //刷新列表
                                getTaskTimes(true)
                            }
                        }).show()
                }

                override fun onItemLongClick(position: Int) {
                    //标记被点击的item位置
                    clickedPosition = position
                    AlertControlDialog.Builder().setContext(requireContext()).setTitle("删除提示")
                        .setMessage("确定要删除这个任务吗").setNegativeButton("取消")
                        .setPositiveButton("确定").setOnDialogButtonClickListener(object :
                            AlertControlDialog.OnDialogButtonClickListener {
                            override fun onConfirmClick() {
                                deleteTask(dataBeans[position])
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
        dataBeans.removeAt(clickedPosition)
        taskTimeAdapter.notifyItemRemoved(clickedPosition)
        taskTimeAdapter.notifyItemRangeChanged(
            clickedPosition, dataBeans.size - clickedPosition
        )
        if (dataBeans.size == 0) {
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