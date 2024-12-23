package com.pengxh.autodingding.ui

import android.os.Bundle
import com.pengxh.autodingding.R
import com.pengxh.autodingding.databinding.ActivityEmailConfigBinding
import com.pengxh.autodingding.extensions.createTextMail
import com.pengxh.autodingding.extensions.initImmersionBar
import com.pengxh.autodingding.extensions.sendTextMail
import com.pengxh.autodingding.utils.Constant
import com.pengxh.autodingding.utils.KeyValueKit
import com.pengxh.kt.lite.base.KotlinBaseActivity
import com.pengxh.kt.lite.extensions.isEmail
import com.pengxh.kt.lite.extensions.show
import com.pengxh.kt.lite.utils.SaveKeyValues
import com.pengxh.kt.lite.widget.TitleBarView
import com.pengxh.kt.lite.widget.dialog.AlertInputDialog

class EmailConfigActivity : KotlinBaseActivity<ActivityEmailConfigBinding>() {

    private val context = this

    override fun initOnCreate(savedInstanceState: Bundle?) {
        binding.emailTextView.text = KeyValueKit.getEmailAddress()
        binding.emailTitleView.text = SaveKeyValues.getValue(
            Constant.EMAIL_TITLE, "打卡结果通知"
        ) as String
    }

    override fun initViewBinding(): ActivityEmailConfigBinding {
        return ActivityEmailConfigBinding.inflate(layoutInflater)
    }

    override fun observeRequestState() {

    }

    override fun setupTopBarLayout() {
        binding.rootView.initImmersionBar(this, true, R.color.white)
        binding.titleView.setOnClickListener(object : TitleBarView.OnClickListener {
            override fun onLeftClick() {
                finish()
            }

            override fun onRightClick() {

            }
        })
    }

    override fun initEvent() {
        binding.emailLayout.setOnClickListener {
            AlertInputDialog.Builder()
                .setContext(this)
                .setTitle("设置邮箱")
                .setHintMessage("请输入邮箱")
                .setNegativeButton("取消")
                .setPositiveButton("确定")
                .setOnDialogButtonClickListener(object :
                    AlertInputDialog.OnDialogButtonClickListener {
                    override fun onConfirmClick(value: String) {
                        if (!value.isEmail()) {
                            "输入的邮箱格式不对，请检查".show(context)
                            return
                        }

                        SaveKeyValues.putValue(Constant.EMAIL_ADDRESS, value)
                        binding.emailTextView.text = value
                    }

                    override fun onCancelClick() {}
                }).build().show()
        }

        binding.emailTitleLayout.setOnClickListener {
            AlertInputDialog.Builder()
                .setContext(this)
                .setTitle("设置邮件标题")
                .setHintMessage("请输入邮件标题")
                .setNegativeButton("取消")
                .setPositiveButton("确定")
                .setOnDialogButtonClickListener(object :
                    AlertInputDialog.OnDialogButtonClickListener {
                    override fun onConfirmClick(value: String) {
                        SaveKeyValues.putValue(Constant.EMAIL_TITLE, value)
                        binding.emailTitleView.text = value
                    }

                    override fun onCancelClick() {}
                }).build().show()
        }

        binding.sendEmailButton.setOnClickListener {
            val emailAddress = KeyValueKit.getEmailAddress()
            if (emailAddress.isEmpty()) {
                "邮箱地址为空".show(context)
                return@setOnClickListener
            }

            "这是一封测试邮件，不必关注".createTextMail("邮箱测试", emailAddress).sendTextMail()
        }
    }
}