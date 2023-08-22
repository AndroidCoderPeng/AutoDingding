package com.pengxh.autodingding.fragment

import android.app.Notification
import android.app.NotificationChannel
import android.app.NotificationManager
import android.content.Context
import android.content.Intent
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.provider.Settings
import android.text.TextUtils
import com.pengxh.autodingding.BaseApplication
import com.pengxh.autodingding.BuildConfig
import com.pengxh.autodingding.R
import com.pengxh.autodingding.extensions.notificationEnable
import com.pengxh.autodingding.ui.HistoryRecordActivity
import com.pengxh.autodingding.ui.NoticeRecordActivity
import com.pengxh.autodingding.utils.Constant
import com.pengxh.kt.lite.base.KotlinBaseFragment
import com.pengxh.kt.lite.extensions.navigatePageTo
import com.pengxh.kt.lite.extensions.show
import com.pengxh.kt.lite.utils.SaveKeyValues
import com.pengxh.kt.lite.widget.dialog.AlertInputDialog
import com.pengxh.kt.lite.widget.dialog.AlertMessageDialog
import kotlinx.android.synthetic.main.fragment_settings.appVersion
import kotlinx.android.synthetic.main.fragment_settings.emailLayout
import kotlinx.android.synthetic.main.fragment_settings.emailTextView
import kotlinx.android.synthetic.main.fragment_settings.floatCheckBox
import kotlinx.android.synthetic.main.fragment_settings.historyLayout
import kotlinx.android.synthetic.main.fragment_settings.introduceLayout
import kotlinx.android.synthetic.main.fragment_settings.noticeCheckBox
import kotlinx.android.synthetic.main.fragment_settings.notificationLayout
import kotlinx.android.synthetic.main.fragment_settings.recordSize
import kotlinx.android.synthetic.main.fragment_settings.updateCodeView

class SettingsFragment : KotlinBaseFragment() {

    private val kTag = "SettingsFragment"
    private val notificationManager by lazy { requireContext().getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager }
    private val historyBeanDao by lazy { BaseApplication.get().daoSession.historyRecordBeanDao }

    override fun setupTopBarLayout() {

    }

    override fun observeRequestState() {

    }

    override fun initLayoutView(): Int = R.layout.fragment_settings

    override fun initData(savedInstanceState: Bundle?) {
        val emailAddress = SaveKeyValues.getValue(Constant.EMAIL_ADDRESS, "") as String
        if (!TextUtils.isEmpty(emailAddress)) {
            emailTextView.text = emailAddress
        }

        appVersion.text = BuildConfig.VERSION_NAME
    }

    override fun initEvent() {
        emailLayout.setOnClickListener {
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
                            emailTextView.text = value
                        } else {
                            "什么都还没输入呢！".show(requireContext())
                        }
                    }

                    override fun onCancelClick() {}
                }).build().show()
        }

        historyLayout.setOnClickListener {
            requireContext().navigatePageTo<HistoryRecordActivity>()
        }

        floatCheckBox.setOnClickListener {
            val sdkInt = Build.VERSION.SDK_INT
            if (sdkInt >= Build.VERSION_CODES.O) {
                startActivity(Intent(Settings.ACTION_MANAGE_OVERLAY_PERMISSION))
            } else if (sdkInt >= Build.VERSION_CODES.M) {
                val intent = Intent(Settings.ACTION_MANAGE_OVERLAY_PERMISSION)
                intent.data = Uri.parse("package:${requireContext().packageName}")
                startActivity(intent)
            }
        }

        noticeCheckBox.setOnClickListener {
            startActivity(Intent(Settings.ACTION_NOTIFICATION_LISTENER_SETTINGS))
        }

        notificationLayout.setOnClickListener {
            requireContext().navigatePageTo<NoticeRecordActivity>()
        }

        introduceLayout.setOnClickListener {
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

        updateCodeView.setOnLongClickListener {
            val updateLink = "https://www.pgyer.com/MBGt"
            AlertMessageDialog.Builder()
                .setContext(requireContext())
                .setTitle("识别结果")
                .setMessage(updateLink)
                .setPositiveButton("前往更新页面(密码：123)")
                .setOnDialogButtonClickListener(object :
                    AlertMessageDialog.OnDialogButtonClickListener {
                    override fun onConfirmClick() {
                        val intent = Intent()
                        intent.action = "android.intent.action.VIEW"
                        intent.data = Uri.parse(updateLink)
                        startActivity(intent)
                    }
                }).build().show()
            true
        }
    }


    /**
     * 每次切换到此页面都需要重新计算记录
     */
    override fun onResume() {
        super.onResume()
        recordSize.text = historyBeanDao.loadAll().size.toString()

        floatCheckBox.isChecked = Settings.canDrawOverlays(requireContext())

        if (requireContext().notificationEnable()) {
            noticeCheckBox.isChecked = true
            createNotification()
        } else {
            //取消通知栏
            notificationManager.cancel(Int.MAX_VALUE)
        }
    }

    private fun createNotification() {
        //Android8.0以上必须添加 渠道 才能显示通知栏
        val builder: Notification.Builder = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
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