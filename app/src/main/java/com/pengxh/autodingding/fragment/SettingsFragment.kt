package com.pengxh.autodingding.fragment

import android.content.ComponentName
import android.content.Intent
import android.content.pm.PackageManager
import android.content.res.ColorStateList
import android.graphics.Color
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.os.Handler
import android.os.Message
import android.provider.Settings
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.WindowManager
import androidx.lifecycle.lifecycleScope
import com.pengxh.autodingding.BuildConfig
import com.pengxh.autodingding.R
import com.pengxh.autodingding.databinding.FragmentSettingsBinding
import com.pengxh.autodingding.extensions.notificationEnable
import com.pengxh.autodingding.extensions.openApplication
import com.pengxh.autodingding.service.FloatingWindowService
import com.pengxh.autodingding.service.NotificationMonitorService
import com.pengxh.autodingding.ui.EmailConfigActivity
import com.pengxh.autodingding.ui.NoticeRecordActivity
import com.pengxh.autodingding.ui.QuestionAndAnswerActivity
import com.pengxh.autodingding.ui.TaskConfigActivity
import com.pengxh.autodingding.utils.Constant
import com.pengxh.autodingding.utils.KeyValueKit
import com.pengxh.kt.lite.base.KotlinBaseFragment
import com.pengxh.kt.lite.extensions.convertColor
import com.pengxh.kt.lite.extensions.navigatePageTo
import com.pengxh.kt.lite.extensions.setScreenBrightness
import com.pengxh.kt.lite.extensions.show
import com.pengxh.kt.lite.utils.SaveKeyValues
import com.pengxh.kt.lite.utils.WeakReferenceHandler
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch


class SettingsFragment : KotlinBaseFragment<FragmentSettingsBinding>(), Handler.Callback {

    companion object {
        var weakReferenceHandler: WeakReferenceHandler? = null
    }

    override fun setupTopBarLayout() {

    }

    override fun observeRequestState() {

    }

    override fun initViewBinding(
        inflater: LayoutInflater, container: ViewGroup?
    ): FragmentSettingsBinding {
        return FragmentSettingsBinding.inflate(inflater, container, false)
    }

    override fun initOnCreate(savedInstanceState: Bundle?) {
        weakReferenceHandler = WeakReferenceHandler(this)
        binding.appVersion.text = BuildConfig.VERSION_NAME
        if (requireContext().notificationEnable()) {
            turnOnNotificationMonitorService()
        }
    }

    override fun initEvent() {
        binding.emailConfigLayout.setOnClickListener {
            requireContext().navigatePageTo<EmailConfigActivity>()
        }

        binding.taskConfigLayout.setOnClickListener {
            requireContext().navigatePageTo<TaskConfigActivity>()
        }

        binding.floatSwitch.setOnClickListener {
            val sdkInt = Build.VERSION.SDK_INT
            if (sdkInt >= Build.VERSION_CODES.M) {
                //6.0+
                if (sdkInt >= Build.VERSION_CODES.O) {
                    //8.0+
                    val intent = Intent(Settings.ACTION_MANAGE_OVERLAY_PERMISSION)
                    startActivityForResult(intent, 101)
                } else {
                    val intent = Intent(Settings.ACTION_MANAGE_OVERLAY_PERMISSION)
                    intent.data = Uri.parse("package:${requireContext().packageName}")
                    startActivityForResult(intent, 101)
                }
            } else {
                "手机系统版本太低".show(requireContext())
            }
        }

        binding.noticeSwitch.setOnClickListener {
            startActivityForResult(Intent(Settings.ACTION_NOTIFICATION_LISTENER_SETTINGS), 100)
        }

        binding.openTestLayout.setOnClickListener {
            requireContext().openApplication(Constant.DING_DING, false)
        }

        binding.turnoffLightSwitch.setOnCheckedChangeListener { _, isChecked ->
            if (isChecked) {
                //最低亮度
                requireActivity().window.setScreenBrightness(WindowManager.LayoutParams.BRIGHTNESS_OVERRIDE_OFF)
            } else {
                //恢复默认亮度
                requireActivity().window.setScreenBrightness(WindowManager.LayoutParams.BRIGHTNESS_OVERRIDE_NONE)
            }
        }

        binding.backToHomeSwitch.setOnCheckedChangeListener { _, isChecked ->
            SaveKeyValues.putValue(Constant.BACK_TO_HOME, isChecked)
        }

        binding.notificationLayout.setOnClickListener {
            requireContext().navigatePageTo<NoticeRecordActivity>()
        }

        binding.introduceLayout.setOnClickListener {
            requireContext().navigatePageTo<QuestionAndAnswerActivity>()
        }
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (requestCode == 100) {
            if (requireContext().notificationEnable()) {
                turnOnNotificationMonitorService()
            }
        } else if (requestCode == 101) {
            binding.floatSwitch.isChecked = Settings.canDrawOverlays(requireContext())
        }
    }

    override fun handleMessage(msg: Message): Boolean {
        if (msg.what == Constant.NOTICE_LISTENER_CONNECTED_CODE) {
            binding.noticeSwitch.isChecked = true
            binding.tipsView.visibility = View.GONE
        } else if (msg.what == Constant.NOTICE_LISTENER_DISCONNECTED_CODE) {
            binding.noticeSwitch.isChecked = false
            binding.tipsView.visibility = View.VISIBLE
        }
        return true
    }

    override fun onResume() {
        super.onResume()
        val emailAddress = KeyValueKit.getEmailAddress()
        if (emailAddress == "") {
            binding.emailTagView.backgroundTintList = ColorStateList.valueOf(Color.RED)
        } else {
            binding.emailTagView.backgroundTintList =
                ColorStateList.valueOf(R.color.iOSGreen.convertColor(requireContext()))
        }

        binding.floatSwitch.isChecked = Settings.canDrawOverlays(requireContext())
        val serviceIntent = Intent(requireContext(), FloatingWindowService::class.java)
        if (binding.floatSwitch.isChecked) {
            requireContext().startService(serviceIntent)
        } else {
            requireContext().stopService(serviceIntent)
        }

        binding.backToHomeSwitch.isChecked = SaveKeyValues.getValue(
            Constant.BACK_TO_HOME, false
        ) as Boolean

        if (requireContext().notificationEnable()) {
            binding.tipsView.text = "通知监听服务状态查询中，请稍后"
            binding.tipsView.setTextColor(R.color.purple_500.convertColor(requireContext()))
        } else {
            binding.tipsView.text = "通知监听服务未开启，无法监听打卡通知"
            binding.tipsView.setTextColor(R.color.red.convertColor(requireContext()))
        }
    }

    private fun turnOnNotificationMonitorService() {
        lifecycleScope.launch(Dispatchers.IO) {
            requireContext().packageManager.setComponentEnabledSetting(
                ComponentName(requireContext(), NotificationMonitorService::class.java),
                PackageManager.COMPONENT_ENABLED_STATE_DISABLED, PackageManager.DONT_KILL_APP
            )

            delay(1000)

            requireContext().packageManager.setComponentEnabledSetting(
                ComponentName(requireContext(), NotificationMonitorService::class.java),
                PackageManager.COMPONENT_ENABLED_STATE_ENABLED, PackageManager.DONT_KILL_APP
            )
        }
    }
}