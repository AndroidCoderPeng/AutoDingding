package com.pengxh.autodingding.ui;

import android.os.CountDownTimer;
import android.os.Environment;
import android.os.Handler;
import android.text.TextUtils;
import android.view.View;

import androidx.recyclerview.widget.LinearLayoutManager;

import com.gyf.immersionbar.ImmersionBar;
import com.pengxh.androidx.lite.base.AndroidxBaseActivity;
import com.pengxh.androidx.lite.utils.ColorUtil;
import com.pengxh.androidx.lite.utils.DeviceSizeUtil;
import com.pengxh.androidx.lite.utils.ImmerseStatusBarUtil;
import com.pengxh.androidx.lite.utils.SaveKeyValues;
import com.pengxh.androidx.lite.utils.WeakReferenceHandler;
import com.pengxh.androidx.lite.widget.EasyPopupWindow;
import com.pengxh.androidx.lite.widget.EasyToast;
import com.pengxh.androidx.lite.widget.dialog.AlertControlDialog;
import com.pengxh.androidx.lite.widget.dialog.AlertMessageDialog;
import com.pengxh.autodingding.BaseApplication;
import com.pengxh.autodingding.R;
import com.pengxh.autodingding.adapter.HistoryRecordAdapter;
import com.pengxh.autodingding.bean.HistoryRecordBean;
import com.pengxh.autodingding.databinding.ActivityHistoryBinding;
import com.pengxh.autodingding.greendao.DaoSession;
import com.pengxh.autodingding.utils.Constant;
import com.pengxh.autodingding.utils.ExcelUtils;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

public class HistoryRecordActivity extends AndroidxBaseActivity<ActivityHistoryBinding> {

    private static final String TAG = "HistoryRecordActivity";
    private static final int[] images = new int[]{R.drawable.ic_delete, R.drawable.ic_export};
    private static final String[] titles = new String[]{"删除记录", "导出记录"};
    private static final String[] excelTitle = {"uuid", "日期", "打卡信息"};
    private EasyPopupWindow easyPopupWindow;
    private WeakReferenceHandler weakReferenceHandler;
    private DaoSession daoSession;
    private List<HistoryRecordBean> dataBeans = new ArrayList<>();
    private boolean isRefresh = false;
    private boolean isLoadMore = false;
    private int offset = 0;// 本地数据库分页从0开始
    private HistoryRecordAdapter historyAdapter;


    @Override
    protected void setupTopBarLayout() {
        ImmerseStatusBarUtil.setColor(this, ColorUtil.convertColor(this, R.color.colorAppThemeLight));
        ImmersionBar.with(this).statusBarDarkFont(false).init();
        viewBinding.titleView.setText("打卡记录");
        viewBinding.titleRightView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                easyPopupWindow.showAsDropDown(viewBinding.titleRightView
                        , 0
                        , DeviceSizeUtil.dp2px(HistoryRecordActivity.this, 10));
            }
        });
    }

    @Override
    public void initData() {
        weakReferenceHandler = new WeakReferenceHandler(callback);
        daoSession = BaseApplication.getInstance().getDaoSession();

        dataBeans = queryHistoryRecord();
        weakReferenceHandler.sendEmptyMessage(2022021403);

        easyPopupWindow = new EasyPopupWindow(this);
        easyPopupWindow.setPopupMenuItem(images, titles);
        easyPopupWindow.setOnPopupWindowClickListener(position -> {
            if (position == 0) {
                //添加导出功能
                if (dataBeans.size() == 0) {
                    new AlertMessageDialog.Builder()
                            .setContext(this)
                            .setTitle("温馨提示")
                            .setMessage("空空如也，无法删除")
                            .setPositiveButton("确定")
                            .setOnDialogButtonClickListener(() -> {

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
                                    daoSession.getHistoryRecordBeanDao().deleteAll();
                                    dataBeans.clear();
                                    historyAdapter.notifyDataSetChanged();
                                    viewBinding.emptyView.setVisibility(View.VISIBLE);
                                }

                                @Override
                                public void onCancelClick() {

                                }
                            }).build().show();
                }
            } else if (position == 1) {
                String emailAddress = (String) SaveKeyValues.getValue(Constant.EMAIL_ADDRESS, "");
                if (TextUtils.isEmpty(emailAddress)) {
                    EasyToast.show(this, "未设置邮箱，无法导出");
                    return;
                }
                if (dataBeans.size() == 0) {
                    EasyToast.show(this, "无打卡记录，无法导出");
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
                                exportToEmail(dataBeans);
                            }

                            @Override
                            public void onCancelClick() {

                            }
                        }).build().show();
            }
        });
    }

    @Override
    public void initEvent() {
        viewBinding.refreshLayout.setOnRefreshListener(refreshLayout -> {
            isRefresh = true;
            new CountDownTimer(1000, 500) {

                @Override
                public void onTick(long millisUntilFinished) {

                }

                @Override
                public void onFinish() {
                    isRefresh = false;
                    dataBeans.clear();
                    offset = 0;
                    dataBeans = queryHistoryRecord();

                    refreshLayout.finishRefresh();
                    weakReferenceHandler.sendEmptyMessage(2022021403);
                }
            }.start();
        });

        viewBinding.refreshLayout.setOnLoadMoreListener(refreshLayout -> {
            isLoadMore = true;
            new CountDownTimer(1000, 500) {

                @Override
                public void onTick(long millisUntilFinished) {

                }

                @Override
                public void onFinish() {
                    isLoadMore = false;
                    offset++;
                    dataBeans.addAll(queryHistoryRecord());

                    refreshLayout.finishLoadMore();
                    weakReferenceHandler.sendEmptyMessage(2022021403);
                }
            }.start();
        });
    }

    private final Handler.Callback callback = msg -> {
        if (msg.what == 2022021403) {
            if (isRefresh || isLoadMore) {
                historyAdapter.notifyDataSetChanged();
            } else { //首次加载数据
                if (dataBeans.size() == 0) {
                    viewBinding.emptyView.setVisibility(View.VISIBLE);
                } else {
                    viewBinding.emptyView.setVisibility(View.GONE);
                    historyAdapter = new HistoryRecordAdapter(this, dataBeans);
                    viewBinding.historyRecordView.setLayoutManager(new LinearLayoutManager(this));
                    viewBinding.historyRecordView.setAdapter(historyAdapter);
                }
            }
        }
        return true;
    };

    private List<HistoryRecordBean> queryHistoryRecord() {
        return daoSession.queryBuilder(HistoryRecordBean.class).offset(offset * 15).limit(15).list();
    }

    private void exportToEmail(List<HistoryRecordBean> historyBeans) {
        //{"date":"2020-04-15","message":"考勤打卡:11:42 下班打卡 早退","uuid":"26btND0uLqU"},{"date":"2020-04-15","message":"考勤打卡:16:32 下班打卡 早退","uuid":"UTWQJzCfTl9"}
        File dir = new File(this.getExternalFilesDir(Environment.DIRECTORY_DOCUMENTS), "DingRecord");
        if (!dir.exists()) {
            dir.mkdir();
        }
        ExcelUtils.initExcel(dir + "/打卡记录表.xls", excelTitle);
        String fileName = dir + "/打卡记录表.xls";
        ExcelUtils.writeObjListToExcel(this, historyBeans, fileName);
    }
}
