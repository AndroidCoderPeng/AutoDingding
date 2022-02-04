package com.pengxh.autodingding.ui;

import android.os.CountDownTimer;
import android.os.Environment;
import android.os.Handler;
import android.os.Message;
import android.util.Log;
import android.view.View;

import androidx.annotation.NonNull;
import androidx.core.content.ContextCompat;

import com.google.gson.Gson;
import com.gyf.immersionbar.ImmersionBar;
import com.pengxh.app.multilib.utils.SizeUtil;
import com.pengxh.app.multilib.widget.EasyToast;
import com.pengxh.app.multilib.widget.dialog.AlertControlDialog;
import com.pengxh.app.multilib.widget.dialog.AlertMessageDialog;
import com.pengxh.autodingding.AndroidxBaseActivity;
import com.pengxh.autodingding.BaseApplication;
import com.pengxh.autodingding.R;
import com.pengxh.autodingding.adapter.HistoryRecordAdapter;
import com.pengxh.autodingding.bean.HistoryRecordBean;
import com.pengxh.autodingding.databinding.ActivityHistoryBinding;
import com.pengxh.autodingding.greendao.HistoryRecordBeanDao;
import com.pengxh.autodingding.utils.ExcelUtils;
import com.pengxh.autodingding.utils.StatusBarColorUtil;
import com.pengxh.autodingding.utils.Utils;
import com.pengxh.autodingding.widgets.EasyPopupWindow;

