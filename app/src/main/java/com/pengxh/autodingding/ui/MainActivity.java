package com.pengxh.autodingding.ui;

import android.annotation.SuppressLint;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.util.Log;
import android.view.KeyEvent;
import android.view.View;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.aihook.alertview.library.AlertView;
import com.aihook.alertview.library.OnItemClickListener;
import com.jzxiang.pickerview.TimePickerDialog;
import com.jzxiang.pickerview.data.Type;
import com.jzxiang.pickerview.listener.OnDateSetListener;
import com.pengxh.app.multilib.base.BaseNormalActivity;
import com.pengxh.app.multilib.utils.BroadcastManager;
import com.pengxh.app.multilib.utils.ColorUtil;
import com.pengxh.app.multilib.utils.SaveKeyValues;
import com.pengxh.app.multilib.widget.EasyToast;
import com.pengxh.autodingding.R;
import com.pengxh.autodingding.service.AutoDingdingService;
import com.pengxh.autodingding.utils.BroadcastAction;
import com.pengxh.autodingding.utils.Utils;
import com.pengxh.autodingding.widgets.EasyPopupWindow;

import java.util.Collections;
import java.util.List;

import butterknife.BindView;
import butterknife.OnClick;

public class MainActivity extends BaseNormalActivity implements View.OnClickListener {

    private static final String TAG = "MainActivity";
    private static final List<String> items = Collections.singletonList("功能介绍");

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
    private BroadcastManager broadcastManager;

    @Override
    public void initView() {
        getWindow().addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);//手机常亮
        setContentView(R.layout.activity_main);
    }

    @Override
    public void initData() {
        broadcastManager = BroadcastManager.getInstance(this);
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
    }

    @Override
    public void initEvent() {
        if (Utils.isAppAvailable(BroadcastAction.DINGDING)) {
            startService(new Intent(this, AutoDingdingService.class));

            //监听AutoDingdingService返回来的消息，更新UI
            broadcastManager.addAction(BroadcastAction.ACTIONS, new BroadcastReceiver() {
                @SuppressLint("SetTextI18n")
                @Override
                public void onReceive(Context context, Intent intent) {
                    String action = intent.getAction();
                    if (action != null) {
                        if (action.equals(BroadcastAction.ACTIONS[2])) {
                            String data = intent.getStringExtra("data");
                            startTimeBtn.setText(data + "s");
                            startProgressBar.setProgress(Integer.parseInt(data));
                            SaveKeyValues.putValue("progress", Integer.parseInt(data));
                            if (startProgressBar.getProgress() == 0) {
                                //重置所有状态
                                amTime.setText("上班打卡时间未设置");
                                startTimeBtn.setText("上班设置");
                                startProgressBar.setVisibility(View.GONE);
                                SaveKeyValues.clearAll();
                            }
                        } else if (action.equals(BroadcastAction.ACTIONS[3])) {
                            String data = intent.getStringExtra("data");
                            endTimeBtn.setText(data + "s");
                            endProgressBar.setProgress(Integer.parseInt(data));
                            SaveKeyValues.putValue("progress", Integer.parseInt(data));
                            if (endProgressBar.getProgress() == 0) {
                                //重置所有状态
                                pmTime.setText("下班打卡时间未设置");
                                endTimeBtn.setText("下班设置");
                                endProgressBar.setVisibility(View.GONE);
                                SaveKeyValues.clearAll();
                            }
                        }
                    } else {
                        Log.e(TAG, "", new Throwable());
                    }
                }
            });
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
                                long deltaTime = Utils.deltaTime(millSeconds / 1000);
                                Log.d(TAG, "amKaoQin: " + deltaTime);
                                if (deltaTime == 0) {
                                    Log.w(TAG, "", new Throwable());
                                } else {
                                    String amKaoQin = Utils.timestampToDate(millSeconds);
                                    amTime.setText("将在" + amKaoQin + "自动打卡");
                                    SaveKeyValues.putValue("amKaoQin", amKaoQin);
                                    int currentPro = startProgressBar.getProgress();
                                    if (currentPro != 0) {
                                        EasyToast.showToast("当前已有定时任务，无法重复设置", EasyToast.ERROR);
                                    } else {
                                        startProgressBar.setMax((int) deltaTime);
                                        startProgressBar.setVisibility(View.VISIBLE);
                                        broadcastManager.sendBroadcast(BroadcastAction.ACTIONS[0], String.valueOf(deltaTime));
                                    }
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
                                long deltaTime = Utils.deltaTime(millSeconds / 1000);
                                Log.d(TAG, "pmKaoQin: " + deltaTime);
                                if (deltaTime == 0) {
                                    Log.w(TAG, "", new Throwable());
                                } else {
                                    String pmKaoQin = Utils.timestampToDate(millSeconds);
                                    pmTime.setText("将在" + pmKaoQin + "自动打卡");
                                    SaveKeyValues.putValue("pmKaoQin", pmKaoQin);
                                    int currentPro = endProgressBar.getProgress();
                                    if (currentPro != 0) {
                                        EasyToast.showToast("当前已有定时任务，无法重复设置", EasyToast.ERROR);
                                    } else {
                                        endProgressBar.setMax((int) deltaTime);
                                        endProgressBar.setVisibility(View.VISIBLE);
                                        broadcastManager.sendBroadcast(BroadcastAction.ACTIONS[1], String.valueOf(deltaTime));
                                    }
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
                        }
                    }
                });
                easyPopupWindow.showAsDropDown(titleLayout, titleLayout.getWidth(), 0);
                break;
        }
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
        broadcastManager.destroy(BroadcastAction.ACTIONS);
    }
}