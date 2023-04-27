package com.pengxh.autodingding.fragment

import android.os.Handler
import android.view.View
import com.pengxh.autodingding.BaseApplication
import com.pengxh.autodingding.R
import com.pengxh.autodingding.adapter.DateTimeAdapter
import com.pengxh.autodingding.bean.DateTimeBean
import com.pengxh.autodingding.extensions.openApplication
import com.pengxh.autodingding.greendao.DateTimeBeanDao
import com.pengxh.autodingding.ui.AddTimerTaskActivity
import com.pengxh.autodingding.ui.UpdateTimerTaskActivity
import com.pengxh.autodingding.utils.Constant
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
    private var dateTimeAdapter: DateTimeAdapter? = null
    private var dataBeans: MutableList<DateTimeBean> = ArrayList()

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
        super.onResume()
        dataBeans = getAutoDingDingTasks()
        weakReferenceHandler.sendEmptyMessage(2023042601)
    }

    private fun getAutoDingDingTasks(): MutableList<DateTimeBean> {
        return dateTimeBeanDao.queryBuilder().orderDesc(DateTimeBeanDao.Properties.Date).list()
    }

    private val callback = Handler.Callback {
        if (it.what == 2023042601) {
            if (dataBeans.size == 0) {
                emptyView.visibility = View.VISIBLE
            } else {
                emptyView.visibility = View.GONE
                dateTimeAdapter = DateTimeAdapter(requireContext(), dataBeans)
                weeklyRecyclerView.adapter = dateTimeAdapter
                dateTimeAdapter?.setOnItemClickListener(object :
                    DateTimeAdapter.OnItemClickListener {
                    override fun onItemClick(layoutPosition: Int) {
                        requireContext().navigatePageTo<UpdateTimerTaskActivity>(dataBeans[layoutPosition].uuid)
                    }

                    override fun onItemLongClick(view: View?, layoutPosition: Int) {
                        dateTimeBeanDao.delete(dataBeans[layoutPosition])
                        dataBeans.removeAt(layoutPosition)
                        dateTimeAdapter?.notifyItemRemoved(layoutPosition)
                        dateTimeAdapter?.notifyItemChanged(layoutPosition)
                        if (dataBeans.size == 0) {
                            emptyView.visibility = View.VISIBLE
                        } else {
                            emptyView.visibility = View.GONE
                        }
                    }

                    override fun onCountDownFinish() {
                        requireContext().openApplication(Constant.DING_DING)
                    }
                })
            }
        }
        true
    }

    override fun onPause() {
        super.onPause()
        dateTimeAdapter?.stopCountDownTimer()
    }

    override fun initEvent() {
        addTimerButton.setOnClickListener {
            requireContext().navigatePageTo<AddTimerTaskActivity>()
        }
    }
}