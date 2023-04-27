package com.pengxh.autodingding.fragment

import android.os.CountDownTimer
import android.os.Handler
import android.view.View
import com.pengxh.autodingding.BaseApplication
import com.pengxh.autodingding.R
import com.pengxh.autodingding.adapter.DateTimeAdapter
import com.pengxh.autodingding.bean.DateTimeBean
import com.pengxh.autodingding.greendao.DateTimeBeanDao
import com.pengxh.autodingding.ui.AddTimerTaskActivity
import com.pengxh.autodingding.ui.UpdateTimerTaskActivity
import com.pengxh.autodingding.utils.VerticalMarginItemDecoration
import com.pengxh.kt.lite.base.KotlinBaseFragment
import com.pengxh.kt.lite.extensions.dp2px
import com.pengxh.kt.lite.extensions.navigatePageTo
import com.pengxh.kt.lite.utils.WeakReferenceHandler
import kotlinx.android.synthetic.main.fragment_dingding.*

class DingDingFragment : KotlinBaseFragment() {

    private val kTag = "DingDingFragment"
    private val dateTimeBeanDao by lazy { BaseApplication.get().daoSession.dateTimeBeanDao }
    private lateinit var weakReferenceHandler: WeakReferenceHandler
    private lateinit var dateTimeAdapter: DateTimeAdapter
    private var dataBeans: MutableList<DateTimeBean> = ArrayList()
    private var isRefresh = false
    private var isLoadMore = false
    private var offset = 0 // 本地数据库分页从0开始

    override fun setupTopBarLayout() {

    }

    override fun observeRequestState() {

    }

    override fun initLayoutView(): Int = R.layout.fragment_dingding

    override fun initData() {
        weakReferenceHandler = WeakReferenceHandler(callback)
        dataBeans = getAutoDingDingTasks()
        weakReferenceHandler.sendEmptyMessage(2023042601)
        //设置分割线
        weeklyRecyclerView.addItemDecoration(
            VerticalMarginItemDecoration(1f.dp2px(requireContext()), 7f.dp2px(requireContext()))
        )
    }

    override fun onResume() {
        dataBeans = getAutoDingDingTasks()
        weakReferenceHandler.sendEmptyMessage(2023042601)
        super.onResume()
    }

    private fun getAutoDingDingTasks(): MutableList<DateTimeBean> {
        return dateTimeBeanDao.queryBuilder()
            .orderDesc(DateTimeBeanDao.Properties.Date)
            .offset(offset * 15).limit(15).list()
    }

    private val callback = Handler.Callback {
        if (it.what == 2023042601) {
            if (isRefresh) {
                dateTimeAdapter.notifyDataSetChanged()
            } else { //首次加载数据
                if (dataBeans.size == 0) {
                    emptyView.visibility = View.VISIBLE
                } else {
                    emptyView.visibility = View.GONE
                    dateTimeAdapter = DateTimeAdapter(requireContext(), dataBeans)
                    weeklyRecyclerView.adapter = dateTimeAdapter
                    dateTimeAdapter.setOnItemClickListener(object :
                        DateTimeAdapter.OnItemClickListener {
                        override fun onItemClick(index: Int) {
                            requireContext().navigatePageTo<UpdateTimerTaskActivity>(dataBeans[index].uuid)
                        }

                        override fun onItemLongClick(view: View?, index: Int) {
                            dateTimeBeanDao.delete(dataBeans[index])
                            dataBeans.removeAt(index)
                            dateTimeAdapter.notifyItemRemoved(index)
                            dateTimeAdapter.notifyItemRangeChanged(
                                index, dataBeans.size - index
                            )
                            if (dataBeans.size == 0) {
                                emptyView.visibility = View.VISIBLE
                            } else {
                                emptyView.visibility = View.GONE
                            }
                        }
                    })
                }
            }
        }
        true
    }

    override fun initEvent() {
        addTimerButton.setOnClickListener {
            requireContext().navigatePageTo<AddTimerTaskActivity>()
        }

        refreshLayout.setOnRefreshListener {
            isRefresh = true
            object : CountDownTimer(1000, 500) {
                override fun onTick(millisUntilFinished: Long) {}
                override fun onFinish() {
                    isRefresh = false
                    dataBeans.clear()
                    offset = 0
                    dataBeans = getAutoDingDingTasks()
                    it.finishRefresh()
                    weakReferenceHandler.sendEmptyMessage(2023042601)
                }
            }.start()
        }

        refreshLayout.setOnLoadMoreListener {
            isLoadMore = true
            object : CountDownTimer(1000, 500) {
                override fun onTick(millisUntilFinished: Long) {}
                override fun onFinish() {
                    isLoadMore = false
                    offset++
                    dataBeans.addAll(getAutoDingDingTasks())
                    it.finishLoadMore()
                    weakReferenceHandler.sendEmptyMessage(2023042601)
                }
            }.start()
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
}