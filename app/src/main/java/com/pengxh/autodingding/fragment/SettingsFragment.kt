package com.pengxh.autodingding.fragment

import android.content.ComponentName
import android.content.Intent
import android.content.pm.PackageManager
import android.content.pm.ResolveInfo
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.os.Handler
import android.os.Message
import android.provider.Settings
import android.text.TextUtils
import android.view.LayoutInflater
import android.view.ViewGroup
import android.view.WindowManager
import com.pengxh.autodingding.BuildConfig
import com.pengxh.autodingding.R
import com.pengxh.autodingding.databinding.FragmentSettingsBinding
import com.pengxh.autodingding.extensions.initImmersionBar
import com.pengxh.autodingding.extensions.notificationEnable
import com.pengxh.autodingding.service.FloatingWindowService
import com.pengxh.autodingding.service.NotificationMonitorService
import com.pengxh.autodingding.service.SkipConfirmService
import com.pengxh.autodingding.ui.NoticeRecordActivity
import com.pengxh.autodingding.ui.QuestionAndAnswerActivity
import com.pengxh.autodingding.utils.Constant
import com.pengxh.kt.lite.base.KotlinBaseFragment
import com.pengxh.kt.lite.extensions.convertColor
import com.pengxh.kt.lite.extensions.navigatePageTo
import com.pengxh.kt.lite.extensions.setScreenBrightness
import com.pengxh.kt.lite.extensions.show
import com.pengxh.kt.lite.utils.LoadingDialogHub
import com.pengxh.kt.lite.utils.SaveKeyValues
import com.pengxh.kt.lite.utils.WeakReferenceHandler
import com.pengxh.kt.lite.widget.dialog.AlertInputDialog
import com.pengxh.kt.lite.widget.dialog.AlertMessageDialog
import com.pengxh.kt.lite.widget.dialog.BottomActionSheet


class SettingsFragment : KotlinBaseFragment<FragmentSettingsBinding>(), Handler.Callback {

    private val kTag = "SettingsFragment"
    private val timeArray = arrayListOf("15s", "30s", "45s", "60s", "120s")

    companion object {
        var weakReferenceHandler: WeakReferenceHandler? = null
    }

    override fun setupTopBarLayout() {
        binding.rootView.initImmersionBar(this, true, R.color.white)
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

        val emailAddress = SaveKeyValues.getValue(Constant.EMAIL_ADDRESS, "") as String
        if (!TextUtils.isEmpty(emailAddress)) {
            binding.emailTextView.text = emailAddress
        }

        binding.appVersion.text = BuildConfig.VERSION_NAME
    }

