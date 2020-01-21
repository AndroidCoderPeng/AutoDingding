package com.pengxh.autodingding.ui;

import android.app.Dialog;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.KeyEvent;
import android.view.View;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.aihook.alertview.library.AlertView;
import com.aihook.alertview.library.OnItemClickListener;
import com.gyf.immersionbar.ImmersionBar;
import com.pengxh.app.multilib.base.BaseNormalActivity;
import com.pengxh.app.multilib.widget.EasyToast;
import com.pengxh.app.multilib.widget.dialog.InputDialog;
import com.pengxh.autodingding.R;
import com.pengxh.autodingding.adapter.DingDingClockAdapter;
import com.pengxh.autodingding.bean.TimeSetBean;
import com.pengxh.autodingding.utils.Constant;
import com.pengxh.autodingding.utils.SQLiteUtil;
import com.pengxh.autodingding.utils.TimeOrDateUtil;
import com.pengxh.autodingding.utils.Utils;
import com.pengxh.autodingding.widgets.EasyPopupWindow;

import java.util.Arrays;
import java.util.List;

import butterknife.BindView;
import butterknife.OnClick;

/**
 * @author: Pengxh
 * @email: 290677893@qq.com
 * @description: TODO
 * @date: 2020/1/19 16:19
 */
public class DingDingClockActivity extends BaseNormalActivity
        implements View.OnClickListener, OnItemClickListener {

    private static final String TAG = "DingDingClockActivity";
    private static final List<String> items = Arrays.asList("功能介绍", "邮箱设置");

    @BindView(R.id.titleLayout)
    RelativeLayout titleLayout;
    @BindView(R.id.textViewTitle)
    TextView textViewTitle;
    @BindView(R.id.imageViewTitleRight)
    ImageView imageViewTitleRight;
    @BindView(R.id.clockRecyclerView)
    RecyclerView clockRecyclerView;

    private AlertView alertView;
    private SQLiteUtil sqLiteUtil;

    @Override
    public void initView() {
        setContentView(R.layout.activity_clock);
        ImmersionBar.with(this).fitsSystemWindows(true).statusBarColor(R.color.colorAppThemeLight).init();
    }

    @Override
    public void initData() {
        String qqEmail = Utils.readEmailAddress();
        if (!qqEmail.equals("")) {
            textViewTitle.setText("打卡通知邮箱：" + qqEmail);
        }
        sqLiteUtil = SQLiteUtil.getInstance();
    }

    @Override
    public void initEvent() {
        if (Utils.isAppAvailable(Constant.DINGDING)) {
            DingDingClockAdapter clockAdapter = new DingDingClockAdapter(this, TimeOrDateUtil.getDateList(), getSupportFragmentManager());
            clockRecyclerView.setLayoutManager(new LinearLayoutManager(this));
            clockRecyclerView.setAdapter(clockAdapter);
        } else {
            alertView = new AlertView("温馨提示", "手机没有安装钉钉软件，无法自动打卡", null, new String[]{"确定"}, null, this, AlertView.Style.Alert,
                    (o, position) -> {
                        alertView.dismiss();
                        DingDingClockActivity.this.finish();
                    }).setCancelable(false);
            alertView.show();
        }
    }

    @OnClick({R.id.imageViewTitleRight})
    @Override
    public void onClick(View v) {
        if (v.getId() == R.id.imageViewTitleRight) {
            EasyPopupWindow easyPopupWindow = new EasyPopupWindow(this, items);
            easyPopupWindow.setPopupWindowClickListener(new EasyPopupWindow.PopupWindowClickListener() {
                @Override
                public void popupWindowClick(int position) {
                    if (position == 0) {
                        alertView = new AlertView("功能介绍", getResources().getString(R.string.about),
                                null, new String[]{"确定"}, null,
                                DingDingClockActivity.this, AlertView.Style.Alert,
                                null).setCancelable(false);
                        alertView.show();
                    } else if (position == 1) {
                        setEmailAddress();
                    }
                }
            });
            easyPopupWindow.showAsDropDown(titleLayout, titleLayout.getWidth(), 0);
        }
    }

    private void setEmailAddress() {
        new InputDialog.Builder().setContext(this).setTitle("设置邮箱").setNegativeButton("取消").setPositiveButton("确定").setOnDialogClickListener(new InputDialog.onDialogClickListener() {
            @Override
            public void onConfirmClick(Dialog dialog, String input) {
                if (!input.isEmpty()) {
                    if (input.endsWith("@qq.com")) {
                        Utils.saveEmailAddress(input);
                        textViewTitle.setText("打卡通知邮箱：" + input);
                    } else {
                        EasyToast.showToast("邮箱设置失败，暂时只支持QQ邮箱！", EasyToast.WARING);
                    }
                    dialog.dismiss();
                } else {
                    EasyToast.showToast("什么都还没输入呢！", EasyToast.ERROR);
                }
            }

            @Override
            public void onCancelClick(Dialog dialog) {
                dialog.dismiss();
            }
        }).build().show();
    }

    //屏蔽返回键
    @Override
    public boolean onKeyDown(int keyCode, KeyEvent event) {
        if (keyCode == KeyEvent.KEYCODE_BACK) {
            List<TimeSetBean> timeSetBeans = sqLiteUtil.loadAllTimeSet();
            for (TimeSetBean timeSetBean : timeSetBeans) {
                Log.d(TAG, "onKeyDown: " + timeSetBean.getStartTime());
            }
            if (timeSetBeans.size() == 0) {
                finish();
            } else {
                alertView = new AlertView("温馨提示", "是否强制退出？", "取消", new String[]{"确定"}, null,
                        this, AlertView.Style.Alert, this).setCancelable(false);
                alertView.show();
            }
            return true;
        }
        return false;
    }

    @Override
    public void onItemClick(Object o, int position) {
        switch (position) {
            case 0:
                sqLiteUtil.deleteAll();
                alertView.dismiss();
                finish();
                break;
            default:
                break;
        }
    }
}