package com.pengxh.autodingding.fragment

import android.content.Intent
import android.net.Uri
import android.text.TextUtils
import cn.bertsir.zbar.utils.QRUtils
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
import kotlinx.android.synthetic.main.fragment_settings.*

class SettingsFragment : KotlinBaseFragment() {

    private val historyBeanDao by lazy { BaseApplication.get().daoSession.historyRecordBeanDao }

    override fun setupTopBarLayout() {

    }

    override fun observeRequestState() {

    }

    override fun initLayoutView(): Int = R.layout.fragment_settings

    override fun initData() {
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

        //先识别出来备用
        try {
            val codeValue = QRUtils.getInstance().decodeQRcode(updateCodeView)
            SaveKeyValues.putValue("updateLink", codeValue)
        } catch (e: Exception) {
            e.printStackTrace()
        }
        updateCodeView.setOnLongClickListener {
            val updateLink =
                SaveKeyValues.getValue("updateLink", "https://www.pgyer.com/MBGt") as String
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
        noticeCheckBox.isChecked = requireContext().notificationEnable()
        super.onResume()
    }
}