package com.pengxh.autodingding.adapter

import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentManager
import androidx.fragment.app.FragmentPagerAdapter

class BaseFragmentAdapter(manager: FragmentManager, private val pages: List<Fragment>) :
    FragmentPagerAdapter(manager) {
    override fun getItem(position: Int): Fragment = pages[position]

    override fun getCount(): Int = pages.size
}