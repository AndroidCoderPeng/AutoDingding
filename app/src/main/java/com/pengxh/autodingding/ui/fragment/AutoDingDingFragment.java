package com.pengxh.autodingding.ui.fragment;

import android.annotation.SuppressLint;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.graphics.Color;
import android.os.CountDownTimer;
import android.os.Handler;
import android.os.Message;
import android.util.Log;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.fragment.app.FragmentActivity;
import androidx.fragment.app.FragmentManager;

import com.gyf.immersionbar.ImmersionBar;
import com.jzxiang.pickerview.TimePickerDialog;
import com.jzxiang.pickerview.data.Type;
import com.pengxh.app.multilib.utils.BroadcastManager;
import com.pengxh.app.multilib.utils.ColorUtil;
import com.pengxh.app.multilib.utils.LogToFile;
import com.pengxh.app.multilib.widget.EasyToast;
import com.pengxh.autodingding.BaseFragment;
import com.pengxh.autodingding.R;
import com.pengxh.autodingding.ui.WelcomeActivity;
import com.pengxh.autodingding.utils.Constant;
import com.pengxh.autodingding.utils.SQLiteUtil;
import com.pengxh.autodingding.utils.SendMailUtil;
import com.pengxh.autodingding.utils.StatusBarColorUtil;
import com.pengxh.autodingding.utils.TimeOrDateUtil;
import com.pengxh.autodingding.utils.Utils;

import java.util.Timer;
import java.util.TimerTask;

import butterknife.BindView;
import butterknife.OnClick;

@SuppressLint("SetTextI18n")
public class AutoDingDingFragment extends BaseFragment implements View.OnClickListener {

    private static final String TAG = "AutoDingDingFragment";

    @BindView(R.id.mTitleLeftView)
    ImageView mTitleLeftView;
    @BindView(R.id.mTitleView)
    TextView mTitleView;
    @BindView(R.id.mTitleRightView)
    ImageView mTitleRightView;
    @BindView(R.id.currentTime)
    TextView currentTime;
    @BindView(R.id.startTimeView)
    TextView startTimeView;
    @BindView(R.id.endTimeView)
    TextView endTimeView;
    @BindView(R.id.amTime)
    TextView amTime;
    @BindView(R.id.pmTime)
    TextView pmTime;

    private FragmentManager fragmentManager;
    private BroadcastManager broadcastManager;
    private String message;
    private FragmentActivity activity;

    @Override
    protected int initLayoutView() {
        return R.layout.fragment_day;
    }

    @Override
    protected void initData() {
        activity = getActivity();

        mTitleLeftView.setVisibility(View.GONE);
        mTitleView.setText("自动打卡");
        mTitleRightView.setVisibility(View.GONE);

        new Timer().schedule(new TimerTask() {
            @Override
            public void run() {
                String systemTime = TimeOrDateUtil.timestampToTime(System.currentTimeMillis());
                currentTime.post(() -> currentTime.setText(systemTime));
            }
        }, 0, 1000);

        fragmentManager = activity.getSupportFragmentManager();
        broadcastManager = BroadcastManager.getInstance(activity);
    }

    @Override
    protected void initEvent() {
        broadcastManager.addAction(Constant.DINGDING_ACTION, new BroadcastReceiver() {
            @Override
            public void onReceive(Context context, Intent intent) {
                String action = intent.getAction();
                if (action != null && action.equals(Constant.DINGDING_ACTION)) {
                    message = intent.getStringExtra("data");
                    LogToFile.d(TAG, "接收到广播, 通知内容: " + message);
                    //TODO 保存打卡记录
                    SQLiteUtil.getInstance().saveHistory(Utils.uuid(), TimeOrDateUtil.rTimestampToDate(System.currentTimeMillis()), message);
                    BroadcastManager.getInstance(context).sendBroadcast(Constant.ACTION_UPDATE, "update");
                }
            }
        });
    }

    @Override
    public void initImmersionBar() {
        StatusBarColorUtil.setColor(activity, Color.parseColor("#0094FF"));
        ImmersionBar.with(this).init();
    }

    @OnClick({R.id.startLayoutView, R.id.endLayoutView})
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
                            amTime.setText(TimeOrDateUtil.timestampToDate(millSeconds));
                            //计算时间差
                            onDuty(millSeconds);
                        }).build().show(fragmentManager, "year_month_day_hour_minute");
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
                            pmTime.setText(TimeOrDateUtil.timestampToDate(millSeconds));
                            //计算时间差
                            offDuty(millSeconds);
                        }).build().show(fragmentManager, "year_month_day_hour_minute");
                break;
            default:
                break;
        }
    }

    private void onDuty(long millSeconds) {
        long deltaTime = TimeOrDateUtil.deltaTime(millSeconds / 1000);
        if (deltaTime == 0) {
            LogToFile.w(TAG, String.valueOf(new Throwable()));
            return;
        }
        //显示倒计时
        String text = startTimeView.getText().toString();

        if (text.equals("--")) {
            new CountDownTimer(deltaTime * 1000, 1000) {
                @Override
                public void onTick(long l) {
                    startTimeView.setText((int) (l / 1000) + "s");
                }

                @Override
                public void onFinish() {
                    startTimeView.setText("--");
                    Utils.openDingDing(Constant.DINGDING);

//                    new ReturnActivityService().returnActivity();
                    handler.sendEmptyMessageDelayed(1, 15 * 1000);
                }
            }.start();
        } else {
            EasyToast.showToast("已有任务在进行中", EasyToast.WARING);
        }
    }

    private void offDuty(long millSeconds) {
        long deltaTime = TimeOrDateUtil.deltaTime(millSeconds / 1000);
        if (deltaTime == 0) {
            return;
        }
        //显示倒计时
        String text = endTimeView.getText().toString();
        if (text.equals("--")) {
            new CountDownTimer(deltaTime * 1000, 1000) {
                @Override
                public void onTick(long l) {
                    endTimeView.setText((int) (l / 1000) + "s");
                }

                @Override
                public void onFinish() {
                    endTimeView.setText("--");
                    Utils.openDingDing(Constant.DINGDING);

//                    new ReturnActivityService().returnActivity();
                    handler.sendEmptyMessageDelayed(1, 15 * 1000);
                }
            }.start();
        } else {
            EasyToast.showToast("已有任务在进行中", EasyToast.WARING);
        }
    }

    @SuppressLint("HandlerLeak")
    private Handler handler = new Handler() {
        @Override
        public void handleMessage(Message msg) {
            if (msg.what == 1) {
                Log.d(TAG, "handleMessage: 回主页");
                Intent intent = new Intent(activity, WelcomeActivity.class);
                intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                activity.startActivity(intent);
                String emailAddress = Utils.readEmailAddress();
                if (emailAddress.equals("")) {
                    return;
                }
                Log.d(TAG, "邮箱地址: " + emailAddress + ", 邮件内容： " + message);
                if (message == null) {
                    return;
                }
                //发送打卡成功的邮件
                LogToFile.d(TAG, "邮箱地址: " + emailAddress + ", 邮件内容： " + message);
                SendMailUtil.send(emailAddress, message);
            }
        }
    };

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        broadcastManager.destroy(Constant.DINGDING_ACTION);
    }
}