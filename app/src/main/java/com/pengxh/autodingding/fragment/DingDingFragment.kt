package com.pengxh.autodingding.fragment

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.pengxh.autodingding.BaseApplication
import com.pengxh.autodingding.adapter.DateTimeAdapter
import com.pengxh.autodingding.bean.DateTimeBean
import com.pengxh.autodingding.databinding.FragmentDingdingBinding
import com.pengxh.autodingding.extensions.convertToWeek
import com.pengxh.autodingding.extensions.openApplication
import com.pengxh.autodingding.extensions.showDatePicker
import com.pengxh.autodingding.extensions.showDateTimePicker
import com.pengxh.autodingding.greendao.DateTimeBeanDao
import com.pengxh.autodingding.utils.Constant
import com.pengxh.autodingding.utils.OnDateSelectedCallback
import com.pengxh.kt.lite.base.KotlinBaseFragment
import com.pengxh.kt.lite.divider.RecyclerViewItemOffsets
import com.pengxh.kt.lite.extensions.appendZero
import com.pengxh.kt.lite.extensions.dp2px
import com.pengxh.kt.lite.widget.dialog.AlertControlDialog
import java.util.UUID

class DingDingFragment : KotlinBaseFragment<FragmentDingdingBinding>() {

    private val kTag = "DingDingFragment"
    private val dateTimeBeanDao by lazy { BaseApplication.get().daoSession.dateTimeBeanDao }
    private val marginOffset by lazy { 10.dp2px(requireContext()) }
    private lateinit var dateTimeAdapter: DateTimeAdapter
    private var dataBeans: MutableList<DateTimeBean> = ArrayList()
    private var clickedPosition = 0

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
        getAutoDingDingTasks(false)
    }

    private fun getAutoDingDingTasks(isRefresh: Boolean) {
        val queryResult = dateTimeBeanDao.queryBuilder().orderDesc(
            DateTimeBeanDao.Properties.Date
        ).list()

        if (queryResult.size == 0) {
            binding.emptyView.visibility = View.VISIBLE
        } else {
            binding.emptyView.visibility = View.GONE
        }

        if (isRefresh) {
            dateTimeAdapter.setRefreshData(queryResult)
        } else {
            dataBeans = queryResult
            dateTimeAdapter = DateTimeAdapter(requireContext(), dataBeans)
            binding.recyclerView.adapter = dateTimeAdapter
            binding.recyclerView.addItemDecoration(
                RecyclerViewItemOffsets(
                    marginOffset, marginOffset shr 1, marginOffset, marginOffset shr 1
                )
            )
            dateTimeAdapter.setOnItemClickListener(object : DateTimeAdapter.OnItemClickListener {
                override fun onItemClick(position: Int) {
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
                                requireActivity().showDateTimePicker(
                                    dateTimeBean, object : OnDateSelectedCallback {
                                        override fun onTimePicked(vararg args: String) {
                                            dateTimeBean.date = "${args[0]}-${args[1]}-${args[2]}"
                                            dateTimeBean.time =
                                                "${args[3]}:${args[4]}:${randomSeconds()}"
                                            dateTimeBean.weekDay = dateTimeBean.date.convertToWeek()

                                            dateTimeBeanDao.update(dateTimeBean)
                                            //刷新列表
                                            getAutoDingDingTasks(true)
                                        }
                                    })
                            }

                            override fun onCancelClick() {
                                val dateTimeBean = dataBeans[position]
                                requireActivity().showDatePicker(
                                    dateTimeBean, object : OnDateSelectedCallback {
                                        override fun onTimePicked(vararg args: String) {
                                            dateTimeBean.date = "${args[0]}-${args[1]}-${args[2]}"
                                            dateTimeBean.weekDay = dateTimeBean.date.convertToWeek()

                                            dateTimeBeanDao.update(dateTimeBean)
                                            //刷新列表
                                            getAutoDingDingTasks(true)
                                        }
                                    })
                            }
                        }).build().show()
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

                override fun onCountDownFinish() {
                    requireContext().openApplication(Constant.DING_DING)
                }
            })
        }
    }

    private fun deleteTask(bean: DateTimeBean) {
        dateTimeBeanDao.delete(bean)
        dataBeans.removeAt(clickedPosition)
        dateTimeAdapter.notifyItemRemoved(clickedPosition)
        dateTimeAdapter.notifyItemRangeChanged(
            clickedPosition, dataBeans.size - clickedPosition
        )
        dateTimeAdapter.stopCountDownTimer(bean)
        if (dataBeans.size == 0) {
            binding.emptyView.visibility = View.VISIBLE
        } else {
            binding.emptyView.visibility = View.GONE
        }
    }

    override fun initEvent() {
        binding.addTimerButton.setOnClickListener {
            requireActivity().showDateTimePicker(null, object : OnDateSelectedCallback {
                override fun onTimePicked(vararg args: String) {
                    val bean = DateTimeBean()
                    bean.uuid = UUID.randomUUID().toString()
                    bean.date = "${args[0]}-${args[1]}-${args[2]}"
                    bean.time = "${args[3]}:${args[4]}:${randomSeconds()}"
                    bean.weekDay = bean.date.convertToWeek()

                    dateTimeBeanDao.insert(bean)
                    //刷新列表
                    getAutoDingDingTasks(true)
                }
            })
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        dataBeans.forEach {
            dateTimeAdapter.stopCountDownTimer(it)
        }
    }

    /**
     * 产生随机秒数
     * */
    private fun randomSeconds(): String {
        return (0 until 60).random().appendZero()
    }
}