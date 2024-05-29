package com.pengxh.autodingding.fragment

import android.content.Intent
import android.os.Bundle
import android.os.Handler
import android.os.Message
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.activity.result.contract.ActivityResultContracts
import com.pengxh.autodingding.BaseApplication
import com.pengxh.autodingding.adapter.DateTimeAdapter
import com.pengxh.autodingding.bean.DateTimeBean
import com.pengxh.autodingding.databinding.FragmentDingdingBinding
import com.pengxh.autodingding.extensions.openApplication
import com.pengxh.autodingding.greendao.DateTimeBeanDao
import com.pengxh.autodingding.ui.AddTimerTaskActivity
import com.pengxh.autodingding.ui.UpdateTimerTaskActivity
import com.pengxh.autodingding.utils.Constant
import com.pengxh.kt.lite.base.KotlinBaseFragment
import com.pengxh.kt.lite.divider.RecyclerViewItemOffsets
import com.pengxh.kt.lite.utils.WeakReferenceHandler
import com.pengxh.kt.lite.widget.dialog.AlertControlDialog

class DingDingFragment : KotlinBaseFragment<FragmentDingdingBinding>(), Handler.Callback {

    private val kTag = "DingDingFragment"
    private val dateTimeBeanDao by lazy { BaseApplication.get().daoSession.dateTimeBeanDao }
    private val weakReferenceHandler by lazy { WeakReferenceHandler(this) }
    private var dateTimeAdapter: DateTimeAdapter? = null
    private var dataBeans: MutableList<DateTimeBean> = ArrayList()
    private var isRefresh = false
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
        getAutoDingDingTasks()
    }

    private fun getAutoDingDingTasks() {
        val queryResult = dateTimeBeanDao.queryBuilder()
            .orderDesc(DateTimeBeanDao.Properties.Date).list()
        if (isRefresh) {
            dateTimeAdapter?.setRefreshData(queryResult)
            isRefresh = false

            if (dataBeans.size == 0) {
                binding.emptyView.visibility = View.VISIBLE
            } else {
                binding.emptyView.visibility = View.GONE
            }
        } else {
            dataBeans = queryResult
            weakReferenceHandler.sendEmptyMessage(2023042601)
        }
    }

    override fun handleMessage(msg: Message): Boolean {
        if (msg.what == 2023042601) {
            if (dataBeans.size == 0) {
                binding.emptyView.visibility = View.VISIBLE
            } else {
                binding.emptyView.visibility = View.GONE
                dateTimeAdapter = DateTimeAdapter(requireContext(), dataBeans)
                binding.weeklyRecyclerView.adapter = dateTimeAdapter
                binding.weeklyRecyclerView.addItemDecoration(
                    RecyclerViewItemOffsets(0, 10, 0, 20)
                )
                dateTimeAdapter?.setOnItemClickListener(object :
                    DateTimeAdapter.OnItemClickListener {
                    override fun onItemClick(position: Int) {
                        val intent = Intent(requireContext(), UpdateTimerTaskActivity::class.java)
                        intent.putExtra(Constant.INTENT_PARAM, dataBeans[position].uuid)
                        updateTaskLauncher.launch(intent)
                    }

                    override fun onItemLongClick(position: Int) {
                        //标记被点击的item位置
                        clickedPosition = position
                        AlertControlDialog.Builder()
                            .setContext(requireContext())
                            .setTitle("删除提示")
                            .setMessage("确定要删除这个任务吗")
                            .setNegativeButton("取消")
                            .setPositiveButton("确定")
                            .setOnDialogButtonClickListener(object :
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
        return true
    }

    private val updateTaskLauncher = registerForActivityResult(
        ActivityResultContracts.StartActivityForResult()
    ) {
        Log.d(kTag, "updateTaskLauncher: ")
        isRefresh = true
        getAutoDingDingTasks()
    }

    private fun deleteTask(bean: DateTimeBean) {
        dateTimeBeanDao.delete(bean)
        dataBeans.removeAt(clickedPosition)
        dateTimeAdapter?.notifyItemRemoved(clickedPosition)
        dateTimeAdapter?.notifyItemRangeChanged(
            clickedPosition, dataBeans.size - clickedPosition
        )
        dateTimeAdapter?.stopCountDownTimer(bean)
        if (dataBeans.size == 0) {
            binding.emptyView.visibility = View.VISIBLE
        } else {
            binding.emptyView.visibility = View.GONE
        }
    }

    override fun initEvent() {
        binding.addTimerButton.setOnClickListener {
            addTaskLauncher.launch(Intent(requireContext(), AddTimerTaskActivity::class.java))
        }
    }

    private val addTaskLauncher = registerForActivityResult(
        ActivityResultContracts.StartActivityForResult()
    ) {
        Log.d(kTag, "addTaskLauncher: ")
        isRefresh = true
        getAutoDingDingTasks()
    }
}