    override fun initEvent() {
        binding.emailLayout.setOnClickListener {
            AlertInputDialog.Builder()
                .setContext(requireContext())
                .setTitle("设置邮箱")
                .setHintMessage("请输入邮箱")
                .setNegativeButton("取消")
                .setPositiveButton("确定")
                .setOnDialogButtonClickListener(object :
                    AlertInputDialog.OnDialogButtonClickListener {
                    override fun onConfirmClick(value: String) {
                        if (!TextUtils.isEmpty(value)) {
                            SaveKeyValues.putValue(Constant.EMAIL_ADDRESS, value)
                            binding.emailTextView.text = value
                        } else {
                            "什么都还没输入呢！".show(requireContext())
                        }
                    }

                    override fun onCancelClick() {}
                }).build().show()
        }

        binding.emailTypeRadioGroup.setOnCheckedChangeListener { _, checkedId ->
            when (checkedId) {
                R.id.textEmailView -> SaveKeyValues.putValue(Constant.EMAIL_TYPE, 0)
                R.id.imageEmailView -> SaveKeyValues.putValue(Constant.EMAIL_TYPE, 1)
            }
        }

        binding.timeoutLayout.setOnClickListener {
            BottomActionSheet.Builder()
                .setContext(requireContext())
                .setActionItemTitle(timeArray)
                .setItemTextColor(R.color.colorAppThemeLight.convertColor(requireContext()))
                .setOnActionSheetListener(object : BottomActionSheet.OnActionSheetListener {
                    override fun onActionItemClick(position: Int) {
                        val time = timeArray[position]
                        binding.timeoutTextView.text = time
                        SaveKeyValues.putValue(Constant.TIMEOUT, time)
                    }
                }).build().show()
        }

        binding.floatSwitch.setOnCheckedChangeListener { _, isChecked ->
            if (isChecked) {
                openFloatWindowPermission()
            }
        }

        binding.noticeSwitch.setOnClickListener {
            if (!requireContext().notificationEnable()) {
                startActivity(Intent(Settings.ACTION_NOTIFICATION_LISTENER_SETTINGS))
            }

            if (binding.noticeSwitch.isChecked) {
                LoadingDialogHub.show(requireActivity(), "服务器启动中，请稍后...")
                binding.noticeSwitch.isChecked = false
            } else {
                AlertMessageDialog.Builder()
                    .setContext(requireContext())
                    .setTitle("警告")
                    .setMessage("关闭此服务，将不会监听打卡通知")
                    .setPositiveButton("知道了")
                    .setOnDialogButtonClickListener(object :
                        AlertMessageDialog.OnDialogButtonClickListener {
                        override fun onConfirmClick() {
                            binding.noticeSwitch.isChecked = false
                        }
                    }).build().show()
            }

            val component = ComponentName(requireContext(), NotificationMonitorService::class.java)
            requireContext().packageManager.setComponentEnabledSetting(
                component,
                PackageManager.COMPONENT_ENABLED_STATE_DISABLED,
                PackageManager.DONT_KILL_APP
            )

            requireContext().packageManager.setComponentEnabledSetting(
                component,
                PackageManager.COMPONENT_ENABLED_STATE_ENABLED,
                PackageManager.DONT_KILL_APP
            )
        }

        binding.autoServiceSwitch.setOnClickListener {
            startActivity(Intent(Settings.ACTION_ACCESSIBILITY_SETTINGS))
        }

        binding.openTestLayout.setOnClickListener {
            val packageManager = requireContext().packageManager
            val resolveIntent = Intent(Intent.ACTION_MAIN, null)
            resolveIntent.addCategory(Intent.CATEGORY_LAUNCHER)
            resolveIntent.setPackage(Constant.DING_DING)
            val apps = packageManager.queryIntentActivities(resolveIntent, 0)
            val iterator: Iterator<ResolveInfo> = apps.iterator()
            if (!iterator.hasNext()) {
                return@setOnClickListener
            }
            val resolveInfo = iterator.next()
            val intent = Intent(Intent.ACTION_MAIN)
            intent.addCategory(Intent.CATEGORY_LAUNCHER)
            intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
            intent.component = ComponentName(
                resolveInfo.activityInfo.packageName, resolveInfo.activityInfo.name
            )
            startActivity(intent)
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

        binding.exchangeProVersionSwitch.setOnClickListener {
            "未实现".show(requireContext())
//            AlertControlDialog.Builder()
//                .setContext(requireContext())
//                .setTitle("温馨提示")
//                .setMessage("版本切换将会重新启动应用，且Pro版本和普通版本功能不互通，是否继续？")
//                .setNegativeButton("取消")
//                .setPositiveButton("确定")
//                .setOnDialogButtonClickListener(
//                    object : AlertControlDialog.OnDialogButtonClickListener {
//                        override fun onConfirmClick() {
//                            SaveKeyValues.putValue(
//                                Constant.CHANGE_VERSION, binding.exchangeProVersionSwitch.isChecked
//                            )
//                            //退出应用
//                            ActivityStackManager.finishAllActivity()
//                            requireContext().navigatePageTo<MainActivity>()
//                        }
//
//                        override fun onCancelClick() {
//
//                        }
//                    }
//                ).build().show()
        }

        binding.notificationLayout.setOnClickListener {
            requireContext().navigatePageTo<NoticeRecordActivity>()
        }

        binding.introduceLayout.setOnClickListener {
            requireContext().navigatePageTo<QuestionAndAnswerActivity>()
        }
    }

    override fun handleMessage(msg: Message): Boolean {
        when (msg.what) {
            2024060601 -> {
                try {
                    LoadingDialogHub.dismiss()
                } catch (e: UninitializedPropertyAccessException) {
                    e.printStackTrace()
                }
                binding.noticeSwitch.isChecked = true
            }

            2024060602 -> {
                binding.noticeSwitch.isChecked = false
            }
        }
        return true
    }

    private fun openFloatWindowPermission() {
        if (!Settings.canDrawOverlays(requireContext())) {
            val sdkInt = Build.VERSION.SDK_INT
            if (sdkInt >= Build.VERSION_CODES.O) {
                startActivity(Intent(Settings.ACTION_MANAGE_OVERLAY_PERMISSION))
            } else if (sdkInt >= Build.VERSION_CODES.M) {
                val intent = Intent(Settings.ACTION_MANAGE_OVERLAY_PERMISSION)
                intent.data = Uri.parse("package:${requireContext().packageName}")
                startActivity(intent)
            }
        }
    }

    override fun onResume() {
        super.onResume()
        val type = SaveKeyValues.getValue(Constant.EMAIL_TYPE, 0) as Int
        if (type == 0) {
            binding.textEmailView.isChecked = true
        } else {
            binding.imageEmailView.isChecked = true
        }

        binding.timeoutTextView.text = SaveKeyValues.getValue(Constant.TIMEOUT, "15s") as String

        binding.floatSwitch.isChecked = Settings.canDrawOverlays(requireContext())
        if (binding.floatSwitch.isChecked) {
            requireContext().startService(
                Intent(requireContext(), FloatingWindowService::class.java)
            )
        }

        val b = SaveKeyValues.getValue(Constant.CHANGE_VERSION, false) as Boolean
        if (b) {
            binding.versionTipsView.text = "切换普通版"
        } else {
            binding.versionTipsView.text = "切换Pro版"
        }
        binding.exchangeProVersionSwitch.isChecked = b

        binding.autoServiceSwitch.isChecked = SkipConfirmService.isServiceRunning
    }
}