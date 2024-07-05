package com.pengxh.autodingding.fragment

import android.os.Bundle
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.lifecycle.ViewModelProvider
import com.pengxh.autodingding.R
import com.pengxh.autodingding.databinding.FragmentAutoDingdingBinding
import com.pengxh.autodingding.extensions.initImmersionBar
import com.pengxh.autodingding.vm.DateDayViewModel
import com.pengxh.kt.lite.base.KotlinBaseFragment

/**
 * 支持每天上下班半小时随机时间自动打卡
 * */
class AutoDingDingFragment : KotlinBaseFragment<FragmentAutoDingdingBinding>() {

    private lateinit var dateDayViewModel: DateDayViewModel

    override fun initEvent() {

    }

    override fun initOnCreate(savedInstanceState: Bundle?) {
        binding.marqueeView.requestFocus()

        dateDayViewModel = ViewModelProvider(this)[DateDayViewModel::class.java]
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