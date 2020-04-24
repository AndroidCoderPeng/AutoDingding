package com.pengxh.autodingding.ui.fragment;

import android.annotation.SuppressLint;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.os.Build;
import android.os.Bundle;
import android.os.CountDownTimer;
import android.os.Handler;
import android.os.Message;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.jzxiang.pickerview.TimePickerDialog;
import com.jzxiang.pickerview.data.Type;
import com.pengxh.app.multilib.utils.BroadcastManager;
import com.pengxh.app.multilib.utils.ColorUtil;
import com.pengxh.app.multilib.utils.SaveKeyValues;
import com.pengxh.app.multilib.widget.EasyToast;
import com.pengxh.autodingding.R;
import com.pengxh.autodingding.ui.MainActivity;
import com.pengxh.autodingding.utils.Constant;
import com.pengxh.autodingding.utils.LogToFile;
import com.pengxh.autodingding.utils.SQLiteUtil;
import com.pengxh.autodingding.utils.SendMailUtil;
import com.pengxh.autodingding.utils.TimeOrDateUtil;
import com.pengxh.autodingding.utils.Utils;

import java.util.Objects;
import java.util.Timer;
import java.util.TimerTask;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;
import butterknife.Unbinder;

@SuppressLint("SetTextI18n")
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
    private FragmentManager fragmentManager;
    private BroadcastManager broadcastManager;
    private Context context;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View mView = LayoutInflater.from(this.getContext()).inflate(R.layout.fragment_day, null);
        unbinder = ButterKnife.bind(this, mView);
        context = getContext();
        initEvent();
        return mView;
    }

    private void initEvent() {
        fragmentManager = Objects.requireNonNull(getActivity()).getSupportFragmentManager();
        new Timer().schedule(new TimerTask() {
            @Override
            public void run() {
                String systemTime = TimeOrDateUtil.timestampToTime(System.currentTimeMillis());
                currentTime.post(() -> currentTime.setText(systemTime));
            }
        }, 0, 1000);

        broadcastManager = BroadcastManager.getInstance(context);
        broadcastManager.addAction(Constant.DINGDING_ACTION, new BroadcastReceiver() {
            @Override
            public void onReceive(Context context, Intent intent) {
                String action = intent.getAction();
                if (action != null && action.equals(Constant.DINGDING_ACTION)) {
                    String message = intent.getStringExtra("data");
                    Log.d(TAG, "接收到广播, 通知内容: " + message);
                    LogToFile.d(TAG, "接收到广播, 通知内容: " + message);
                    //TODO 保存打卡记录
                    //考勤打卡:11:14 下班打卡成功,进入钉钉查看详情
                    //[4条]考勤打卡:11:11 下班打卡成功,进入钉钉查看详情
                    String result;
                    if (message.startsWith("[")) {
                        result = message.substring(message.indexOf("]") + 1, message.indexOf(","));
                    } else {
                        result = message.substring(0, message.indexOf(","));
                    }
                    SQLiteUtil.getInstance().saveHistory(Utils.uuid(),
                            TimeOrDateUtil.rTimestampToDate(System.currentTimeMillis()),
                            result);
                    SaveKeyValues.putValue("emailMessage", message);
                    BroadcastManager.getInstance(context).sendBroadcast(Constant.ACTION_UPDATE, "update");
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
            Log.w(TAG, "", new Throwable());
            return;
        }
        //显示倒计时
        String text = startTimeView.getText().toString();

        if (text.equals("--")) {
            new CountDownTimer(deltaTime * 1000, 1000) {
                @Override
                public void onTick(long l) {
                    int tickTime = (int) (l / 1000);
                    //更新UI
                    startTimeView.setText(tickTime + "s");
                    /**
                     * 7.0系统只能倒计时到1
                     *
                     * 9.1系统可以倒计时到0
                     * */
                    if (Build.VERSION.SDK_INT <= Build.VERSION_CODES.N) {
                        if (tickTime == 1) {
                            startTimeView.setText("--");
                        }
                    } else {
                        if (tickTime == 0) {
                            startTimeView.setText("--");
                        }
                    }
                }

                @Override
                public void onFinish() {
                    Utils.openDingDing(Constant.DINGDING);
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
            Log.w(TAG, "", new Throwable());
            return;
        }
        //显示倒计时
        String text = endTimeView.getText().toString();
        if (text.equals("--")) {
            new CountDownTimer(deltaTime * 1000, 1000) {
                @Override
                public void onTick(long l) {
                    int tickTime = (int) (l / 1000);
                    //更新UI
                    endTimeView.setText(tickTime + "s");
                    if (Build.VERSION.SDK_INT <= Build.VERSION_CODES.N) {
                        if (tickTime == 1) {
                            endTimeView.setText("--");
                        }
                    } else {
                        if (tickTime == 0) {
                            endTimeView.setText("--");
                        }
                    }
                }

                @Override
                public void onFinish() {
                    Utils.openDingDing(Constant.DINGDING);
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
                Intent intent = new Intent(context, MainActivity.class);
                intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                context.startActivity(intent);

                String emailAddress = Utils.readEmailAddress();
                //发送打卡成功的邮件
                Log.d(TAG, "发送打卡成功的邮件: " + emailAddress);
                if (emailAddress.equals("")) {
                    return;
                }
                SendMailUtil.send(emailAddress, (String) SaveKeyValues.getValue("emailMessage", ""));
            }
        }
    };

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        broadcastManager.destroy(Constant.DINGDING_ACTION);
    }
}