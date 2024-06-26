package com.pengxh.autodingding.ui

import android.annotation.SuppressLint
import android.os.Bundle
import android.widget.DatePicker
import com.pengxh.autodingding.BaseApplication
import com.pengxh.autodingding.R
import com.pengxh.autodingding.bean.DateTimeBean
import com.pengxh.autodingding.databinding.ActivityAddTimerTaskBinding
import com.pengxh.autodingding.extensions.convertToWeek
import com.pengxh.autodingding.extensions.initImmersionBar
import com.pengxh.kt.lite.base.KotlinBaseActivity
import com.pengxh.kt.lite.extensions.appendZero
import com.pengxh.kt.lite.utils.ActivityStackManager
import com.pengxh.kt.lite.widget.TitleBarView
import java.util.*

@SuppressLint("SetTextI18n")
class AddTimerTaskActivity : KotlinBaseActivity<ActivityAddTimerTaskBinding>() {

    private val dateTimeBeanDao by lazy { BaseApplication.get().daoSession.dateTimeBeanDao }
    private val calendar by lazy { Calendar.getInstance() }

    override fun initViewBinding(): ActivityAddTimerTaskBinding {
        return ActivityAddTimerTaskBinding.inflate(layoutInflater)
    }

    override fun initOnCreate(savedInstanceState: Bundle?) {
        ActivityStackManager.addActivity(this)

        //设置默认显示日期
        val month = (calendar.get(Calendar.MONTH) + 1).appendZero()
        val dayOfMonth = calendar.get(Calendar.DAY_OF_MONTH).appendZero()
        binding.selectedDateView.text = "${calendar.get(Calendar.YEAR)}-${month}-${dayOfMonth}"
        binding.datePicker.init(
            calendar.get(Calendar.YEAR),
            calendar.get(Calendar.MONTH),
            calendar.get(Calendar.DAY_OF_MONTH),
            object : DatePicker.OnDateChangedListener {
                override fun onDateChanged(
                    view: DatePicker?, year: Int, monthOfYear: Int, dayOfMonth: Int
                ) {
                    binding.selectedDateView.text =
                        "${year}-${(monthOfYear + 1).appendZero()}-${dayOfMonth.appendZero()}"
                }
            })

        //设置默认显示时间
        binding.selectedTimeView.text =
            "${binding.timePicker.hour.appendZero()}:${binding.timePicker.minute.appendZero()}"
        binding.timePicker.setOnTimeChangedListener { _, hourOfDay, minute ->
            binding.selectedTimeView.text = "${hourOfDay.appendZero()}:${minute.appendZero()}"
        }
    }

    override fun initEvent() {
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
        binding.rootView.initImmersionBar(this, false, R.color.colorAppThemeLight)
        binding.titleView.setOnClickListener(object : TitleBarView.OnClickListener {
            override fun onLeftClick() {
                finish()
            }

            override fun onRightClick() {

            }
        })
    }
}