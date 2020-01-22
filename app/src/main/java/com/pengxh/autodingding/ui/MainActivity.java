package com.pengxh.autodingding.ui;

import android.annotation.SuppressLint;
import android.content.Intent;
import android.os.CountDownTimer;
import android.os.Handler;
import android.os.Message;
import android.util.Log;
import android.view.View;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.aihook.alertview.library.AlertView;
import com.gyf.immersionbar.ImmersionBar;
import com.jzxiang.pickerview.TimePickerDialog;
import com.jzxiang.pickerview.data.Type;
import com.pengxh.app.multilib.base.BaseNormalActivity;
import com.pengxh.app.multilib.utils.ColorUtil;
import com.pengxh.app.multilib.widget.EasyToast;
import com.pengxh.autodingding.R;
import com.pengxh.autodingding.utils.Constant;
import com.pengxh.autodingding.utils.SendMailUtil;
import com.pengxh.autodingding.utils.TimeOrDateUtil;
import com.pengxh.autodingding.utils.Utils;

import butterknife.BindView;
import butterknife.OnClick;

public class MainActivity extends BaseNormalActivity implements View.OnClickListener {

    private static final String TAG = "MainActivity";

    @BindView(R.id.titleLayout)
    RelativeLayout titleLayout;
    @BindView(R.id.textViewTitle)
    TextView textViewTitle;
    @BindView(R.id.imageViewTitleRight)
    ImageView imageViewTitleRight;
    @BindView(R.id.startTimeView)
    TextView startTimeView;
    @BindView(R.id.endTimeView)
    TextView endTimeView;
    @BindView(R.id.amTime)
    TextView amTime;
    @BindView(R.id.pmTime)
    TextView pmTime;

    @Override
    public void initView() {
        setContentView(R.layout.activity_main);
        ImmersionBar.with(this).fitsSystemWindows(true).statusBarColor(R.color.colorAppThemeLight).init();
        imageViewTitleRight.setVisibility(View.GONE);
    }

    @Override
    public void initData() {
        String emailAddress = Utils.readEmailAddress();
        if (!emailAddress.equals("")) {
            textViewTitle.setText("打卡通知邮箱：" + emailAddress);
        }
    }

    @Override
    public void initEvent() {

    }

    @OnClick({R.id.startLayoutView, R.id.endLayoutView, R.id.introduceTxt})
    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.startLayoutView:
                //设置上班时间
                new TimePickerDialog.Builder().setThemeColor(ColorUtil.getRandomColor())
                        .setWheelItemTextSize(15)
                        .setCyclic(false)
                        .setMinMillseconds(System.currentTimeMillis())
                        .setMaxMillseconds(System.currentTimeMillis() + Constant.ONE_MONTH)
                        .setType(Type.ALL)
                        .setCallBack((timePickerView, millSeconds) -> {
                            //计算时间差
                            long deltaTime = TimeOrDateUtil.deltaTime(millSeconds / 1000);
                            Log.d(TAG, "amKaoQin: " + deltaTime);
                            if (deltaTime == 0) {
                                Log.w(TAG, "", new Throwable());
                                return;
                            }
                            amTime.setText("打卡时间：" + TimeOrDateUtil.timestampToDate(millSeconds));
                            //显示倒计时
                            String text = startTimeView.getText().toString();
                            if (text.equals("--")) {
                                new CountDownTimer(deltaTime * 1000, 1000) {
                                    @Override
                                    public void onTick(long l) {
                                        int tickTime = (int) (l / 1000);
                                        //更新UI
                                        startTimeView.setText(tickTime + "s");
                                    }

                                    @Override
                                    public void onFinish() {
                                        Utils.openDingding(Constant.DINGDING);
                                        handler.sendEmptyMessageDelayed(1, 10 * 1000);
                                    }
                                }.start();
                            } else {
                                EasyToast.showToast("已有任务在进行中", EasyToast.WARING);
                            }
                        }).build().show(getSupportFragmentManager(), "year_month_day_hour_minute");
                break;
            case R.id.endLayoutView:
                //设置下班时间
                new TimePickerDialog.Builder().setThemeColor(ColorUtil.getRandomColor())
                        .setWheelItemTextSize(15)
                        .setCyclic(false)
                        .setMinMillseconds(System.currentTimeMillis())
                        .setMaxMillseconds(System.currentTimeMillis() + Constant.ONE_MONTH)
                        .setType(Type.ALL)
                        .setCallBack((timePickerView, millSeconds) -> {
                            //计算时间差
                            long deltaTime = TimeOrDateUtil.deltaTime(millSeconds / 1000);
                            Log.d(TAG, "pmKaoQin: " + deltaTime);
                            if (deltaTime == 0) {
                                Log.w(TAG, "", new Throwable());
                                return;
                            }
                            pmTime.setText("打卡时间：" + TimeOrDateUtil.timestampToDate(millSeconds));
                            //显示倒计时
                            String text = startTimeView.getText().toString();
                            if (text.equals("--")) {
                                new CountDownTimer(deltaTime * 1000, 1000) {
                                    @Override
                                    public void onTick(long l) {
                                        int tickTime = (int) (l / 1000);
                                        //更新UI
                                        endTimeView.setText(tickTime + "s");
                                    }

                                    @Override
                                    public void onFinish() {
                                        Utils.openDingding(Constant.DINGDING);
                                        handler.sendEmptyMessageDelayed(1, 10 * 1000);
                                    }
                                }.start();
                            } else {
                                EasyToast.showToast("已有任务在进行中", EasyToast.WARING);
                            }
                        }).build().show(getSupportFragmentManager(), "year_month_day_hour_minute");
                break;
            case R.id.introduceTxt:
                new AlertView("功能介绍", getResources().getString(R.string.about),
                        null, new String[]{"确定"}, null,
                        this, AlertView.Style.Alert,
                        null).setCancelable(false).show();
                break;
        }
    }

    @SuppressLint("HandlerLeak")
    private Handler handler = new Handler() {
        @Override
        public void handleMessage(Message msg) {
            if (msg.what == 1) {
                Intent intent = new Intent(getApplicationContext(), MainActivity.class);
                intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                startActivity(intent);

                String emailAddress = Utils.readEmailAddress();
                //发送打卡成功的邮件
                Log.d(TAG, "发送打卡成功的邮件: " + emailAddress);
                if (emailAddress.equals("")) {
                    return;
                }
                SendMailUtil.send(emailAddress);
            }
        }
    };
}