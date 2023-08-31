package com.pengxh.autodingding.ui

import android.annotation.SuppressLint
import android.os.Bundle
import com.gyf.immersionbar.ImmersionBar
import com.pengxh.autodingding.BaseApplication
import com.pengxh.autodingding.R
import com.pengxh.autodingding.bean.DateTimeBean
import com.pengxh.autodingding.databinding.ActivityAddTimerTaskBinding
import com.pengxh.autodingding.extensions.appendZero
import com.pengxh.autodingding.extensions.convertToWeek
import com.pengxh.kt.lite.base.KotlinBaseActivity
import com.pengxh.kt.lite.extensions.convertColor
import com.pengxh.kt.lite.utils.ImmerseStatusBarUtil
import java.util.*

@SuppressLint("SetTextI18n")
class AddTimerTaskActivity : KotlinBaseActivity<ActivityAddTimerTaskBinding>() {

    private val dateTimeBeanDao by lazy { BaseApplication.get().daoSession.dateTimeBeanDao }
    private val calendar by lazy { Calendar.getInstance() }

    override fun initViewBinding(): ActivityAddTimerTaskBinding {
        return ActivityAddTimerTaskBinding.inflate(layoutInflater)
    }

    override fun initOnCreate(savedInstanceState: Bundle?) {

    }

    override fun initEvent() {
        binding.titleInclude.leftBackView.setOnClickListener { finish() }

        //设置默认显示日期
        val month = (calendar.get(Calendar.MONTH) + 1).appendZero()
        val dayOfMonth = calendar.get(Calendar.DAY_OF_MONTH).appendZero()
        binding.selectedDateView.text = "${calendar.get(Calendar.YEAR)}-${month}-${dayOfMonth}"

        binding.datePicker.init(
            calendar.get(Calendar.YEAR),
            calendar.get(Calendar.MONTH),
            calendar.get(Calendar.DAY_OF_MONTH)
        ) { _, year, monthOfYear, dayOfMonth ->
            binding.selectedDateView.text =
                "${year}-${(monthOfYear + 1).appendZero()}-${dayOfMonth.appendZero()}"
        }

        //设置默认显示时间
        binding.selectedTimeView.text =
            "${binding.timePicker.hour.appendZero()}:${binding.timePicker.minute.appendZero()}"
        binding.timePicker.setOnTimeChangedListener { _, hourOfDay, minute ->
            binding.selectedTimeView.text = "${hourOfDay.appendZero()}:${minute.appendZero()}"
        }

        binding.saveTimerButton.setOnClickListener {
            val bean = DateTimeBean()
            bean.uuid = UUID.randomUUID().toString()
            bean.date = binding.selectedDateView.text.toString()
            bean.time = binding.selectedTimeView.text.toString()
            bean.weekDay = binding.selectedDateView.text.toString().convertToWeek()

            dateTimeBeanDao.insert(bean)
            finish()
        }
    }

    override fun observeRequestState() {

    }

    override fun setupTopBarLayout() {
        ImmerseStatusBarUtil.setColor(
            this, R.color.colorAppThemeLight.convertColor(this)
        )
        ImmersionBar.with(this).statusBarDarkFont(false).init()
        binding.titleInclude.titleView.text = "新建定时任务"
    }
}