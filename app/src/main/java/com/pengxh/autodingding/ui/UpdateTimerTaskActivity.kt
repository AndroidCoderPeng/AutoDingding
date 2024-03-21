package com.pengxh.autodingding.ui

import android.annotation.SuppressLint
import android.os.Bundle
import com.pengxh.autodingding.BaseApplication
import com.pengxh.autodingding.R
import com.pengxh.autodingding.bean.DateTimeBean
import com.pengxh.autodingding.databinding.ActivityUpdateTimerTaskBinding
import com.pengxh.autodingding.extensions.convertToWeek
import com.pengxh.autodingding.extensions.initImmersionBar
import com.pengxh.autodingding.greendao.DateTimeBeanDao
import com.pengxh.kt.lite.base.KotlinBaseActivity
import com.pengxh.kt.lite.extensions.appendZero
import com.pengxh.kt.lite.utils.Constant
import com.pengxh.kt.lite.widget.TitleBarView
import java.util.*

@SuppressLint("SetTextI18n")
class UpdateTimerTaskActivity : KotlinBaseActivity<ActivityUpdateTimerTaskBinding>() {

    private val dateTimeBeanDao by lazy { BaseApplication.get().daoSession.dateTimeBeanDao }
    private val calendar by lazy { Calendar.getInstance() }
    private lateinit var dateTimeBean: DateTimeBean

    override fun initViewBinding(): ActivityUpdateTimerTaskBinding {
        return ActivityUpdateTimerTaskBinding.inflate(layoutInflater)
    }

    override fun initOnCreate(savedInstanceState: Bundle?) {
        val taskUuid = intent.getStringExtra(Constant.INTENT_PARAM)!!
        dateTimeBean = dateTimeBeanDao.queryBuilder().where(
            DateTimeBeanDao.Properties.Uuid.eq(taskUuid)
        ).unique()

        //设置默认显示日期
        binding.selectedDateView.text = dateTimeBean.date
        binding.selectedTimeView.text = dateTimeBean.time
    }

    override fun initEvent() {
        binding.datePicker.init(
            calendar.get(Calendar.YEAR),
            calendar.get(Calendar.MONTH),
            calendar.get(Calendar.DAY_OF_MONTH)
        ) { _, year, monthOfYear, dayOfMonth ->
            binding.selectedDateView.text =
                "${year}-${(monthOfYear + 1).appendZero()}-${dayOfMonth.appendZero()}"
        }

        binding.timePicker.setOnTimeChangedListener { _, hourOfDay, minute ->
            binding.selectedTimeView.text = "${hourOfDay.appendZero()}:${minute.appendZero()}"
        }

        binding.updateTimerButton.setOnClickListener {
            dateTimeBean.date = binding.selectedDateView.text.toString()
            dateTimeBean.time = binding.selectedTimeView.text.toString()
            dateTimeBean.weekDay = binding.selectedDateView.text.toString().convertToWeek()

            dateTimeBeanDao.update(dateTimeBean)
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