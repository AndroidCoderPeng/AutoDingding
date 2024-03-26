package com.pengxh.autodingding.fragment

import android.os.Bundle
import android.os.Handler
import android.os.Message
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.pengxh.autodingding.BaseApplication
import com.pengxh.autodingding.adapter.DateTimeAdapter
import com.pengxh.autodingding.bean.DateTimeBean
import com.pengxh.autodingding.databinding.FragmentDingdingBinding
import com.pengxh.autodingding.extensions.openApplication
import com.pengxh.autodingding.greendao.DateTimeBeanDao
import com.pengxh.autodingding.ui.AddTimerTaskActivity
import com.pengxh.autodingding.ui.UpdateTimerTaskActivity
import com.pengxh.autodingding.utils.Constant
import com.pengxh.kt.lite.base.KotlinBaseFragment
import com.pengxh.kt.lite.divider.RecyclerViewItemOffsets
import com.pengxh.kt.lite.extensions.navigatePageTo
import com.pengxh.kt.lite.utils.WeakReferenceHandler

class DingDingFragment : KotlinBaseFragment<FragmentDingdingBinding>(), Handler.Callback {

    private val kTag = "DingDingFragment"
    private val dateTimeBeanDao by lazy { BaseApplication.get().daoSession.dateTimeBeanDao }
    private lateinit var weakReferenceHandler: WeakReferenceHandler
    private var dateTimeAdapter: DateTimeAdapter? = null
    private var dataBeans: MutableList<DateTimeBean> = ArrayList()

    override fun setupTopBarLayout() {

    }

    override fun observeRequestState() {

    }

    override fun initViewBinding(
        inflater: LayoutInflater, container: ViewGroup?
    ): FragmentDingdingBinding {
        return FragmentDingdingBinding.inflate(inflater, container, false)
    }

    override fun initOnCreate(savedInstanceState: Bundle?) {
        weakReferenceHandler = WeakReferenceHandler(this)
        dataBeans = getAutoDingDingTasks()
        weakReferenceHandler.sendEmptyMessage(2023042601)
        //设置分割线
        binding.weeklyRecyclerView.addItemDecoration(
            RecyclerViewItemOffsets(0, 10, 0, 20)
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

    override fun handleMessage(msg: Message): Boolean {
        if (msg.what == 2023042601) {
            if (dataBeans.size == 0) {
                binding.emptyView.visibility = View.VISIBLE
            } else {
                binding.emptyView.visibility = View.GONE
                dateTimeAdapter = DateTimeAdapter(requireContext(), dataBeans)
                binding.weeklyRecyclerView.adapter = dateTimeAdapter
                dateTimeAdapter?.setOnItemClickListener(object :
                    DateTimeAdapter.OnItemClickListener {
                    override fun onItemClick(position: Int) {
                        requireContext().navigatePageTo<UpdateTimerTaskActivity>(dataBeans[position].uuid)
                    }

                    override fun onItemLongClick(position: Int) {
                        dateTimeBeanDao.delete(dataBeans[position])
                        dataBeans.removeAt(position)
                        dateTimeAdapter?.notifyDataSetChanged()
                        if (dataBeans.size == 0) {
                            binding.emptyView.visibility = View.VISIBLE
                        } else {
                            binding.emptyView.visibility = View.GONE
                        }
                    }

                    override fun onCountDownFinish() {
                        requireContext().openApplication(Constant.DING_DING)
                    }
                })
            }
        }
        return true
    }

    override fun onPause() {
        super.onPause()
        dateTimeAdapter?.stopCountDownTimer()
    }

    override fun initEvent() {
        binding.addTimerButton.setOnClickListener {
            requireContext().navigatePageTo<AddTimerTaskActivity>()
        }
    }
}