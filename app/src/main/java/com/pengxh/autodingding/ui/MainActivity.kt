package com.pengxh.autodingding.ui

import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.view.KeyEvent
import android.view.MenuItem
import androidx.fragment.app.Fragment
import androidx.viewpager.widget.ViewPager
import com.pengxh.autodingding.R
import com.pengxh.autodingding.adapter.BaseFragmentAdapter
import com.pengxh.autodingding.databinding.ActivityMainBinding
import com.pengxh.autodingding.extensions.initImmersionBar
import com.pengxh.autodingding.extensions.isAppAvailable
import com.pengxh.autodingding.fragment.DingDingFragment
import com.pengxh.autodingding.fragment.SettingsFragment
import com.pengxh.autodingding.service.FloatingWindowService
import com.pengxh.autodingding.utils.Constant
import com.pengxh.kt.lite.base.KotlinBaseActivity
import com.pengxh.kt.lite.extensions.show
import com.pengxh.kt.lite.utils.SaveKeyValues
import com.pengxh.kt.lite.widget.dialog.AlertMessageDialog


class MainActivity : KotlinBaseActivity<ActivityMainBinding>() {

    private val kTag = "MainActivity"
    private var menuItem: MenuItem? = null
    private val fragmentPages: MutableList<Fragment> = ArrayList()
    private var clickTime: Long = 0

    override fun initViewBinding(): ActivityMainBinding {
        return ActivityMainBinding.inflate(layoutInflater)
    }

    override fun setupTopBarLayout() {
        binding.rootView.initImmersionBar(this, false, R.color.colorAppThemeLight)
        binding.titleView.text = "自动打卡"
    }

    override fun initOnCreate(savedInstanceState: Bundle?) {
        fragmentPages.add(DingDingFragment())
        fragmentPages.add(SettingsFragment())

        if (!isAppAvailable(Constant.DING_DING)) {
            showAlertDialog()
            return
        }

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

        startService(Intent(this, FloatingWindowService::class.java))
    }

    override fun onNewIntent(intent: Intent?) {
        super.onNewIntent(intent)
        Log.d(kTag, "onNewIntent => 回到前台")
    }

    override fun initEvent() {
        binding.bottomNavigation.setOnItemSelectedListener { item ->
            val itemId: Int = item.itemId
            if (itemId == R.id.nav_dingding) {
                if (isAppAvailable(Constant.DING_DING)) {
                    binding.viewPager.currentItem = 0
                } else {
                    showAlertDialog()
                }
                binding.titleView.text = "自动打卡"
            } else if (itemId == R.id.nav_settings) {
                binding.viewPager.currentItem = 1
                binding.titleView.text = "其他设置"
            }
            false
        }
        val fragmentAdapter = BaseFragmentAdapter(supportFragmentManager, fragmentPages)
        binding.viewPager.adapter = fragmentAdapter
        binding.viewPager.offscreenPageLimit = fragmentPages.size
        binding.viewPager.addOnPageChangeListener(object : ViewPager.OnPageChangeListener {
            override fun onPageScrolled(
                position: Int, positionOffset: Float, positionOffsetPixels: Int
            ) {
            }

            override fun onPageSelected(position: Int) {
                if (menuItem != null) {
                    menuItem!!.isChecked = false
                } else {
                    binding.bottomNavigation.menu.getItem(0).isChecked = false
                }
                menuItem = binding.bottomNavigation.menu.getItem(position)
                menuItem!!.isChecked = true
                if (position == 0) {
                    binding.titleView.text = "自动打卡"
                } else {
                    binding.titleView.text = "其他设置"
                }
            }

            override fun onPageScrollStateChanged(state: Int) {}
        })
    }

    override fun observeRequestState() {

    }

    private fun showAlertDialog() {
        AlertMessageDialog.Builder()
            .setContext(this)
            .setTitle("温馨提醒")
            .setMessage("手机没有安装《钉钉》软件，无法自动打卡")
            .setPositiveButton("知道了")
            .setOnDialogButtonClickListener(object :
                AlertMessageDialog.OnDialogButtonClickListener {
                override fun onConfirmClick() {

                }
            }).build().show()
    }

    override fun onKeyDown(keyCode: Int, event: KeyEvent?): Boolean {
        return if (keyCode == KeyEvent.KEYCODE_BACK) {
            if (System.currentTimeMillis() - clickTime > 2000) {
                "再按一次退出应用".show(this)
                clickTime = System.currentTimeMillis()
                true
            } else {
                super.onKeyDown(keyCode, event)
            }
        } else super.onKeyDown(keyCode, event)
    }
}