package com.pengxh.autodingding.ui;

import android.os.Environment;
import android.util.Log;
import android.view.View;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.TextView;

import com.aihook.alertview.library.AlertView;
import com.google.gson.Gson;
import com.pengxh.app.multilib.base.BaseNormalActivity;
import com.pengxh.app.multilib.utils.BroadcastManager;
import com.pengxh.app.multilib.utils.DensityUtil;
import com.pengxh.app.multilib.widget.EasyToast;
import com.pengxh.autodingding.R;
import com.pengxh.autodingding.adapter.HistoryAdapter;
import com.pengxh.autodingding.bean.HistoryBean;
import com.pengxh.autodingding.utils.Constant;
import com.pengxh.autodingding.utils.ExcelUtils;
import com.pengxh.autodingding.utils.SQLiteUtil;
import com.pengxh.autodingding.utils.Utils;
import com.pengxh.autodingding.widgets.EasyPopupWindow;
import com.scwang.smartrefresh.layout.SmartRefreshLayout;

import java.io.File;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import butterknife.BindView;
import butterknife.OnClick;

public class HistoryActivity extends BaseNormalActivity implements View.OnClickListener {

    @BindView(R.id.parentLayout)
    LinearLayout parentLayout;
    @BindView(R.id.mTitleLeftView)
    ImageView mTitleLeftView;
    @BindView(R.id.mTitleView)
    TextView mTitleView;
    @BindView(R.id.mTitleRightView)
    ImageView mTitleRightView;
    @BindView(R.id.refreshLayout)
    SmartRefreshLayout refreshLayout;
    @BindView(R.id.emptyView)
    ImageView emptyView;
    @BindView(R.id.historyList)
    ListView historyList;

    private static final String TAG = "HistoryActivity";
    private static final List<String> items = Arrays.asList("删除记录", "导出记录");
    private static final String[] excelTitle = {"uuid", "日期", "打卡信息"};
    private List<HistoryBean> recordData = new ArrayList<>();
    private HistoryAdapter historyAdapter;
    private SQLiteUtil sqLiteUtil;

    @Override
    public int initLayoutView() {
        return R.layout.activity_history;
    }

    @Override
    public void initData() {
        mTitleLeftView.setVisibility(View.GONE);
        mTitleView.setText("自动打卡");
        mTitleRightView.setBackgroundResource(R.drawable.settings);

        loadRecord();
    }

    @Override
    public void initEvent() {
        refreshLayout.setOnRefreshListener(refreshLayout -> {
            loadRecord();
            refreshLayout.finishRefresh();
            BroadcastManager.getInstance(this).sendBroadcast(Constant.ACTION_UPDATE, "update");
        });
        refreshLayout.setEnableLoadMore(false);
    }

    private void loadRecord() {
        sqLiteUtil = SQLiteUtil.getInstance();
        recordData = sqLiteUtil.loadHistory();
        if (recordData.size() == 0) {
            emptyView.setVisibility(View.VISIBLE);
        } else {
            emptyView.setVisibility(View.GONE);
            historyAdapter = new HistoryAdapter(this, recordData);
            historyList.setAdapter(historyAdapter);
        }
    }

    @OnClick({R.id.mTitleRightView})
    @Override
    public void onClick(View view) {
        EasyPopupWindow easyPopupWindow = new EasyPopupWindow(this, items);
        easyPopupWindow.setPopupWindowClickListener(position -> {
            if (position == 0) {
                //添加导出功能
                if (recordData.size() == 0) {
                    new AlertView("温馨提示", "空空如也，无法删除", null, new String[]{"确定"}, null, this, AlertView.Style.Alert,
                            null).setCancelable(false).show();
                } else {
                    new AlertView("温馨提示", "是否确定清除打卡记录？", "取消", new String[]{"确定"}, null, this, AlertView.Style.Alert, (o, position1) -> {
                        if (position1 == 0) {
                            sqLiteUtil.deleteAll();
                            recordData.clear();
                            historyAdapter.notifyDataSetChanged();
                            emptyView.setVisibility(View.VISIBLE);
                            BroadcastManager.getInstance(this).sendBroadcast(Constant.ACTION_UPDATE, "update");
                        }
                    }).setCancelable(false).show();
                }
            } else if (position == 1) {
                String emailAddress = Utils.readEmailAddress();
                if (emailAddress.equals("")) {
                    EasyToast.showToast("未设置邮箱，无法导出", EasyToast.WARING);
                    return;
                }
                if (recordData.size() == 0) {
                    EasyToast.showToast("无打卡记录，无法导出", EasyToast.WARING);
                    return;
                }
                new AlertView("温馨提示", "导出到" + emailAddress, "取消", new String[]{"确定"}, null, this, AlertView.Style.Alert, (o, pos) -> {
                    if (pos == 0) {
                        //导出Excel
                        pullToEmail(recordData);
                    }
                }).setCancelable(false).show();
            }
        });
        easyPopupWindow.showAsDropDown(mTitleRightView
                , mTitleRightView.getWidth()
                , DensityUtil.dp2px(this, 10));
    }

    private void pullToEmail(List<HistoryBean> historyBeans) {
        //{"date":"2020-04-15","message":"考勤打卡:11:42 下班打卡 早退","uuid":"26btND0uLqU"},{"date":"2020-04-15","message":"考勤打卡:16:32 下班打卡 早退","uuid":"UTWQJzCfTl9"}
        Log.d(TAG, "pullToEmail: " + new Gson().toJson(historyBeans));
        File dir = new File(Environment.getExternalStorageDirectory(), "DingRecord");
        if (!dir.exists()) {
            dir.mkdir();
        }
        ExcelUtils.initExcel(dir.toString() + "/打卡记录表.xls", excelTitle);
        String fileName = Environment.getExternalStorageDirectory() + "/DingRecord/打卡记录表.xls";
        ExcelUtils.writeObjListToExcel(historyBeans, fileName);
    }
}
