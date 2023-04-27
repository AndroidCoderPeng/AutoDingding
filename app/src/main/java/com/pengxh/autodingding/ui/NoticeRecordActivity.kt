package com.pengxh.autodingding.ui

import android.os.CountDownTimer
import android.os.Handler
import android.os.Message
import android.view.View
import com.gyf.immersionbar.ImmersionBar
import com.pengxh.autodingding.BaseApplication
import com.pengxh.autodingding.R
import com.pengxh.autodingding.bean.NotificationBean
import com.pengxh.autodingding.greendao.NotificationBeanDao
import com.pengxh.autodingding.utils.DividerItemDecoration
import com.pengxh.kt.lite.adapter.NormalRecyclerAdapter
import com.pengxh.kt.lite.adapter.ViewHolder
import com.pengxh.kt.lite.base.KotlinBaseActivity
import com.pengxh.kt.lite.extensions.convertColor
import com.pengxh.kt.lite.extensions.dp2px
import com.pengxh.kt.lite.utils.ImmerseStatusBarUtil
import com.pengxh.kt.lite.utils.WeakReferenceHandler
import kotlinx.android.synthetic.main.activity_notice.*
import kotlinx.android.synthetic.main.include_base_title.*

class NoticeRecordActivity : KotlinBaseActivity() {

    private val context = this@NoticeRecordActivity
    private val notificationBeanDao by lazy { BaseApplication.get().daoSession.notificationBeanDao }
    private lateinit var weakReferenceHandler: WeakReferenceHandler
    private lateinit var noticeAdapter: NormalRecyclerAdapter<NotificationBean>
    private var dataBeans: MutableList<NotificationBean> = ArrayList()
    private var isRefresh = false
    private var isLoadMore = false
    private var offset = 0 // 本地数据库分页从0开始

    override fun setupTopBarLayout() {
        ImmerseStatusBarUtil.setColor(
            this, R.color.colorAppThemeLight.convertColor(this)
        )
        ImmersionBar.with(this).statusBarDarkFont(false).init()
        titleView.text = "所有通知"
    }

    override fun initData() {
        weakReferenceHandler = WeakReferenceHandler(callback)
        dataBeans = queryNotificationRecord()
        weakReferenceHandler.sendEmptyMessage(2022061901)
    }

    override fun initEvent() {
        leftBackView.setOnClickListener { finish() }

        refreshLayout.setOnRefreshListener { refreshLayout ->
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

        refreshLayout.setOnLoadMoreListener { refreshLayout ->
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

    override fun initLayoutView(): Int = R.layout.activity_notice

    override fun observeRequestState() {

    }

    private val callback = Handler.Callback { msg: Message ->
        if (msg.what == 2022061901) {
            if (isRefresh || isLoadMore) {
                noticeAdapter.notifyDataSetChanged()
            } else { //首次加载数据
                if (dataBeans.size == 0) {
                    emptyView.visibility = View.VISIBLE
                } else {
                    emptyView.visibility = View.GONE
                    noticeAdapter =
                        object : NormalRecyclerAdapter<NotificationBean>(
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
                    notificationView.addItemDecoration(
                        DividerItemDecoration(
                            10f.dp2px(context).toFloat(), 10f.dp2px(context).toFloat()
                        )
                    )
                    notificationView.adapter = noticeAdapter
                }
            }
        }
        true
    }

    private fun queryNotificationRecord(): MutableList<NotificationBean> {
        return notificationBeanDao.queryBuilder()
            .orderDesc(NotificationBeanDao.Properties.PostTime)
            .offset(offset * 15).limit(15).list()
    }
}