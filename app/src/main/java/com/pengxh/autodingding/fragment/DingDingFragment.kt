package com.pengxh.autodingding.fragment

import android.os.Handler
import android.view.View
import com.pengxh.autodingding.BaseApplication
import com.pengxh.autodingding.R
import com.pengxh.autodingding.adapter.DateTimeAdapter
import com.pengxh.autodingding.bean.DateTimeBean
import com.pengxh.autodingding.greendao.DateTimeBeanDao
import com.pengxh.autodingding.ui.AddTimerActivity
import com.pengxh.autodingding.utils.VerticalMarginItemDecoration
import com.pengxh.kt.lite.base.KotlinBaseFragment
import com.pengxh.kt.lite.extensions.dp2px
import com.pengxh.kt.lite.extensions.navigatePageTo
import com.pengxh.kt.lite.extensions.show
import com.pengxh.kt.lite.utils.WeakReferenceHandler
import kotlinx.android.synthetic.main.fragment_dingding.*

class DingDingFragment : KotlinBaseFragment() {

    companion object {
        lateinit var weakReferenceHandler: WeakReferenceHandler
    }

    private val kTag = "DingDingFragment"
    private val dateTimeBeanDao by lazy { BaseApplication.get().daoSession.dateTimeBeanDao }
    private lateinit var dateTimeAdapter: DateTimeAdapter
    private var dataBeans: MutableList<DateTimeBean> = ArrayList()

    override fun setupTopBarLayout() {

    }

    override fun observeRequestState() {

    }

    override fun initLayoutView(): Int = R.layout.fragment_dingding

    override fun initData() {
        weakReferenceHandler = WeakReferenceHandler(callback)
        dateTimeAdapter = DateTimeAdapter(requireContext())
        //设置分割线
        weeklyRecyclerView.addItemDecoration(
            VerticalMarginItemDecoration(10f.dp2px(requireContext()), 0)
        )
    }

    override fun onResume() {
        dataBeans = dateTimeBeanDao.queryBuilder().orderDesc(DateTimeBeanDao.Properties.Date).list()
        weakReferenceHandler.sendEmptyMessage(2023042601)
        super.onResume()
    }

    private val callback = Handler.Callback {
        if (it.what == 2023042601) {
            dateTimeAdapter.setupDateTimeData(dataBeans)
            weeklyRecyclerView.adapter = dateTimeAdapter
            dateTimeAdapter.setOnItemLongClickListener(object :
                DateTimeAdapter.OnItemLongClickListener {
                override fun onItemLongClick(view: View?, index: Int) {
                    dateTimeBeanDao.delete(dataBeans[index])
                    dataBeans.removeAt(index)
                    dateTimeAdapter.notifyItemRemoved(index)
                    dateTimeAdapter.notifyItemRangeChanged(
                        index, dataBeans.size - index
                    )
                }
            })
        }
        true
    }

    override fun initEvent() {
        addTimerButton.setOnClickListener {
            requireContext().navigatePageTo<AddTimerActivity>()
        }

//        startLayoutView.setOnClickListener { v ->
//            //设置上班时间
//            TimePickerDialog.Builder()
//                .setWheelItemTextSize(15)
//                .setCyclic(false)
//                .setMinMillseconds(System.currentTimeMillis())
//                .setMaxMillseconds(System.currentTimeMillis() + Constant.ONE_WEEK)
//                .setType(Type.ALL)
//                .setCallBack { _: TimePickerDialog?, millSeconds: Long ->
//                    amTime.text = millSeconds.timestampToDate()
//                    //计算时间差
//                    onDuty(millSeconds)
//                }.build().show(childFragmentManager, "year_month_day_hour_minute")
//        }
//
//        endLayoutView.setOnClickListener { v ->
//            //设置下班时间
//            TimePickerDialog.Builder()
//                .setWheelItemTextSize(15)
//                .setCyclic(false)
//                .setMinMillseconds(System.currentTimeMillis())
//                .setMaxMillseconds(System.currentTimeMillis() + Constant.ONE_WEEK)
//                .setType(Type.ALL)
//                .setCallBack { _: TimePickerDialog?, millSeconds: Long ->
//                    pmTime.text = millSeconds.timestampToDate()
//                    //计算时间差
//                    offDuty(millSeconds)
//                }.build().show(childFragmentManager, "year_month_day_hour_minute")
//        }
//
//        endAmDuty.setOnClickListener { v ->
//            amCountDownTimer?.cancel()
//            startTimeView.text = "--"
//        }
//
//        endPmDuty.setOnClickListener { v ->
//            pmCountDownTimer?.cancel()
//            endTimeView.text = "--"
//        }
    }

//    private fun onDuty(millSeconds: Long) {
//        val deltaTime = deltaTime(millSeconds / 1000)
//        if (deltaTime == 0L) {
//            return
//        }
//        //显示倒计时
//        val text: String = startTimeView.text.toString()
//        if (text == "--") {
//            amCountDownTimer = object : CountDownTimer(deltaTime * 1000, 1000) {
//                override fun onTick(l: Long) {
//                    startTimeView.text = (l / 1000).toInt().toString()
//                }
//
//                override fun onFinish() {
//                    startTimeView.text = "--"
//                    DingDingUtil.openDingDing(Constant.DINGDING)
//                }
//            }
//            amCountDownTimer?.start()
//        } else {
//            "已有任务在进行中".show(requireContext())
//        }
//    }
//
//    private fun offDuty(millSeconds: Long) {
//        val deltaTime = deltaTime(millSeconds / 1000)
//        if (deltaTime == 0L) {
//            return
//        }
//        //显示倒计时
//        val text: String = endTimeView.text.toString()
//        if (text == "--") {
//            pmCountDownTimer = object : CountDownTimer(deltaTime * 1000, 1000) {
//                override fun onTick(l: Long) {
//                    endTimeView.text = (l / 1000).toInt().toString()
//                }
//
//                override fun onFinish() {
//                    endTimeView.text = "--"
//                    DingDingUtil.openDingDing(Constant.DINGDING)
//                }
//            }
//            pmCountDownTimer?.start()
//        } else {
//            "已有任务在进行中".show(requireContext())
//        }
//    }

    /**
     * 计算时间差
     *
     * @param fixedTime 结束时间
     */
    private fun deltaTime(fixedTime: Long): Long {
        val currentTime = System.currentTimeMillis() / 1000
        if (fixedTime > currentTime) {
            return fixedTime - currentTime
        } else {
            "时间设置异常".show(requireContext())
        }
        return 0L
    }
}