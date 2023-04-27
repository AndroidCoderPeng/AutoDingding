package com.pengxh.autodingding.ui

import android.app.Notification
import android.app.NotificationChannel
import android.app.NotificationManager
import android.content.Context
import android.content.Intent
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.os.Build
import android.provider.Settings
import android.view.MenuItem
import androidx.fragment.app.Fragment
import androidx.viewpager.widget.ViewPager
import com.gyf.immersionbar.ImmersionBar
import com.pengxh.autodingding.R
import com.pengxh.autodingding.adapter.BaseFragmentAdapter
import com.pengxh.autodingding.extensions.isAppAvailable
import com.pengxh.autodingding.extensions.notificationEnable
import com.pengxh.autodingding.fragment.DingDingFragment
import com.pengxh.autodingding.fragment.SettingsFragment
import com.pengxh.autodingding.utils.Constant
import com.pengxh.kt.lite.base.KotlinBaseActivity
import com.pengxh.kt.lite.extensions.convertColor
import com.pengxh.kt.lite.utils.ImmerseStatusBarUtil
import com.pengxh.kt.lite.utils.SaveKeyValues
import com.pengxh.kt.lite.widget.dialog.AlertMessageDialog
import kotlinx.android.synthetic.main.activity_main.*

class MainActivity : KotlinBaseActivity() {

    private var menuItem: MenuItem? = null
    private val fragmentPages: MutableList<Fragment> = ArrayList()
    private val notificationManager by lazy { getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager }

    override fun setupTopBarLayout() {
        ImmerseStatusBarUtil.setColor(
            this, R.color.colorAppThemeLight.convertColor(this)
        )
        ImmersionBar.with(this).statusBarDarkFont(false).init()
        titleView.text = "钉钉打卡"
    }

    override fun initData() {
        if (!notificationEnable()) {
            try {
                //打开通知监听设置页面
                startActivity(Intent(Settings.ACTION_NOTIFICATION_LISTENER_SETTINGS))
            } catch (e: Exception) {
                e.printStackTrace()
            }
        } else {
            //创建常住通知栏
            createNotification()
        }

        fragmentPages.add(DingDingFragment())
        fragmentPages.add(SettingsFragment())

        if (!isAppAvailable(Constant.DING_DING)) {
            showAlertDialog("手机没有安装《钉钉》软件，无法自动打卡")
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
    }

    override fun initEvent() {
        bottomNavigation.setOnItemSelectedListener { item ->
            val itemId: Int = item.itemId
            if (itemId == R.id.nav_dingding) {
                if (isAppAvailable(Constant.DING_DING)) {
                    viewPager.currentItem = 0
                } else {
                    showAlertDialog("手机没有安装《钉钉》软件，无法自动打卡")
                }
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
                position: Int, positionOffset: Float, positionOffsetPixels: Int
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

    private fun createNotification() {
        //Android8.0以上必须添加 渠道 才能显示通知栏
        val builder: Notification.Builder = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            //创建渠道
            val name = resources.getString(R.string.app_name)
            val id = name + "_DefaultNotificationChannel"
            val mChannel = NotificationChannel(id, name, NotificationManager.IMPORTANCE_DEFAULT)
            mChannel.setShowBadge(true)
            mChannel.enableVibration(true)
            mChannel.vibrationPattern = longArrayOf(100, 200, 300)
            mChannel.lockscreenVisibility = Notification.VISIBILITY_PUBLIC //设置锁屏可见
            notificationManager.createNotificationChannel(mChannel)
            Notification.Builder(this, id)
        } else {
            Notification.Builder(this)
        }
        val bitmap: Bitmap =
            BitmapFactory.decodeResource(resources, R.mipmap.logo_round)
        builder.setContentTitle("钉钉打卡通知监听已打开")
            .setContentText("如果通知消失，请重新开启应用")
            .setWhen(System.currentTimeMillis())
            .setLargeIcon(bitmap)
            .setSmallIcon(R.mipmap.logo_round)
            .setAutoCancel(false)
        val notification = builder.build()
        notification.flags = Notification.FLAG_NO_CLEAR
        notificationManager.notify(Int.MAX_VALUE, notification)
    }
}