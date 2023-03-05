package com.pengxh.autodingding.fragment

import android.os.CountDownTimer
import com.jzxiang.pickerview.TimePickerDialog
import com.jzxiang.pickerview.data.Type
import com.pengxh.autodingding.R
import com.pengxh.autodingding.utils.Constant
import com.pengxh.autodingding.utils.DingDingUtil
import com.pengxh.kt.lite.base.KotlinBaseFragment
import com.pengxh.kt.lite.extensions.show
import com.pengxh.kt.lite.extensions.timestampToDate
import kotlinx.android.synthetic.main.fragment_dingding.*

class DingDingFragment : KotlinBaseFragment() {

    private var amCountDownTimer: CountDownTimer? = null
    private var pmCountDownTimer: CountDownTimer? = null

    override fun setupTopBarLayout() {

    }

    override fun observeRequestState() {

    }

    override fun initLayoutView(): Int = R.layout.fragment_dingding

    override fun initData() {

    }

    override fun initEvent() {
        startLayoutView.setOnClickListener { v ->
            //设置上班时间
            TimePickerDialog.Builder()
                .setWheelItemTextSize(15)
                .setCyclic(false)
                .setMinMillseconds(System.currentTimeMillis())
                .setMaxMillseconds(System.currentTimeMillis() + Constant.ONE_WEEK)
                .setType(Type.ALL)
                .setCallBack { _: TimePickerDialog?, millSeconds: Long ->
                    amTime.text = millSeconds.timestampToDate()
                    //计算时间差
                    onDuty(millSeconds)
                }.build().show(childFragmentManager, "year_month_day_hour_minute")
        }

        endLayoutView.setOnClickListener { v ->
            //设置下班时间
            TimePickerDialog.Builder()
                .setWheelItemTextSize(15)
                .setCyclic(false)
                .setMinMillseconds(System.currentTimeMillis())
                .setMaxMillseconds(System.currentTimeMillis() + Constant.ONE_WEEK)
                .setType(Type.ALL)
                .setCallBack { _: TimePickerDialog?, millSeconds: Long ->
                    pmTime.text = millSeconds.timestampToDate()
                    //计算时间差
                    offDuty(millSeconds)
                }.build().show(childFragmentManager, "year_month_day_hour_minute")
        }

        endAmDuty.setOnClickListener { v ->
            amCountDownTimer?.cancel()
            startTimeView.text = "--"
        }

        endPmDuty.setOnClickListener { v ->
            pmCountDownTimer?.cancel()
            endTimeView.text = "--"
        }
    }

    private fun onDuty(millSeconds: Long) {
        val deltaTime = deltaTime(millSeconds / 1000)
        if (deltaTime == 0L) {
            return
        }
        //显示倒计时
        val text: String = startTimeView.text.toString()
        if (text == "--") {
            amCountDownTimer = object : CountDownTimer(deltaTime * 1000, 1000) {
                override fun onTick(l: Long) {
                    startTimeView.text = (l / 1000).toInt().toString()
                }

                override fun onFinish() {
                    startTimeView.text = "--"
                    DingDingUtil.openDingDing(Constant.DINGDING)
                }
            }
            amCountDownTimer?.start()
        } else {
            "已有任务在进行中".show(requireContext())
        }
    }

    private fun offDuty(millSeconds: Long) {
        val deltaTime = deltaTime(millSeconds / 1000)
        if (deltaTime == 0L) {
            return
        }
        //显示倒计时
        val text: String = endTimeView.text.toString()
        if (text == "--") {
            pmCountDownTimer = object : CountDownTimer(deltaTime * 1000, 1000) {
                override fun onTick(l: Long) {
                    endTimeView.text = (l / 1000).toInt().toString()
                }

                override fun onFinish() {
                    endTimeView.text = "--"
                    DingDingUtil.openDingDing(Constant.DINGDING)
                }
            }
            pmCountDownTimer?.start()
        } else {
            "已有任务在进行中".show(requireContext())
        }
    }

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