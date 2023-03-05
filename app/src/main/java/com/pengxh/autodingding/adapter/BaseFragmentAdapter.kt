package com.pengxh.autodingding.adapter

import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentManager
import androidx.fragment.app.FragmentPagerAdapter

class BaseFragmentAdapter(fm: FragmentManager, private val pageList: List<Fragment>) :
    FragmentPagerAdapter(fm) {
    override fun getItem(position: Int): Fragment = pageList[position]

    override fun getCount(): Int = pageList.size
}