import java.io.File;
import java.lang.ref.WeakReference;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class HistoryRecordActivity extends AndroidxBaseActivity<ActivityHistoryBinding> implements View.OnClickListener {

    private static final String TAG = "HistoryActivity";
    private static final List<String> items = Arrays.asList("删除记录", "导出记录");
    private static final String[] excelTitle = {"uuid", "日期", "打卡信息"};
    private WeakReferenceHandler weakReferenceHandler;
    private HistoryRecordBeanDao recordBeanDao;
    private List<HistoryRecordBean> dataBeans = new ArrayList<>();
    private boolean isRefresh = false;
    private HistoryRecordAdapter historyAdapter;

    @Override
    protected void setupTopBarLayout() {
        StatusBarColorUtil.setColor(this, ContextCompat.getColor(this, R.color.colorAppThemeLight));
        ImmersionBar.with(this).statusBarDarkFont(false).init();
        viewBinding.titleView.setText("自动打卡");
        viewBinding.titleRightView.setOnClickListener(this);
    }

    @Override
    public void initData() {
        weakReferenceHandler = new WeakReferenceHandler(this);
        recordBeanDao = BaseApplication.getDaoSession().getHistoryRecordBeanDao();
        dataBeans = recordBeanDao.loadAll();
        weakReferenceHandler.sendEmptyMessage(20220103);
    }

    @Override
    public void initEvent() {
        viewBinding.refreshLayout.setOnRefreshListener(layout -> {
            isRefresh = true;
            new CountDownTimer(1500, 500) {

                @Override
                public void onTick(long millisUntilFinished) {

                }

                @Override
                public void onFinish() {
                    dataBeans.clear();
                    dataBeans = recordBeanDao.loadAll();
                    layout.finishRefresh();
                    isRefresh = false;
                    weakReferenceHandler.sendEmptyMessage(20220103);
                }
            }.start();
//            BroadcastManager.getInstance(this).sendBroadcast(Constant.ACTION_UPDATE, "update");
        });
        viewBinding.refreshLayout.setEnableLoadMore(false);
    }

    private static class WeakReferenceHandler extends Handler {

        private final WeakReference<HistoryRecordActivity> reference;

        private WeakReferenceHandler(HistoryRecordActivity activity) {
            reference = new WeakReference<>(activity);
        }

        @Override
        public void handleMessage(@NonNull Message msg) {
            super.handleMessage(msg);
            HistoryRecordActivity activity = reference.get();
            if (msg.what == 20220103) {
                if (activity.isRefresh) {
                    activity.historyAdapter.notifyDataSetChanged();
                } else { //首次加载数据
                    if (activity.dataBeans.size() == 0) {
                        activity.viewBinding.emptyView.setVisibility(View.VISIBLE);
                    } else {
                        activity.viewBinding.emptyView.setVisibility(View.GONE);
                        activity.historyAdapter = new HistoryRecordAdapter(activity, activity.dataBeans);
                        activity.viewBinding.historyListView.setAdapter(activity.historyAdapter);
                    }
                }
            }
        }
    }

    @Override
    public void onClick(View view) {
        EasyPopupWindow easyPopupWindow = new EasyPopupWindow(this, items);
        easyPopupWindow.setPopupWindowClickListener(position -> {
            if (position == 0) {
                //添加导出功能
                if (dataBeans.size() == 0) {
                    new AlertMessageDialog.Builder()
                            .setContext(this)
                            .setTitle("温馨提示")
                            .setMessage("空空如也，无法删除")
                            .setPositiveButton("确定")
                            .setOnDialogButtonClickListener(new AlertMessageDialog.OnDialogButtonClickListener() {
                                @Override
                                public void onConfirmClick() {

                                }
                            }).build().show();
                } else {
                    new AlertControlDialog.Builder()
                            .setContext(this)
                            .setTitle("温馨提示")
                            .setMessage("是否确定清除打卡记录？")
                            .setNegativeButton("取消")
                            .setPositiveButton("确定")
                            .setOnDialogButtonClickListener(new AlertControlDialog.OnDialogButtonClickListener() {
                                @Override
                                public void onConfirmClick() {
                                    recordBeanDao.deleteAll();
                                    dataBeans.clear();
                                    historyAdapter.notifyDataSetChanged();
//                            emptyView.setVisibility(View.VISIBLE);
//                            BroadcastManager.getInstance(this).sendBroadcast(Constant.ACTION_UPDATE, "update");
                                }

                                @Override
                                public void onCancelClick() {

                                }
                            }).build().show();
                }
            } else if (position == 1) {
                String emailAddress = Utils.readEmailAddress();
                if (emailAddress.equals("")) {
                    EasyToast.showToast("未设置邮箱，无法导出", EasyToast.WARING);
                    return;
                }
                if (dataBeans.size() == 0) {
                    EasyToast.showToast("无打卡记录，无法导出", EasyToast.WARING);
                    return;
                }
                new AlertControlDialog.Builder()
                        .setContext(this)
                        .setTitle("温馨提示")
                        .setMessage("导出到" + emailAddress + "？")
                        .setNegativeButton("取消")
                        .setPositiveButton("确定")
                        .setOnDialogButtonClickListener(new AlertControlDialog.OnDialogButtonClickListener() {
                            @Override
                            public void onConfirmClick() {
                                //导出Excel
                                pullToEmail(dataBeans);
                            }

                            @Override
                            public void onCancelClick() {

                            }
                        }).build().show();
            }
        });
        easyPopupWindow.showAsDropDown(viewBinding.titleRightView
                , viewBinding.titleRightView.getWidth()
                , SizeUtil.dp2px(this, 10));
    }

    private void pullToEmail(List<HistoryRecordBean> historyBeans) {
        //{"date":"2020-04-15","message":"考勤打卡:11:42 下班打卡 早退","uuid":"26btND0uLqU"},{"date":"2020-04-15","message":"考勤打卡:16:32 下班打卡 早退","uuid":"UTWQJzCfTl9"}
        Log.d(TAG, "pullToEmail: " + new Gson().toJson(historyBeans));
        File dir = new File(this.getExternalFilesDir(Environment.DIRECTORY_DOCUMENTS), "DingRecord");
        if (!dir.exists()) {
            dir.mkdir();
        }
        ExcelUtils.initExcel(dir + "/打卡记录表.xls", excelTitle);
        String fileName = dir + "/打卡记录表.xls";
        ExcelUtils.writeObjListToExcel(historyBeans, fileName);
    }
}
