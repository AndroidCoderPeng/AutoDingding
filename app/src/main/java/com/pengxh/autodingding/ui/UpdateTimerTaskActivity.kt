package com.pengxh.autodingding.ui

import android.annotation.SuppressLint
import com.gyf.immersionbar.ImmersionBar
import com.pengxh.autodingding.BaseApplication
import com.pengxh.autodingding.R
import com.pengxh.autodingding.bean.DateTimeBean
import com.pengxh.autodingding.extensions.appendZero
import com.pengxh.autodingding.extensions.convertToWeek
import com.pengxh.autodingding.greendao.DateTimeBeanDao
import com.pengxh.kt.lite.base.KotlinBaseActivity
import com.pengxh.kt.lite.extensions.convertColor
import com.pengxh.kt.lite.utils.Constant
import com.pengxh.kt.lite.utils.ImmerseStatusBarUtil
import kotlinx.android.synthetic.main.activity_update_timer_task.*
import kotlinx.android.synthetic.main.include_base_title.*
import java.util.*

@SuppressLint("SetTextI18n")
class UpdateTimerTaskActivity : KotlinBaseActivity() {

    private val dateTimeBeanDao by lazy { BaseApplication.get().daoSession.dateTimeBeanDao }
    private val calendar by lazy { Calendar.getInstance() }
    private lateinit var dateTimeBean: DateTimeBean

    override fun initData() {
        val taskUuid = intent.getStringExtra(Constant.INTENT_PARAM)!!
        dateTimeBean = dateTimeBeanDao.queryBuilder().where(
            DateTimeBeanDao.Properties.Uuid.eq(taskUuid)
        ).unique()

        //设置默认显示日期
        selectedDateView.text = dateTimeBean.date
        selectedTimeView.text = dateTimeBean.time
    }

    override fun initEvent() {
        leftBackView.setOnClickListener { finish() }

        datePicker.init(
            calendar.get(Calendar.YEAR),
            calendar.get(Calendar.MONTH),
            calendar.get(Calendar.DAY_OF_MONTH)
        ) { _, year, monthOfYear, dayOfMonth ->
            selectedDateView.text =
                "${year}-${(monthOfYear + 1).appendZero()}-${dayOfMonth.appendZero()}"
        }

        timePicker.setOnTimeChangedListener { _, hourOfDay, minute ->
            selectedTimeView.text = "${hourOfDay.appendZero()}:${minute.appendZero()}"
        }

        updateTimerButton.setOnClickListener {
            dateTimeBean.date = selectedDateView.text.toString()
            dateTimeBean.time = selectedTimeView.text.toString()
            dateTimeBean.weekDay = selectedDateView.text.toString().convertToWeek()

            dateTimeBeanDao.update(dateTimeBean)
            finish()
        }
    }

    override fun initLayoutView(): Int = R.layout.activity_update_timer_task

    override fun observeRequestState() {

    }

    override fun setupTopBarLayout() {
        ImmerseStatusBarUtil.setColor(
            this, R.color.colorAppThemeLight.convertColor(this)
        )
        ImmersionBar.with(this).statusBarDarkFont(false).init()
        titleView.text = "修改定时任务"
    }
}