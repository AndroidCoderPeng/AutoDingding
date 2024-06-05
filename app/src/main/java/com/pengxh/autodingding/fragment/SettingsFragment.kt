package com.pengxh.autodingding.fragment

import android.app.Notification
import android.app.NotificationChannel
import android.app.NotificationManager
import android.content.Context
import android.content.Intent
import android.graphics.BitmapFactory
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.provider.Settings
import android.text.TextUtils
import android.view.LayoutInflater
import android.view.ViewGroup
import android.view.WindowManager
import com.pengxh.autodingding.BuildConfig
import com.pengxh.autodingding.R
import com.pengxh.autodingding.databinding.FragmentSettingsBinding
import com.pengxh.autodingding.extensions.notificationEnable
import com.pengxh.autodingding.service.AutoSignInService
import com.pengxh.autodingding.service.FloatingWindowService
import com.pengxh.autodingding.ui.NoticeRecordActivity
import com.pengxh.autodingding.utils.Constant
import com.pengxh.kt.lite.base.KotlinBaseFragment
import com.pengxh.kt.lite.extensions.navigatePageTo
import com.pengxh.kt.lite.extensions.setScreenBrightness
import com.pengxh.kt.lite.extensions.show
import com.pengxh.kt.lite.utils.SaveKeyValues
import com.pengxh.kt.lite.widget.dialog.AlertInputDialog
import com.pengxh.kt.lite.widget.dialog.AlertMessageDialog

class SettingsFragment : KotlinBaseFragment<FragmentSettingsBinding>() {

    private val kTag = "SettingsFragment"
    private val notificationManager by lazy { requireContext().getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager }

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

        binding.radioGroup.setOnCheckedChangeListener { _, checkedId ->
            if (checkedId == binding.allNoticeRadioButton.id) {
                SaveKeyValues.putValue(Constant.NOTICE_TYPE, 0)
            } else {
                SaveKeyValues.putValue(Constant.NOTICE_TYPE, 1)
            }
        }

        binding.floatSwitch.setOnCheckedChangeListener { _, isChecked ->
            if (isChecked) {
                openFloatWindowPermission()
            }
        }

        binding.noticeSwitch.setOnClickListener {
            startActivity(Intent(Settings.ACTION_NOTIFICATION_LISTENER_SETTINGS))
        }

        binding.autoServiceSwitch.setOnClickListener {
            startActivity(Intent(Settings.ACTION_ACCESSIBILITY_SETTINGS))
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

        binding.notificationLayout.setOnClickListener {
            requireContext().navigatePageTo<NoticeRecordActivity>()
        }

        binding.introduceLayout.setOnClickListener {
            AlertMessageDialog.Builder()
                .setContext(requireContext())
                .setTitle("功能介绍")
                .setMessage(requireContext().getString(R.string.about))
                .setPositiveButton("看完了")
                .setOnDialogButtonClickListener(
                    object : AlertMessageDialog.OnDialogButtonClickListener {
                        override fun onConfirmClick() {

                        }
                    }
                ).build().show()
        }
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
        val type = SaveKeyValues.getValue(Constant.NOTICE_TYPE, 1) as Int
        if (type == 1) {
            binding.dingNoticeRadioButton.isChecked = true
        } else {
            binding.allNoticeRadioButton.isChecked = true
        }

        binding.floatSwitch.isChecked = Settings.canDrawOverlays(requireContext())
        if (binding.floatSwitch.isChecked) {
            requireContext().startService(
                Intent(requireContext(), FloatingWindowService::class.java)
            )
        }

        if (requireContext().notificationEnable()) {
            binding.noticeSwitch.isChecked = true
            createNotification()
        } else {
            //取消通知栏
            notificationManager.cancel(Int.MAX_VALUE)
        }

        binding.autoServiceSwitch.isChecked = AutoSignInService.isServiceRunning
    }

    private fun createNotification() {
        //Android8.0以上必须添加 渠道 才能显示通知栏
        val builder = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            //创建渠道
            val name = resources.getString(R.string.app_name)
            val id = name + "_DefaultNotificationChannel"
            val mChannel = NotificationChannel(id, name, NotificationManager.IMPORTANCE_DEFAULT)
            mChannel.setShowBadge(true)
            mChannel.enableVibration(false)
            mChannel.lockscreenVisibility = Notification.VISIBILITY_PUBLIC //设置锁屏可见
            notificationManager.createNotificationChannel(mChannel)
            Notification.Builder(requireContext(), id)
        } else {
            Notification.Builder(requireContext())
        }
        val bitmap = BitmapFactory.decodeResource(resources, R.mipmap.logo_round)
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