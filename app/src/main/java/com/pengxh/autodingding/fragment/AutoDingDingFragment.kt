package com.pengxh.autodingding.fragment

import android.os.Bundle
import android.view.LayoutInflater
import android.view.ViewGroup
import com.pengxh.autodingding.databinding.FragmentAutoDingdingBinding
import com.pengxh.kt.lite.base.KotlinBaseFragment

/**
 * 支持每天上下班半小时随机时间自动打卡
 * */
class AutoDingDingFragment : KotlinBaseFragment<FragmentAutoDingdingBinding>() {
    override fun initEvent() {

    }

    override fun initOnCreate(savedInstanceState: Bundle?) {

    }

    override fun initViewBinding(
        inflater: LayoutInflater,
        container: ViewGroup?
    ): FragmentAutoDingdingBinding {
        return FragmentAutoDingdingBinding.inflate(inflater, container, false)
    }

    override fun observeRequestState() {

    }

    override fun setupTopBarLayout() {

    }
}