package com.pengxh.autodingding.ui

import android.view.MenuItem
import androidx.fragment.app.Fragment
import androidx.viewpager.widget.ViewPager
import com.gyf.immersionbar.ImmersionBar
import com.pengxh.autodingding.R
import com.pengxh.autodingding.adapter.BaseFragmentAdapter
import com.pengxh.autodingding.fragment.DingDingFragment
import com.pengxh.autodingding.fragment.SettingsFragment
import com.pengxh.kt.lite.base.KotlinBaseActivity
import com.pengxh.kt.lite.extensions.convertColor
import com.pengxh.kt.lite.utils.ImmerseStatusBarUtil
import com.pengxh.kt.lite.utils.SaveKeyValues
import com.pengxh.kt.lite.widget.dialog.AlertMessageDialog
import kotlinx.android.synthetic.main.activity_main.*

class MainActivity : KotlinBaseActivity() {

    private var menuItem: MenuItem? = null
    private val fragmentPages: MutableList<Fragment> = ArrayList()

    override fun setupTopBarLayout() {
        ImmerseStatusBarUtil.setColor(
            this, R.color.colorAppThemeLight.convertColor(this)
        )
        ImmersionBar.with(this).statusBarDarkFont(false).init()
        titleView.text = "钉钉打卡"
    }

    override fun initData() {
        fragmentPages.add(DingDingFragment())
        fragmentPages.add(SettingsFragment())

        val isFirst = SaveKeyValues.getValue("isFirst", true) as Boolean
        if (isFirst) {
            AlertMessageDialog.Builder()
                .setContext(this)
                .setTitle("温馨提醒")
                .setMessage("本软件仅供内部使用，严禁商用或者用作其他非法用途")
                .setPositiveButton("知道了")
                .setOnDialogButtonClickListener(object :
                    AlertMessageDialog.OnDialogButtonClickListener {
                    override fun onConfirmClick() {
                        SaveKeyValues.putValue("isFirst", false)
                    }
                }).build().show()
        }
    }

    override fun initEvent() {
        bottomNavigation.setOnItemSelectedListener { item ->
            val itemId: Int = item.itemId
            if (itemId == R.id.nav_dingding) {
//                if (DingDingUtil.isAppAvailable(Constant.DINGDING)) {
//                    viewPager.currentItem = 0
//                    titleView.text = "钉钉打卡"
//                } else {
//                    showAlertDialog("手机没有安装《钉钉》软件，无法自动打卡")
//                }
                viewPager.currentItem = 0
                titleView.text = "钉钉打卡"
            } else if (itemId == R.id.nav_settings) {
                viewPager.currentItem = 1
                titleView.text = "其他设置"
            }
            false
        }
        val fragmentAdapter = BaseFragmentAdapter(supportFragmentManager, fragmentPages)
        viewPager.adapter = fragmentAdapter
        viewPager.offscreenPageLimit = fragmentPages.size
        viewPager.addOnPageChangeListener(object : ViewPager.OnPageChangeListener {
            override fun onPageScrolled(
                position: Int,
                positionOffset: Float,
                positionOffsetPixels: Int
            ) {
            }

            override fun onPageSelected(position: Int) {
                if (menuItem != null) {
                    menuItem!!.isChecked = false
                } else {
                    bottomNavigation.menu.getItem(0).isChecked = false
                }
                menuItem = bottomNavigation.menu.getItem(position)
                menuItem!!.isChecked = true
                if (position == 0) {
                    titleView.text = "钉钉打卡"
                } else {
                    titleView.text = "其他设置"
                }
            }

            override fun onPageScrollStateChanged(state: Int) {}
        })
    }

    override fun initLayoutView(): Int = R.layout.activity_main

    override fun observeRequestState() {

    }

    private fun showAlertDialog(message: String) {
        AlertMessageDialog.Builder()
            .setContext(this)
            .setTitle("温馨提醒")
            .setMessage(message)
            .setPositiveButton("知道了")
            .setOnDialogButtonClickListener(object :
                AlertMessageDialog.OnDialogButtonClickListener {
                override fun onConfirmClick() {

                }
            }).build().show()
    }
}