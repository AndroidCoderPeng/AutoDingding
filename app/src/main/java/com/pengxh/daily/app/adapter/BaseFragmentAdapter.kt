package com.pengxh.daily.app.adapter

import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentManager
import androidx.fragment.app.FragmentPagerAdapter

class BaseFragmentAdapter(manager: FragmentManager, private val pages: List<Fragment>) :
    FragmentPagerAdapter(manager) {
    override fun getItem(position: Int): Fragment = pages[position]

    override fun getCount(): Int = pages.size

    override fun destroyItem(container: ViewGroup, position: Int, `object`: Any) {
        //注释掉父类方法，一直不销毁Fragment
    }
}