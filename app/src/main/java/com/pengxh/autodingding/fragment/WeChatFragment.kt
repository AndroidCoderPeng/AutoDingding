package com.pengxh.autodingding.fragment

import android.os.Bundle
import android.view.LayoutInflater
import android.view.ViewGroup
import com.pengxh.autodingding.databinding.FragmentWechatBinding
import com.pengxh.kt.lite.base.KotlinBaseFragment

class WeChatFragment : KotlinBaseFragment<FragmentWechatBinding>() {
    override fun initEvent() {

    }

    override fun initOnCreate(savedInstanceState: Bundle?) {

    }

    override fun initViewBinding(
        inflater: LayoutInflater, container: ViewGroup?
    ): FragmentWechatBinding {
        return FragmentWechatBinding.inflate(inflater, container, false)
    }

    override fun observeRequestState() {

    }

    override fun setupTopBarLayout() {

    }
}