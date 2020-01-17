package com.pengxh.autodingding.ui;

import android.annotation.SuppressLint;
import android.app.Dialog;
import android.arch.lifecycle.MutableLiveData;
import android.arch.lifecycle.Observer;
import android.content.Intent;
import android.support.annotation.Nullable;
import android.util.Log;
import android.view.KeyEvent;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.aihook.alertview.library.AlertView;
import com.aihook.alertview.library.OnItemClickListener;
import com.gyf.immersionbar.ImmersionBar;
import com.jzxiang.pickerview.TimePickerDialog;
import com.jzxiang.pickerview.data.Type;
import com.jzxiang.pickerview.listener.OnDateSetListener;
import com.pengxh.app.multilib.base.BaseNormalActivity;
import com.pengxh.app.multilib.utils.ColorUtil;
import com.pengxh.app.multilib.utils.SaveKeyValues;
import com.pengxh.app.multilib.widget.EasyToast;
import com.pengxh.app.multilib.widget.dialog.InputDialog;
import com.pengxh.autodingding.R;
import com.pengxh.autodingding.service.AutoDingdingService;
import com.pengxh.autodingding.utils.Constant;
import com.pengxh.autodingding.utils.HttpCallbackListener;
import com.pengxh.autodingding.utils.LiveDataBus;
import com.pengxh.autodingding.utils.TimeOrDateUtil;
import com.pengxh.autodingding.utils.Utils;
import com.pengxh.autodingding.widgets.EasyPopupWindow;

import java.util.Arrays;
import java.util.List;

import butterknife.BindView;
import butterknife.OnClick;

public class MainActivity extends BaseNormalActivity implements View.OnClickListener {

    private static final String TAG = "MainActivity";
    private static final List<String> items = Arrays.asList("功能介绍", "邮箱设置");

    @BindView(R.id.titleLayout)
    RelativeLayout titleLayout;
    @BindView(R.id.textViewTitle)
    TextView textViewTitle;
    @BindView(R.id.imageViewTitleRight)
    ImageView imageViewTitleRight;
    @BindView(R.id.startTimeBtn)
    Button startTimeBtn;
    @BindView(R.id.endTimeBtn)
    Button endTimeBtn;
    @BindView(R.id.amTime)
    TextView amTime;
    @BindView(R.id.pmTime)
    TextView pmTime;
    @BindView(R.id.startProgressBar)
    ProgressBar startProgressBar;
    @BindView(R.id.endProgressBar)
    ProgressBar endProgressBar;

    private AlertView alertView;
    private Observer<Integer> amUpdateObserver, pmUpdateObserver;
    private MutableLiveData<Integer> amUpdateLiveData, pmUpdateLiveData;

    @Override
    public void initView() {
        setContentView(R.layout.activity_main);
        ImmersionBar.with(this).fitsSystemWindows(true).statusBarColor(R.color.colorAppThemeLight).init();
    }

    @Override
    public void initData() {
        amUpdateLiveData = LiveDataBus.get().with("amUpdate", int.class);
        pmUpdateLiveData = LiveDataBus.get().with("pmUpdate", int.class);
        String qqEmail = Utils.readEmailAddress();
        String amKaoQin = (String) SaveKeyValues.getValue("amKaoQin", "");
        String pmKaoQin = (String) SaveKeyValues.getValue("pmKaoQin", "");
        if (!amKaoQin.equals("")) {
            amTime.setText("将在" + amKaoQin + "自动打卡");
        } else {
            amTime.setText("上班打卡时间未设置");
        }
        if (!pmKaoQin.equals("")) {
            pmTime.setText("将在" + pmKaoQin + "自动打卡");
        } else {
            pmTime.setText("下班打卡时间未设置");
        }
        if (!qqEmail.equals("")) {
            textViewTitle.setText("打卡通知邮箱：" + qqEmail);
        }

        Utils.doHttpRequest(Constant.BASE_URL + "2020-01-01", new HttpCallbackListener() {
            @Override
            public void onError(Exception e) {
                Log.d(TAG, "onError: " + e);
            }

            @Override
            public void onSuccess(String response) {
                Log.d(TAG, "onSuccess: " + response);
            }
        });
    }

    @Override
    public void initEvent() {
        if (Utils.isAppAvailable(Constant.DINGDING)) {
            startService(new Intent(this, AutoDingdingService.class));

            //监听AutoDingdingService返回来的消息，更新UI
            amUpdateObserver = new Observer<Integer>() {
                @SuppressLint("SetTextI18n")
                @Override
                public void onChanged(@Nullable Integer integer) {
                    startTimeBtn.setText(integer + "s");
                    startProgressBar.setProgress(integer);
                    SaveKeyValues.putValue("progress", integer);
                    if (startProgressBar.getProgress() == 0) {
                        //重置所有状态
                        amTime.setText("上班打卡时间未设置");
                        startTimeBtn.setText("上班设置");
                        startProgressBar.setVisibility(View.GONE);
                        SaveKeyValues.clearAll();
                    }
                }
            };
            amUpdateLiveData.observeForever(amUpdateObserver);

            pmUpdateObserver = new Observer<Integer>() {
                @SuppressLint("SetTextI18n")
                @Override
                public void onChanged(@Nullable Integer integer) {
                    endTimeBtn.setText(integer + "s");
                    endProgressBar.setProgress(integer);
                    SaveKeyValues.putValue("progress", integer);
                    if (endProgressBar.getProgress() == 0) {
                        //重置所有状态
                        pmTime.setText("下班打卡时间未设置");
                        endTimeBtn.setText("下班设置");
                        endProgressBar.setVisibility(View.GONE);
                        SaveKeyValues.clearAll();
                    }
                }
            };
            pmUpdateLiveData.observeForever(pmUpdateObserver);
        } else {
            alertView = new AlertView("温馨提示", "手机没有安装钉钉软件，无法自动打卡",
                    null, new String[]{"确定"}, null,
                    this, AlertView.Style.Alert,
                    new OnItemClickListener() {
                        @Override
                        public void onItemClick(Object o, int position) {
                            alertView.dismiss();
                            MainActivity.this.finish();
                        }
                    }).setCancelable(false);
            alertView.show();
        }
    }

