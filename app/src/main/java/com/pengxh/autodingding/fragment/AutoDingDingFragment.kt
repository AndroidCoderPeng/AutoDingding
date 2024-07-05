package com.pengxh.autodingding.fragment

import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.lifecycle.ViewModelProvider
import com.pengxh.autodingding.R
import com.pengxh.autodingding.adapter.TaskTimeAdapter
import com.pengxh.autodingding.bean.TaskTimeBean
import com.pengxh.autodingding.databinding.FragmentAutoDingdingBinding
import com.pengxh.autodingding.extensions.initImmersionBar
import com.pengxh.autodingding.vm.DateDayViewModel
import com.pengxh.kt.lite.base.KotlinBaseFragment
import com.pengxh.kt.lite.divider.RecyclerViewItemOffsets
import com.pengxh.kt.lite.extensions.dp2px

/**
 * 支持每天上下班半小时随机时间自动打卡
 * */
class AutoDingDingFragment : KotlinBaseFragment<FragmentAutoDingdingBinding>() {

    private val kTag = "AutoDingDingFragment"
    private val marginOffset by lazy { 10.dp2px(requireContext()) }
    private lateinit var taskTimeAdapter: TaskTimeAdapter
    private lateinit var dateDayViewModel: DateDayViewModel
    private var dataBeans = ArrayList<TaskTimeBean>()

    override fun initEvent() {

    }

    override fun initOnCreate(savedInstanceState: Bundle?) {
        binding.marqueeView.requestFocus()

        dateDayViewModel = ViewModelProvider(this)[DateDayViewModel::class.java]

        taskTimeAdapter = TaskTimeAdapter(requireContext(), dataBeans)
        binding.recyclerView.adapter = taskTimeAdapter
        binding.recyclerView.addItemDecoration(
            RecyclerViewItemOffsets(
                marginOffset, marginOffset shr 1, marginOffset, marginOffset shr 1
            )
        )
        taskTimeAdapter.setOnItemClickListener(object : TaskTimeAdapter.OnItemClickListener {
            override fun onAddTaskClick() {
                Log.d(kTag, "onAddTaskClick: ")
            }

            override fun onItemClick(bean: TaskTimeBean) {

            }

            override fun onItemLongClick(bean: TaskTimeBean) {

            }
        })
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