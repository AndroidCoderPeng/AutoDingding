package com.pengxh.autodingding.ui

import android.os.Bundle
import android.text.TextUtils
import com.pengxh.autodingding.R
import com.pengxh.autodingding.databinding.ActivityEmailConfigBinding
import com.pengxh.autodingding.extensions.initImmersionBar
import com.pengxh.autodingding.extensions.show
import com.pengxh.autodingding.utils.Constant
import com.pengxh.kt.lite.base.KotlinBaseActivity
import com.pengxh.kt.lite.utils.SaveKeyValues
import com.pengxh.kt.lite.widget.TitleBarView
import com.pengxh.kt.lite.widget.dialog.AlertInputDialog

class EmailConfigActivity : KotlinBaseActivity<ActivityEmailConfigBinding>() {

    private val context = this

    override fun initOnCreate(savedInstanceState: Bundle?) {
        binding.emailTextView.text = SaveKeyValues.getValue(Constant.EMAIL_ADDRESS, "") as String
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
                        if (!TextUtils.isEmpty(value)) {
                            SaveKeyValues.putValue(Constant.EMAIL_ADDRESS, value)
                            binding.emailTextView.text = value
                        } else {
                            "什么都还没输入呢！".show(context)
                        }
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
                        if (!TextUtils.isEmpty(value)) {
                            SaveKeyValues.putValue(Constant.EMAIL_TITLE, value)
                            binding.emailTitleView.text = value
                        } else {
                            "什么都还没输入呢！".show(context)
                        }
                    }

                    override fun onCancelClick() {}
                }).build().show()
        }
    }
}