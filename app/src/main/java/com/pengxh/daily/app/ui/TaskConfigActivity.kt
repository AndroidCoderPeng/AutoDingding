package com.pengxh.daily.app.ui

import android.content.ClipData
import android.content.ClipboardManager
import android.os.Bundle
import com.pengxh.daily.app.BaseApplication
import com.pengxh.daily.app.R
import com.pengxh.daily.app.databinding.ActivityTaskConfigBinding
import com.pengxh.daily.app.extensions.initImmersionBar
import com.pengxh.daily.app.greendao.DailyTaskBeanDao
import com.pengxh.daily.app.service.FloatingWindowService
import com.pengxh.daily.app.utils.Constant
import com.pengxh.daily.app.widgets.TaskMessageDialog
import com.pengxh.kt.lite.base.KotlinBaseActivity
import com.pengxh.kt.lite.extensions.convertColor
import com.pengxh.kt.lite.extensions.getSystemService
import com.pengxh.kt.lite.extensions.show
import com.pengxh.kt.lite.utils.SaveKeyValues
import com.pengxh.kt.lite.widget.TitleBarView
import com.pengxh.kt.lite.widget.dialog.AlertInputDialog
import com.pengxh.kt.lite.widget.dialog.BottomActionSheet

class TaskConfigActivity : KotlinBaseActivity<ActivityTaskConfigBinding>() {

    private val kTag = "TaskConfigActivity"
    private val context = this
    private val timeArray = arrayListOf("15s", "30s", "45s")
    private val dailyTaskBeanDao by lazy { BaseApplication.get().daoSession.dailyTaskBeanDao }
    private val clipboard by lazy { getSystemService<ClipboardManager>() }

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

        binding.outputLayout.setOnClickListener {
            val taskBeans = dailyTaskBeanDao.queryBuilder().orderAsc(
                DailyTaskBeanDao.Properties.Time
            ).list()

            if (taskBeans.isEmpty()) {
                "没有任务可以导出".show(this)
                return@setOnClickListener
            }

            TaskMessageDialog.Builder()
                .setContext(this)
                .setTasks(taskBeans)
                .setOnDialogButtonClickListener(object :
                    TaskMessageDialog.OnDialogButtonClickListener {
                    override fun onConfirmClick(taskValue: String) {
                        val cipData = ClipData.newPlainText("DailyTask", taskValue)
                        clipboard?.setPrimaryClip(cipData)
                        "任务已复制到剪切板".show(context)
                    }
                }).build().show()
        }
    }

    override fun initOnCreate(savedInstanceState: Bundle?) {
        binding.timeoutTextView.text = SaveKeyValues.getValue(
            Constant.STAY_DD_TIMEOUT_KEY, "45s"
        ) as String
        binding.keyTextView.text = SaveKeyValues.getValue(Constant.DING_DING_KEY, "打卡") as String
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
}