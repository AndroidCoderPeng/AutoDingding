package com.pengxh.autodingding.ui

import android.content.Context
import android.os.CountDownTimer
import android.os.Environment
import android.os.Handler
import android.os.Message
import android.text.TextUtils
import android.view.View
import com.gyf.immersionbar.ImmersionBar
import com.pengxh.autodingding.BaseApplication
import com.pengxh.autodingding.R
import com.pengxh.autodingding.bean.HistoryRecordBean
import com.pengxh.autodingding.extensions.writeObjToExcel
import com.pengxh.autodingding.greendao.HistoryRecordBeanDao
import com.pengxh.autodingding.utils.Constant
import com.pengxh.autodingding.utils.DividerItemDecoration
import com.pengxh.autodingding.utils.ExcelUtils
import com.pengxh.kt.lite.adapter.NormalRecyclerAdapter
import com.pengxh.kt.lite.adapter.ViewHolder
import com.pengxh.kt.lite.base.KotlinBaseActivity
import com.pengxh.kt.lite.extensions.convertColor
import com.pengxh.kt.lite.extensions.dp2px
import com.pengxh.kt.lite.extensions.show
import com.pengxh.kt.lite.utils.ImmerseStatusBarUtil
import com.pengxh.kt.lite.utils.SaveKeyValues
import com.pengxh.kt.lite.utils.WeakReferenceHandler
import com.pengxh.kt.lite.widget.EasyPopupWindow
import com.pengxh.kt.lite.widget.dialog.AlertControlDialog
import com.pengxh.kt.lite.widget.dialog.AlertMessageDialog
import kotlinx.android.synthetic.main.activity_history.*
import java.io.File

class HistoryRecordActivity : KotlinBaseActivity() {

    private val context: Context = this@HistoryRecordActivity
    private val images = intArrayOf(R.drawable.ic_delete, R.drawable.ic_export)
    private val titles = arrayOf("删除记录", "导出记录")
    private val excelTitle = arrayOf("uuid", "日期", "打卡信息")
    private val historyRecordBeanDao by lazy { BaseApplication.get().daoSession.historyRecordBeanDao }
    private lateinit var easyPopupWindow: EasyPopupWindow
    private lateinit var weakReferenceHandler: WeakReferenceHandler
    private lateinit var historyAdapter: NormalRecyclerAdapter<HistoryRecordBean>
    private var dataBeans: MutableList<HistoryRecordBean> = ArrayList()
    private var isRefresh = false
    private var isLoadMore = false
    private var offset = 0 // 本地数据库分页从0开始

    override fun setupTopBarLayout() {
        ImmerseStatusBarUtil.setColor(
            this, R.color.colorAppThemeLight.convertColor(this)
        )
        ImmersionBar.with(this).statusBarDarkFont(false).init()
        titleView.text = "打卡记录"
        titleRightView.setOnClickListener {
            easyPopupWindow.showAsDropDown(titleRightView, 0, 10f.dp2px(context))
        }
    }

