package com.pengxh.autodingding.ui.fragment;

import android.annotation.SuppressLint;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.os.CountDownTimer;
import android.os.Handler;
import android.os.Message;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.jzxiang.pickerview.TimePickerDialog;
import com.jzxiang.pickerview.data.Type;
import com.pengxh.app.multilib.utils.BroadcastManager;
import com.pengxh.app.multilib.utils.ColorUtil;
import com.pengxh.app.multilib.widget.EasyToast;
import com.pengxh.autodingding.R;
import com.pengxh.autodingding.ui.MainActivity;
import com.pengxh.autodingding.utils.Constant;
import com.pengxh.autodingding.utils.SQLiteUtil;
import com.pengxh.autodingding.utils.SendMailUtil;
import com.pengxh.autodingding.utils.TimeOrDateUtil;
import com.pengxh.autodingding.utils.Utils;

import java.util.Timer;
import java.util.TimerTask;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;
import butterknife.Unbinder;

public class OneDayFragment extends Fragment implements View.OnClickListener {

    private static final String TAG = "OneDayFragment";

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

    Unbinder unbinder;
    private BroadcastManager broadcastManager;
    private String emailMessage = "";

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View mView = LayoutInflater.from(this.getContext()).inflate(R.layout.fragment_day, null);
        unbinder = ButterKnife.bind(this, mView);
        initEvent();
        return mView;
    }

    private void initEvent() {
        new Timer().schedule(new TimerTask() {
            @Override
            public void run() {
                String systemTime = TimeOrDateUtil.timestampToTime(System.currentTimeMillis());
                currentTime.setText(systemTime);
            }
        }, 0, 1000);

        broadcastManager = BroadcastManager.getInstance(getContext());
        broadcastManager.addAction(Constant.DINGDING_ACTION, new BroadcastReceiver() {
            @Override
            public void onReceive(Context context, Intent intent) {
                String action = intent.getAction();
                if (action == null) {
                    return;
                }
                if (action.equals(Constant.DINGDING_ACTION)) {
                    emailMessage = intent.getStringExtra("data");
                    Log.d(TAG, "onReceive: " + emailMessage);
                    //保存打卡记录
                    //工作通知:CSS-考勤打卡:23:31 上班打卡成功,进入钉钉查看详情
                    //工作通知:CSS-考勤打卡:23:32 下班打卡成功,进入钉钉查看详情
                    String[] split = emailMessage.split("-");
                    String title = split[0].replace("工作通知", "打卡部门");//工作通知:政府事业本部2
                    //[1条]考勤打卡:19:49 下班打卡成功
                    String[] strings = split[1].split(",")[0].split(" ");
                    //[3条]考勤打卡:20:06
                    String s = strings[0];
                    int off = s.indexOf("]");
                    String time = s.substring(off + 1);
                    String message = strings[1];

                    SQLiteUtil.getInstance().saveHistory(Utils.uuid(),
                            title,
                            TimeOrDateUtil.rTimestampToDate(System.currentTimeMillis()),
                            time,
                            message);
                }
            }
        });
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
                            //计算时间差
                            long deltaTime = TimeOrDateUtil.deltaTime(millSeconds / 1000);
                            Log.d(TAG, "amKaoQin: " + deltaTime);
                            if (deltaTime == 0) {
                                Log.w(TAG, "", new Throwable());
                                return;
                            }
                            amTime.setText("上班打卡：" + TimeOrDateUtil.timestampToDate(millSeconds));
                            //显示倒计时
                            String text = startTimeView.getText().toString();
                            if (text.equals("--")) {
                                new CountDownTimer(deltaTime * 1000, 1000) {
                                    @Override
                                    public void onTick(long l) {
                                        int tickTime = (int) (l / 1000);
                                        //更新UI
                                        startTimeView.setText(tickTime + "s");
                                        if (tickTime == 0) {
                                            startTimeView.setText("--");
                                        }
                                    }

                                    @Override
                                    public void onFinish() {
                                        Utils.openDingDing(Constant.DINGDING);
                                        handler.sendEmptyMessageDelayed(1, 10 * 1000);
                                    }
                                }.start();
                            } else {
                                EasyToast.showToast("已有任务在进行中", EasyToast.WARING);
                            }
                        }).build().show(getActivity().getSupportFragmentManager(), "year_month_day_hour_minute");
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
                            pmTime.setText("下班打卡：" + TimeOrDateUtil.timestampToDate(millSeconds));
                            //显示倒计时
                            String text = endTimeView.getText().toString();
                            if (text.equals("--")) {
                                new CountDownTimer(deltaTime * 1000, 1000) {
                                    @Override
                                    public void onTick(long l) {
                                        int tickTime = (int) (l / 1000);
                                        //更新UI
                                        endTimeView.setText(tickTime + "s");
                                        if (tickTime == 0) {
                                            endTimeView.setText("--");
                                        }
                                    }

                                    @Override
                                    public void onFinish() {
                                        Utils.openDingDing(Constant.DINGDING);
                                        handler.sendEmptyMessageDelayed(1, 10 * 1000);
                                    }
                                }.start();
                            } else {
                                EasyToast.showToast("已有任务在进行中", EasyToast.WARING);
                            }
                        }).build().show(getActivity().getSupportFragmentManager(), "year_month_day_hour_minute");
                break;
            default:
                break;
        }
    }

    @SuppressLint("HandlerLeak")
    private Handler handler = new Handler() {
        @Override
        public void handleMessage(Message msg) {
            if (msg.what == 1) {
                Intent intent = new Intent(getActivity(), MainActivity.class);
                intent.putExtra("position", 0);
                intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                startActivity(intent);

                String emailAddress = Utils.readEmailAddress();
                //发送打卡成功的邮件
                Log.d(TAG, "发送打卡成功的邮件: " + emailAddress);
                if (emailAddress.equals("")) {
                    return;
                }
                SendMailUtil.send(emailAddress, emailMessage);
            }
        }
    };

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        broadcastManager.destroy(Constant.DINGDING_ACTION);
    }
}