    @OnClick({R.id.startTimeBtn, R.id.endTimeBtn, R.id.imageViewTitleRight})
    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.startTimeBtn:
                //设置上班时间
                new TimePickerDialog.Builder().setThemeColor(ColorUtil.getRandomColor())
                        .setType(Type.ALL)
                        .setCallBack(new OnDateSetListener() {
                            @Override
                            public void onDateSet(TimePickerDialog timePickerView, long millSeconds) {
                                //计算时间差
                                long deltaTime = TimeOrDateUtil.deltaTime(millSeconds / 1000);
                                Log.d(TAG, "amKaoQin: " + deltaTime);
                                if (deltaTime == 0) {
                                    Log.w(TAG, "", new Throwable());
                                    return;
                                }
                                String amKaoQin = TimeOrDateUtil.timestampToDate(millSeconds);
                                amTime.setText("将在" + amKaoQin + "自动打卡");
                                SaveKeyValues.putValue("amKaoQin", amKaoQin);
                                int currentPro = startProgressBar.getProgress();
                                if (currentPro != 0) {
                                    EasyToast.showToast("当前已有定时任务，无法重复设置", EasyToast.ERROR);
                                } else {
                                    startProgressBar.setMax((int) deltaTime);
                                    startProgressBar.setVisibility(View.VISIBLE);
                                    LiveDataBus.get().with("amKaoQin").setValue(deltaTime);
                                }
                            }
                        }).build().show(getSupportFragmentManager(), "year_month_day_hour_minute");
                break;
            case R.id.endTimeBtn:
                //设置下班时间
                new TimePickerDialog.Builder().setThemeColor(ColorUtil.getRandomColor())
                        .setType(Type.ALL)
                        .setCallBack(new OnDateSetListener() {
                            @Override
                            public void onDateSet(TimePickerDialog timePickerView, long millSeconds) {
                                //计算时间差
                                long deltaTime = TimeOrDateUtil.deltaTime(millSeconds / 1000);
                                Log.d(TAG, "pmKaoQin: " + deltaTime);
                                if (deltaTime == 0) {
                                    Log.w(TAG, "", new Throwable());
                                    return;
                                }
                                String pmKaoQin = TimeOrDateUtil.timestampToDate(millSeconds);
                                pmTime.setText("将在" + pmKaoQin + "自动打卡");
                                SaveKeyValues.putValue("pmKaoQin", pmKaoQin);
                                int currentPro = endProgressBar.getProgress();
                                if (currentPro != 0) {
                                    EasyToast.showToast("当前已有定时任务，无法重复设置", EasyToast.ERROR);
                                } else {
                                    endProgressBar.setMax((int) deltaTime);
                                    endProgressBar.setVisibility(View.VISIBLE);
                                    LiveDataBus.get().with("pmKaoQin").setValue(deltaTime);
                                }
                            }
                        }).build().show(getSupportFragmentManager(), "year_month_day_hour_minute");
                break;
            case R.id.imageViewTitleRight:
                EasyPopupWindow easyPopupWindow = new EasyPopupWindow(this, items);
                easyPopupWindow.setPopupWindowClickListener(new EasyPopupWindow.PopupWindowClickListener() {
                    @Override
                    public void popupWindowClick(int position) {
                        if (position == 0) {
                            new AlertView("功能介绍", getResources().getString(R.string.about),
                                    null, new String[]{"确定"}, null,
                                    MainActivity.this, AlertView.Style.Alert,
                                    null).setCancelable(false).show();
                        } else if (position == 1) {
                            setEmailAddress();
                        }
                    }
                });
                easyPopupWindow.showAsDropDown(titleLayout, titleLayout.getWidth(), 0);
                break;
        }
    }

    private void setEmailAddress() {
        new InputDialog.Builder()
                .setContext(this)
                .setTitle("设置邮箱")
                .setNegativeButton("取消")
                .setPositiveButton("确定")
                .setOnDialogClickListener(new InputDialog.onDialogClickListener() {
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
            int startProgress = (int) SaveKeyValues.getValue("progress", 0);
            int endProgress = (int) SaveKeyValues.getValue("progress", 0);
            if (startProgress <= 1 || endProgress <= 1) {
                finish();
            } else {
                new AlertView("温馨提示", "当前有正在进行中的任务...",
                        null, new String[]{"确定"}, null,
                        this, AlertView.Style.Alert, null).setCancelable(false).show();
            }
            return true;
        }
        return false;
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        amUpdateLiveData.removeObserver(amUpdateObserver);
        pmUpdateLiveData.removeObserver(pmUpdateObserver);
    }
}