package com.pengxh.autodingding.ui;

import android.annotation.SuppressLint;
import android.app.Dialog;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.util.Log;
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
import com.pengxh.app.multilib.base.DoubleClickExitActivity;
import com.pengxh.app.multilib.utils.BroadcastManager;
import com.pengxh.app.multilib.utils.ColorUtil;
import com.pengxh.app.multilib.utils.SaveKeyValues;
import com.pengxh.app.multilib.widget.EasyToast;
import com.pengxh.app.multilib.widget.dialog.InputDialog;
import com.pengxh.autodingding.R;
import com.pengxh.autodingding.service.AutoDingdingService;
import com.pengxh.autodingding.utils.BroadcastAction;
import com.pengxh.autodingding.utils.Utils;
import com.pengxh.autodingding.widgets.EasyPopupWindow;

import java.util.Arrays;
import java.util.List;

import butterknife.BindView;
import butterknife.OnClick;

public class MainActivity extends DoubleClickExitActivity implements View.OnClickListener {

    private static final String TAG = "MainActivity";
    private static final List<String> items = Arrays.asList("邮箱设置");

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
        String qqEmail = (String) SaveKeyValues.getValue("email", "");
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
            textViewTitle.setText("自动打卡邮箱：" + qqEmail);
        }
    }

    @Override
    public void initEvent() {
        if (Utils.isAppAvailable(BroadcastAction.DINGDING)) {
            startService(new Intent(this, AutoDingdingService.class));

            broadcastManager.addAction(BroadcastAction.ACTIONS, new BroadcastReceiver() {
                @SuppressLint("SetTextI18n")
                @Override
                public void onReceive(Context context, Intent intent) {
                    //更新UI
                    String action = intent.getAction();
                    if (action != null) {
                        if (action.equals(BroadcastAction.ACTIONS[2])) {
                            String data = intent.getStringExtra("data");
                            startTimeBtn.setText(data + "s");
                            startProgressBar.setProgress(Integer.parseInt(data));
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
                            if (endProgressBar.getProgress() == 0) {
                                //重置所有状态
                                pmTime.setText("下班打卡时间未设置");
                                endTimeBtn.setText("下班设置");
                                endProgressBar.setVisibility(View.GONE);
                                SaveKeyValues.clearAll();
                            }
                        }
                    } else {
                        Log.e(TAG, "onReceive: ", new Throwable());
                    }
                }
            });
        } else {
            alertView = new AlertView("温馨提示", "手机没有安装钉钉软件，无法自动打卡",
                    null, new String[]{"确定"}, null, this, AlertView.Style.Alert,
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
                        .setType(Type.MONTH_DAY_HOUR_MIN)
                        .setCallBack(new OnDateSetListener() {
                            @Override
                            public void onDateSet(TimePickerDialog timePickerView, long millSeconds) {
                                String amKaoQin = Utils.timestampToDate(millSeconds);
                                Log.d(TAG, "onDateSet: " + amKaoQin);
                                SaveKeyValues.putValue("amKaoQin", amKaoQin);
                                amTime.setText("将在" + amKaoQin + "自动打卡");
                                //计算时间差
                                long deltaTime = Utils.deltaTime(millSeconds / 1000);
                                if (deltaTime == 0) {
                                    return;
                                }
                                startProgressBar.setMax((int) deltaTime);
                                startProgressBar.setVisibility(View.VISIBLE);
                                broadcastManager.sendBroadcast(BroadcastAction.ACTIONS[0], String.valueOf(deltaTime));
                            }
                        }).build().show(getSupportFragmentManager(), "month_day_hour_minute");
                break;
            case R.id.endTimeBtn:
                //设置下班时间
                new TimePickerDialog.Builder().setThemeColor(ColorUtil.getRandomColor())
                        .setType(Type.MONTH_DAY_HOUR_MIN)
                        .setCallBack(new OnDateSetListener() {
                            @Override
                            public void onDateSet(TimePickerDialog timePickerView, long millSeconds) {
                                String pmKaoQin = Utils.timestampToDate(millSeconds);
                                Log.d(TAG, "onDateSet: " + pmKaoQin);
                                SaveKeyValues.putValue("pmKaoQin", pmKaoQin);
                                pmTime.setText("将在" + pmKaoQin + "自动打卡");
                                //计算时间差
                                long deltaTime = Utils.deltaTime(millSeconds / 1000);
                                if (deltaTime == 0) {
                                    return;
                                }
                                endProgressBar.setMax((int) deltaTime);
                                endProgressBar.setVisibility(View.VISIBLE);
                                broadcastManager.sendBroadcast(BroadcastAction.ACTIONS[1], String.valueOf(deltaTime));
                            }
                        }).build().show(getSupportFragmentManager(), "month_day_hour_minute");
                break;
            case R.id.imageViewTitleRight:
                //邮箱设置
                EasyPopupWindow easyPopupWindow = new EasyPopupWindow(this, items);
                easyPopupWindow.setPopupWindowClickListener(new EasyPopupWindow.PopupWindowClickListener() {
                    @Override
                    public void popupWindowClick(int position) {
                        //TODO 截屏需要手机已Root
                        EasyToast.showToast("截屏需要手机已Root！", EasyToast.WARING);

//                        new InputDialog.Builder().setContext(MainActivity.this)
//                                .setTitle("设置邮箱")
//                                .setNegativeButton("取消")
//                                .setPositiveButton("确定")
//                                .setOnDialogClickListener(new InputDialog.onDialogClickListener() {
//                                    @Override
//                                    public void onConfirmClick(Dialog dialog, String input) {
//                                        if (!input.isEmpty()) {
//                                            if (input.endsWith("@qq.com")) {
//                                                SaveKeyValues.putValue("email", input);
//                                                textViewTitle.setText("自动打卡邮箱：" + input);
//                                            } else {
//                                                EasyToast.showToast("邮箱设置失败，暂时只支持QQ邮箱！", EasyToast.WARING);
//                                            }
//                                            dialog.dismiss();
//                                        } else {
//                                            EasyToast.showToast("什么都还没输入呢！", EasyToast.ERROR);
//                                        }
//                                    }
//
//                                    @Override
//                                    public void onCancelClick(Dialog dialog) {
//                                        dialog.dismiss();
//                                    }
//                                }).build().show();
                    }
                });
                easyPopupWindow.showAsDropDown(titleLayout, titleLayout.getWidth(), 0);
                break;
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        broadcastManager.destroy(BroadcastAction.ACTIONS);
    }
}