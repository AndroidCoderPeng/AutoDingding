package com.pengxh.autodingding.ui;

import android.os.Environment;
import android.util.Log;
import android.view.View;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.RelativeLayout;

import com.aihook.alertview.library.AlertView;
import com.google.gson.Gson;
import com.gyf.immersionbar.ImmersionBar;
import com.pengxh.app.multilib.base.BaseNormalActivity;
import com.pengxh.app.multilib.utils.DensityUtil;
import com.pengxh.app.multilib.widget.EasyToast;
import com.pengxh.autodingding.R;
import com.pengxh.autodingding.adapter.HistoryAdapter;
import com.pengxh.autodingding.bean.HistoryBean;
import com.pengxh.autodingding.utils.ExcelUtils;
import com.pengxh.autodingding.utils.SQLiteUtil;
import com.pengxh.autodingding.utils.Utils;
import com.pengxh.autodingding.widgets.EasyPopupWindow;
import com.scwang.smartrefresh.layout.SmartRefreshLayout;

import java.io.File;
import java.util.Arrays;
import java.util.List;

import butterknife.BindView;
import butterknife.OnClick;

public class HistoryActivity extends BaseNormalActivity implements View.OnClickListener {

    @BindView(R.id.titleLayout)
    RelativeLayout titleLayout;
    @BindView(R.id.refreshLayout)
    SmartRefreshLayout refreshLayout;
    @BindView(R.id.emptyView)
    ImageView emptyView;
    @BindView(R.id.historyList)
    ListView historyList;

    private static final String TAG = "HistoryActivity";
    private static final List<String> items = Arrays.asList("删除记录", "导出记录");
    private List<HistoryBean> historyBeans;
    private HistoryAdapter historyAdapter;
    private SQLiteUtil sqLiteUtil;

    @Override
    public void initView() {
        setContentView(R.layout.activity_history);
        ImmersionBar.with(this).fitsSystemWindows(true).statusBarColor(R.color.colorAppThemeLight).init();
    }

    @Override
    public void initData() {
        sqLiteUtil = SQLiteUtil.getInstance();
        historyBeans = sqLiteUtil.loadHistory();
        if (historyBeans.size() == 0) {
            emptyView.setVisibility(View.VISIBLE);
        } else {
            emptyView.setVisibility(View.GONE);
            historyAdapter = new HistoryAdapter(this, historyBeans);
            historyList.setAdapter(historyAdapter);
        }
    }

    @Override
    public void initEvent() {
        refreshLayout.setEnableRefresh(false);
        refreshLayout.setEnableLoadMore(false);
    }

    @OnClick({R.id.settingsView})
    @Override
    public void onClick(View view) {
        EasyPopupWindow easyPopupWindow = new EasyPopupWindow(this, items);
        easyPopupWindow.setPopupWindowClickListener(position -> {
            if (position == 0) {
                //添加导出功能
                if (historyBeans.size() == 0) {
                    new AlertView("温馨提示", "空空如也，无法删除", null, new String[]{"确定"}, null, this, AlertView.Style.Alert,
                            null).setCancelable(false).show();
                } else {
                    new AlertView("温馨提示", "是否确定清除打卡记录？", "取消", new String[]{"确定"}, null, this, AlertView.Style.Alert, (o, position1) -> {
                        if (position1 == 0) {
                            sqLiteUtil.deleteAll();
                            historyBeans.clear();
                            historyAdapter.notifyDataSetChanged();
                            emptyView.setVisibility(View.VISIBLE);
                        }
                    }).setCancelable(false).show();
                }
            } else if (position == 1) {
                String emailAddress = Utils.readEmailAddress();
                if (emailAddress.equals("")) {
                    EasyToast.showToast("未设置邮箱，无法导出", EasyToast.WARING);
                    return;
                }
                if (historyBeans.size() == 0) {
                    EasyToast.showToast("无打卡记录，无法导出", EasyToast.WARING);
                    return;
                }
                new AlertView("温馨提示", "导出到" + emailAddress, "取消", new String[]{"确定"}, null, this, AlertView.Style.Alert, (o, position1) -> {
                    if (position1 == 0) {
                        //导出Excel
                        pullToEmail(historyBeans);
                    }
                }).setCancelable(false).show();
            }
        });
        easyPopupWindow.showAsDropDown(titleLayout, titleLayout.getWidth(), DensityUtil.dp2px(this, 1));
    }

    private static final String[] title = {"uuid", "日期", "打卡信息"};

    private void pullToEmail(List<HistoryBean> historyBeans) {
        //{"date":"2020-04-15","message":"考勤打卡:11:42 下班打卡 早退","uuid":"26btND0uLqU"},{"date":"2020-04-15","message":"考勤打卡:16:32 下班打卡 早退","uuid":"UTWQJzCfTl9"}
        Log.d(TAG, "pullToEmail: " + new Gson().toJson(historyBeans));
        File dir = new File(Environment.getExternalStorageDirectory(), "DingRecord");
        if (!dir.exists()) {
            dir.mkdir();
        }
        ExcelUtils.initExcel(dir.toString() + "/打卡记录表.xls", title);
        String fileName = Environment.getExternalStorageDirectory() + "/DingRecord/打卡记录表.xls";
        ExcelUtils.writeObjListToExcel(historyBeans, fileName, this);
    }
}