    override fun initData() {
        weakReferenceHandler = WeakReferenceHandler(callback)
        dataBeans = queryHistoryRecord()
        weakReferenceHandler.sendEmptyMessage(2022021403)
        easyPopupWindow = EasyPopupWindow(this)
        easyPopupWindow.setPopupMenuItem(images, titles)
        easyPopupWindow.setOnPopupWindowClickListener(object :
            EasyPopupWindow.OnPopupWindowClickListener {
            override fun onPopupItemClicked(position: Int) {
                when (position) {
                    0 -> {
                        if (dataBeans.size == 0) {
                            AlertMessageDialog.Builder()
                                .setContext(context)
                                .setTitle("温馨提示")
                                .setMessage("空空如也，无法删除")
                                .setPositiveButton("确定")
                                .setOnDialogButtonClickListener(
                                    object : AlertMessageDialog.OnDialogButtonClickListener {
                                        override fun onConfirmClick() {

                                        }
                                    }
                                ).build().show()
                        } else {
                            AlertControlDialog.Builder()
                                .setContext(context)
                                .setTitle("温馨提示")
                                .setMessage("是否确定清除打卡记录？")
                                .setNegativeButton("取消")
                                .setPositiveButton("确定")
                                .setOnDialogButtonClickListener(object :
                                    AlertControlDialog.OnDialogButtonClickListener {
                                    override fun onConfirmClick() {
                                        historyRecordBeanDao.deleteAll()
                                        dataBeans.clear()
                                        historyAdapter.notifyDataSetChanged()
                                        emptyView.visibility = View.VISIBLE
                                    }

                                    override fun onCancelClick() {

                                    }
                                }).build().show()
                        }
                    }
                    1 -> {
                        val emailAddress =
                            SaveKeyValues.getValue(Constant.EMAIL_ADDRESS, "") as String
                        if (TextUtils.isEmpty(emailAddress)) {
                            "未设置邮箱，无法导出".show(context)
                            return
                        }
                        if (dataBeans.size == 0) {
                            "无打卡记录，无法导出".show(context)
                            return
                        }
                        AlertControlDialog.Builder()
                            .setContext(context)
                            .setTitle("温馨提示")
                            .setMessage("导出到$emailAddress？")
                            .setNegativeButton("取消")
                            .setPositiveButton("确定")
                            .setOnDialogButtonClickListener(object :
                                AlertControlDialog.OnDialogButtonClickListener {
                                override fun onConfirmClick() {
                                    //导出Excel
                                    exportToEmail(dataBeans)
                                }

                                override fun onCancelClick() {}
                            }).build().show()
                    }
                }
            }
        })
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
                    dataBeans = queryHistoryRecord()
                    refreshLayout.finishRefresh()
                    weakReferenceHandler.sendEmptyMessage(2022021403)
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
                    dataBeans.addAll(queryHistoryRecord())
                    refreshLayout.finishLoadMore()
                    weakReferenceHandler.sendEmptyMessage(2022021403)
                }
            }.start()
        }
    }

    override fun initLayoutView(): Int = R.layout.activity_history

    override fun observeRequestState() {

    }

    private val callback = Handler.Callback { msg: Message ->
        if (msg.what == 2022021403) {
            if (isRefresh || isLoadMore) {
                historyAdapter.notifyDataSetChanged()
            } else { //首次加载数据
                if (dataBeans.size == 0) {
                    emptyView.visibility = View.VISIBLE
                } else {
                    emptyView.visibility = View.GONE
                    historyAdapter = object :
                        NormalRecyclerAdapter<HistoryRecordBean>(
                            R.layout.item_history_rv_l,
                            dataBeans
                        ) {
                        override fun convertView(
                            viewHolder: ViewHolder, position: Int, item: HistoryRecordBean
                        ) {
                            val message: String = item.message
                            if (!message.contains("成功")) {
                                viewHolder.setImageResource(
                                    R.id.tagView, R.drawable.bg_textview_error
                                )
                            }
                            viewHolder.setText(R.id.noticeMessageView, message)
                                .setText(R.id.noticeDateView, item.date)
                        }
                    }
                    historyRecordView.addItemDecoration(
                        DividerItemDecoration(10f.dp2px(context).toFloat(), 0f)
                    )
                    historyRecordView.adapter = historyAdapter
                }
            }
        }
        true
    }

    private fun queryHistoryRecord(): MutableList<HistoryRecordBean> {
        return historyRecordBeanDao.queryBuilder()
            .orderDesc(HistoryRecordBeanDao.Properties.Date)
            .offset(offset * 15).limit(15).list()
    }

    private fun exportToEmail(historyBeans: List<HistoryRecordBean>) {
        //{"date":"2020-04-15","message":"考勤打卡:11:42 下班打卡 早退","uuid":"26btND0uLqU"},{"date":"2020-04-15","message":"考勤打卡:16:32 下班打卡 早退","uuid":"UTWQJzCfTl9"}
        val dir = File(this.getExternalFilesDir(Environment.DIRECTORY_DOCUMENTS), "DingRecord")
        if (!dir.exists()) {
            dir.mkdir()
        }

        val fileName = "$dir/打卡记录表.xls"
        ExcelUtils.initExcel(fileName, excelTitle)
        writeObjToExcel(historyBeans, fileName)
    }
}