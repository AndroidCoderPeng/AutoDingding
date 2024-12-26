package com.pengxh.daily.app.ui

import android.os.Bundle
import com.pengxh.daily.app.R
import com.pengxh.daily.app.databinding.ActivityTaskConfigBinding
import com.pengxh.daily.app.extensions.initImmersionBar
import com.pengxh.daily.app.service.FloatingWindowService
import com.pengxh.daily.app.utils.Constant
import com.pengxh.kt.lite.base.KotlinBaseActivity
import com.pengxh.kt.lite.extensions.convertColor
import com.pengxh.kt.lite.utils.SaveKeyValues
import com.pengxh.kt.lite.widget.TitleBarView
import com.pengxh.kt.lite.widget.dialog.AlertInputDialog
import com.pengxh.kt.lite.widget.dialog.BottomActionSheet

class TaskConfigActivity : KotlinBaseActivity<ActivityTaskConfigBinding>() {

    private val timeArray = arrayListOf("15s", "30s", "45s")

    override fun initOnCreate(savedInstanceState: Bundle?) {
        binding.timeoutTextView.text = SaveKeyValues.getValue(Constant.STAY_DD_TIMEOUT_KEY, "45s") as String
        binding.keyTextView.text = SaveKeyValues.getValue(Constant.DING_DING_KEY, "打卡") as String
        binding.skipHolidaySwitch.isChecked = SaveKeyValues.getValue(
            Constant.SKIP_HOLIDAY_KEY, true
        ) as Boolean
    }

    override fun initViewBinding(): ActivityTaskConfigBinding {
        return ActivityTaskConfigBinding.inflate(layoutInflater)
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
        binding.timeoutLayout.setOnClickListener {
            BottomActionSheet.Builder()
                .setContext(this)
                .setActionItemTitle(timeArray)
                .setItemTextColor(R.color.colorAppThemeLight.convertColor(this))
                .setOnActionSheetListener(object : BottomActionSheet.OnActionSheetListener {
                    override fun onActionItemClick(position: Int) {
                        val time = timeArray[position]
                        binding.timeoutTextView.text = time
                        SaveKeyValues.putValue(Constant.STAY_DD_TIMEOUT_KEY, time)

                        FloatingWindowService.weakReferenceHandler?.apply {
                            val message = obtainMessage()
                            message.what = Constant.UPDATE_TICK_TIME_CODE
                            message.obj = time
                            sendMessage(message)
                        }
                    }
                }).build().show()
        }

        binding.keyLayout.setOnClickListener {
            AlertInputDialog.Builder()
                .setContext(this)
                .setTitle("设置打卡口令")
                .setHintMessage("请输入打卡口令，如：打卡")
                .setNegativeButton("取消")
                .setPositiveButton("确定")
                .setOnDialogButtonClickListener(object :
                    AlertInputDialog.OnDialogButtonClickListener {
                    override fun onConfirmClick(value: String) {
                        SaveKeyValues.putValue(Constant.DING_DING_KEY, value)
                        binding.keyTextView.text = value
                    }

                    override fun onCancelClick() {}
                }).build().show()
        }

        binding.skipHolidaySwitch.setOnCheckedChangeListener { _, isChecked ->
            SaveKeyValues.putValue(Constant.SKIP_HOLIDAY_KEY, isChecked)
        }
    }
}