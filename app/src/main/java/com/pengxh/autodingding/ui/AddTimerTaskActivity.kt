package com.pengxh.autodingding.ui

import android.annotation.SuppressLint
import com.gyf.immersionbar.ImmersionBar
import com.pengxh.autodingding.BaseApplication
import com.pengxh.autodingding.R
import com.pengxh.autodingding.bean.DateTimeBean
import com.pengxh.autodingding.extensions.appendZero
import com.pengxh.autodingding.extensions.convertToWeek
import com.pengxh.kt.lite.base.KotlinBaseActivity
import com.pengxh.kt.lite.extensions.convertColor
import com.pengxh.kt.lite.utils.ImmerseStatusBarUtil
import kotlinx.android.synthetic.main.activity_add_timer_task.*
import kotlinx.android.synthetic.main.include_base_title.*
import java.util.*

@SuppressLint("SetTextI18n")
class AddTimerTaskActivity : KotlinBaseActivity() {

    private val dateTimeBeanDao by lazy { BaseApplication.get().daoSession.dateTimeBeanDao }
    private val calendar by lazy { Calendar.getInstance() }

    override fun initData() {

    }

    override fun initEvent() {
        leftBackView.setOnClickListener { finish() }

        //设置默认显示日期
        val month = (calendar.get(Calendar.MONTH) + 1).appendZero()
        val dayOfMonth = calendar.get(Calendar.DAY_OF_MONTH).appendZero()
        selectedDateView.text = "${calendar.get(Calendar.YEAR)}-${month}-${dayOfMonth}"

        datePicker.init(
            calendar.get(Calendar.YEAR),
            calendar.get(Calendar.MONTH),
            calendar.get(Calendar.DAY_OF_MONTH)
        ) { _, year, monthOfYear, dayOfMonth ->
            selectedDateView.text =
                "${year}-${(monthOfYear + 1).appendZero()}-${dayOfMonth.appendZero()}"
        }

        //设置默认显示时间
        selectedTimeView.text = "${timePicker.hour.appendZero()}:${timePicker.minute.appendZero()}"
        timePicker.setOnTimeChangedListener { _, hourOfDay, minute ->
            selectedTimeView.text = "${hourOfDay.appendZero()}:${minute.appendZero()}"
        }

        saveTimerButton.setOnClickListener {
            val bean = DateTimeBean()
            bean.uuid = UUID.randomUUID().toString()
            bean.date = selectedDateView.text.toString()
            bean.time = selectedTimeView.text.toString()
            bean.weekDay = selectedDateView.text.toString().convertToWeek()

            dateTimeBeanDao.insert(bean)
            finish()
        }
    }

    override fun initLayoutView(): Int = R.layout.activity_add_timer_task

    override fun observeRequestState() {

    }

    override fun setupTopBarLayout() {
        ImmerseStatusBarUtil.setColor(
            this, R.color.colorAppThemeLight.convertColor(this)
        )
        ImmersionBar.with(this).statusBarDarkFont(false).init()
        titleView.text = "新建定时任务"
    }
}