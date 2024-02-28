package com.pengxh.autodingding.ui

import android.content.Context
import android.graphics.Color
import android.os.Bundle
import android.os.CountDownTimer
import android.os.Environment
import android.os.Handler
import android.os.Message
import android.text.TextUtils
import android.view.View
import com.pengxh.autodingding.BaseApplication
import com.pengxh.autodingding.R
import com.pengxh.autodingding.bean.HistoryRecordBean
import com.pengxh.autodingding.databinding.ActivityHistoryBinding
import com.pengxh.autodingding.extensions.initImmersionBar
import com.pengxh.autodingding.extensions.writeObjToExcel
import com.pengxh.autodingding.greendao.HistoryRecordBeanDao
import com.pengxh.autodingding.utils.Constant
import com.pengxh.autodingding.utils.ExcelUtils
import com.pengxh.kt.lite.adapter.NormalRecyclerAdapter
import com.pengxh.kt.lite.adapter.ViewHolder
import com.pengxh.kt.lite.base.KotlinBaseActivity
import com.pengxh.kt.lite.divider.RecyclerViewItemDivider
import com.pengxh.kt.lite.extensions.dp2px
import com.pengxh.kt.lite.extensions.show
import com.pengxh.kt.lite.utils.SaveKeyValues
import com.pengxh.kt.lite.utils.WeakReferenceHandler
import com.pengxh.kt.lite.widget.EasyPopupWindow
import com.pengxh.kt.lite.widget.dialog.AlertControlDialog
import com.pengxh.kt.lite.widget.dialog.AlertMessageDialog
import java.io.File

class HistoryRecordActivity : KotlinBaseActivity<ActivityHistoryBinding>() {

    private val context: Context = this@HistoryRecordActivity
    private val images = intArrayOf(R.drawable.ic_delete, R.drawable.ic_export)
    private val titles = arrayOf("删除记录", "导出记录")
    private val excelTitle = arrayOf("uuid", "日期", "打卡信息")
    private val historyRecordBeanDao by lazy { BaseApplication.get().daoSession.historyRecordBeanDao }
    private val easyPopupWindow by lazy { EasyPopupWindow(this) }
    private lateinit var weakReferenceHandler: WeakReferenceHandler
    private lateinit var historyAdapter: NormalRecyclerAdapter<HistoryRecordBean>
    private var dataBeans: MutableList<HistoryRecordBean> = ArrayList()
    private var isRefresh = false
    private var isLoadMore = false
    private var offset = 0 // 本地数据库分页从0开始

    override fun initViewBinding(): ActivityHistoryBinding {
        return ActivityHistoryBinding.inflate(layoutInflater)
    }

    override fun setupTopBarLayout() {
        binding.rootView.initImmersionBar(this, false, R.color.colorAppThemeLight)
        binding.titleView.text = "打卡记录"
        binding.titleRightView.setOnClickListener {
            easyPopupWindow.showAsDropDown(binding.titleRightView, 0, 10.dp2px(context))
        }
    }

    override fun initOnCreate(savedInstanceState: Bundle?) {
        weakReferenceHandler = WeakReferenceHandler(callback)
        dataBeans = queryHistoryRecord()
        weakReferenceHandler.sendEmptyMessage(2022021403)

        val items = ArrayList<EasyPopupWindow.MenuItem>()
        items.add(EasyPopupWindow.MenuItem(images[0], titles[0]))
        items.add(EasyPopupWindow.MenuItem(images[1], titles[1]))
        easyPopupWindow.set(items, object : EasyPopupWindow.OnPopupWindowClickListener {
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
                                        binding.emptyView.visibility = View.VISIBLE
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
        binding.leftBackView.setOnClickListener { finish() }

        binding.refreshLayout.setOnRefreshListener { refreshLayout ->
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
        binding.refreshLayout.setOnLoadMoreListener { refreshLayout ->
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

    override fun observeRequestState() {

    }

    private val callback = Handler.Callback { msg: Message ->
        if (msg.what == 2022021403) {
            if (isRefresh || isLoadMore) {
                historyAdapter.notifyDataSetChanged()
            } else { //首次加载数据
                if (dataBeans.size == 0) {
                    binding.emptyView.visibility = View.VISIBLE
                } else {
                    binding.emptyView.visibility = View.GONE
                    historyAdapter = object :
                        NormalRecyclerAdapter<HistoryRecordBean>(
                            R.layout.item_history_rv_l, dataBeans
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
                    binding.historyRecordView.addItemDecoration(
                        RecyclerViewItemDivider(1, Color.LTGRAY)
                    )
                    binding.historyRecordView.adapter = historyAdapter
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