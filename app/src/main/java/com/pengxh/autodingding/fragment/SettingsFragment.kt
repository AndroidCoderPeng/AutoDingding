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
import android.view.View
import android.view.ViewGroup
import android.view.WindowManager
import androidx.lifecycle.lifecycleScope
import com.pengxh.autodingding.BuildConfig
import com.pengxh.autodingding.R
import com.pengxh.autodingding.databinding.FragmentSettingsBinding
import com.pengxh.autodingding.extensions.notificationEnable
import com.pengxh.autodingding.extensions.show
import com.pengxh.autodingding.service.FloatingWindowService
import com.pengxh.autodingding.service.NotificationMonitorService
import com.pengxh.autodingding.ui.NoticeRecordActivity
import com.pengxh.autodingding.ui.QuestionAndAnswerActivity
import com.pengxh.autodingding.utils.Constant
import com.pengxh.kt.lite.base.KotlinBaseFragment
import com.pengxh.kt.lite.extensions.convertColor
import com.pengxh.kt.lite.extensions.navigatePageTo
import com.pengxh.kt.lite.extensions.setScreenBrightness
import com.pengxh.kt.lite.utils.SaveKeyValues
import com.pengxh.kt.lite.utils.WeakReferenceHandler
import com.pengxh.kt.lite.widget.dialog.AlertInputDialog
import com.pengxh.kt.lite.widget.dialog.BottomActionSheet
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch


class SettingsFragment : KotlinBaseFragment<FragmentSettingsBinding>(), Handler.Callback {

    companion object {
        var weakReferenceHandler: WeakReferenceHandler? = null
    }

    private val kTag = "SettingsFragment"
    private val timeArray = arrayListOf("15s", "30s", "45s", "60s")

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

        binding.emailTitleLayout.setOnClickListener {
            AlertInputDialog.Builder()
                .setContext(requireContext())
                .setTitle("设置邮件标题")
                .setHintMessage("请输入邮件标题")
                .setNegativeButton("取消")
                .setPositiveButton("确定")
                .setOnDialogButtonClickListener(object :
                    AlertInputDialog.OnDialogButtonClickListener {
                    override fun onConfirmClick(value: String) {
                        if (!TextUtils.isEmpty(value)) {
                            SaveKeyValues.putValue(Constant.EMAIL_TITLE, value)
                            binding.emailTitleView.text = value
                        } else {
                            "什么都还没输入呢！".show(requireContext())
                        }
                    }

                    override fun onCancelClick() {}
                }).build().show()
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

                        val handler = FloatingWindowService.weakReferenceHandler ?: return
                        val message = handler.obtainMessage()
                        message.what = 2024071702
                        message.obj = time
                        handler.sendMessage(message)
                    }
                }).build().show()
        }

        binding.keyLayout.setOnClickListener {
            AlertInputDialog.Builder()
                .setContext(requireContext())
                .setTitle("设置打卡口令")
                .setHintMessage("请输入打卡口令，如：打卡")
                .setNegativeButton("取消")
                .setPositiveButton("确定")
                .setOnDialogButtonClickListener(object :
                    AlertInputDialog.OnDialogButtonClickListener {
                    override fun onConfirmClick(value: String) {
                        if (!TextUtils.isEmpty(value)) {
                            SaveKeyValues.putValue(Constant.DING_DING_KEY, value)
                            binding.keyTextView.text = value
                        } else {
                            "什么都还没输入呢！".show(requireContext())
                        }
                    }

                    override fun onCancelClick() {}
                }).build().show()
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
        if (msg.what == 2024090801) {
            binding.noticeSwitch.isChecked = true
            binding.tipsView.visibility = View.GONE
        } else if (msg.what == 2024090802) {
            binding.noticeSwitch.isChecked = false
            binding.tipsView.visibility = View.VISIBLE
        }
        return true
    }

    override fun onResume() {
        super.onResume()
        binding.emailTextView.text = SaveKeyValues.getValue(Constant.EMAIL_ADDRESS, "") as String
        binding.emailTitleView.text = SaveKeyValues.getValue(
            Constant.EMAIL_TITLE, "打卡结果通知"
        ) as String
        binding.timeoutTextView.text = SaveKeyValues.getValue(Constant.TIMEOUT, "15s") as String
        binding.keyTextView.text = SaveKeyValues.getValue(Constant.DING_DING_KEY, "打卡") as String

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
        lifecycleScope.launch(Dispatchers.IO){
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