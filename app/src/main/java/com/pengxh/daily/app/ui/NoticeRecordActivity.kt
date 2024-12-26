package com.pengxh.daily.app.ui

import android.graphics.Color
import android.os.Bundle
import android.os.CountDownTimer
import android.os.Handler
import android.os.Message
import android.view.View
import com.pengxh.daily.app.BaseApplication
import com.pengxh.daily.app.R
import com.pengxh.daily.app.bean.NotificationBean
import com.pengxh.daily.app.databinding.ActivityNoticeBinding
import com.pengxh.daily.app.extensions.initImmersionBar
import com.pengxh.daily.app.greendao.NotificationBeanDao
import com.pengxh.kt.lite.adapter.NormalRecyclerAdapter
import com.pengxh.kt.lite.adapter.ViewHolder
import com.pengxh.kt.lite.base.KotlinBaseActivity
import com.pengxh.kt.lite.divider.RecyclerViewItemDivider
import com.pengxh.kt.lite.utils.WeakReferenceHandler
import com.pengxh.kt.lite.widget.TitleBarView
import com.pengxh.kt.lite.widget.dialog.AlertMessageDialog

class NoticeRecordActivity : KotlinBaseActivity<ActivityNoticeBinding>(), Handler.Callback {

    private val notificationBeanDao by lazy { BaseApplication.get().daoSession.notificationBeanDao }
    private val weakReferenceHandler by lazy { WeakReferenceHandler(this) }
    private lateinit var noticeAdapter: NormalRecyclerAdapter<NotificationBean>
    private var dataBeans: MutableList<NotificationBean> = ArrayList()
    private var isRefresh = false
    private var isLoadMore = false
    private var offset = 0 // 本地数据库分页从0开始

    override fun initViewBinding(): ActivityNoticeBinding {
        return ActivityNoticeBinding.inflate(layoutInflater)
    }

    override fun setupTopBarLayout() {
        binding.rootView.initImmersionBar(this, true, R.color.white)
        binding.titleView.setOnClickListener(object : TitleBarView.OnClickListener {
            override fun onLeftClick() {
                finish()
            }

            override fun onRightClick() {
                AlertMessageDialog.Builder()
                    .setContext(this@NoticeRecordActivity)
                    .setTitle("温馨提示")
                    .setMessage("此操作将会清空所有通知记录，且不可恢复")
                    .setPositiveButton("知道了")
                    .setOnDialogButtonClickListener(object :
                        AlertMessageDialog.OnDialogButtonClickListener {
                        override fun onConfirmClick() {
                            notificationBeanDao.deleteAll()
                            binding.emptyView.visibility = View.VISIBLE
                            binding.notificationView.visibility = View.GONE
                        }
                    }).build().show()
            }
        })
    }

    override fun initOnCreate(savedInstanceState: Bundle?) {
        dataBeans = queryNotificationRecord()
        weakReferenceHandler.sendEmptyMessage(2022061901)
    }

    override fun initEvent() {
        binding.refreshLayout.setOnRefreshListener { refreshLayout ->
            isRefresh = true
            object : CountDownTimer(1000, 500) {
                override fun onTick(millisUntilFinished: Long) {}
                override fun onFinish() {
                    isRefresh = false
                    dataBeans.clear()
                    offset = 0
                    dataBeans = queryNotificationRecord()
                    refreshLayout.finishRefresh()
                    weakReferenceHandler.sendEmptyMessage(2022061901)
                }
            }.start()
        }

        binding.refreshLayout.setOnLoadMoreListener { refreshLayout ->
            isLoadMore = true
            object : CountDownTimer(1000, 500) {
                override fun onTick(millisUntilFinished: Long) {}
                override fun onFinish() {
                    isLoadMore = false
                    offset++
                    dataBeans.addAll(queryNotificationRecord())
                    refreshLayout.finishLoadMore()
                    weakReferenceHandler.sendEmptyMessage(2022061901)
                }
            }.start()
        }
    }

    override fun observeRequestState() {

    }

    override fun handleMessage(msg: Message): Boolean {
        if (msg.what == 2022061901) {
            if (isRefresh || isLoadMore) {
                noticeAdapter.notifyDataSetChanged()
            } else { //首次加载数据
                if (dataBeans.size == 0) {
                    binding.emptyView.visibility = View.VISIBLE
                    binding.notificationView.visibility = View.GONE
                } else {
                    binding.emptyView.visibility = View.GONE
                    binding.notificationView.visibility = View.VISIBLE
                    noticeAdapter = object : NormalRecyclerAdapter<NotificationBean>(
                        R.layout.item_notice_rv_l, dataBeans
                    ) {
                        override fun convertView(
                            viewHolder: ViewHolder, position: Int, item: NotificationBean
                        ) {
                            viewHolder.setText(R.id.titleView, "标题：${item.notificationTitle}")
                                .setText(R.id.packageNameView, "包名：${item.packageName}")
                                .setText(R.id.messageView, "内容：${item.notificationMsg}")
                                .setText(R.id.postTimeView, item.postTime)
                        }
                    }
                    binding.notificationView.addItemDecoration(
                        RecyclerViewItemDivider(1, Color.LTGRAY)
                    )
                    binding.notificationView.adapter = noticeAdapter
                }
            }
        }
        return true
    }

    private fun queryNotificationRecord(): MutableList<NotificationBean> {
        return notificationBeanDao.queryBuilder()
            .orderDesc(NotificationBeanDao.Properties.PostTime)
            .offset(offset * 15).limit(15).list()
    }
}