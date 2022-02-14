package com.pengxh.autodingding.ui.fragment;

import android.content.Context;
import android.content.Intent;
import android.os.CountDownTimer;
import android.os.Handler;
import android.os.Message;
import android.util.Log;
import android.view.View;

import androidx.annotation.NonNull;

import com.jzxiang.pickerview.TimePickerDialog;
import com.jzxiang.pickerview.data.Type;
import com.pengxh.app.multilib.utils.ColorUtil;
import com.pengxh.app.multilib.widget.EasyToast;
import com.pengxh.autodingding.AndroidxBaseFragment;
import com.pengxh.autodingding.R;
import com.pengxh.autodingding.databinding.FragmentDayBinding;
import com.pengxh.autodingding.ui.WelcomeActivity;
import com.pengxh.autodingding.utils.Constant;
import com.pengxh.autodingding.utils.SendMailUtil;
import com.pengxh.autodingding.utils.TimeOrDateUtil;
import com.pengxh.autodingding.utils.Utils;

import java.lang.ref.WeakReference;
import java.util.Timer;
import java.util.TimerTask;

public class AutoDingDingFragment extends AndroidxBaseFragment<FragmentDayBinding> implements View.OnClickListener {

    private static final String TAG = "AutoDingDingFragment";
    private static WeakReferenceHandler weakReferenceHandler;
    private CountDownTimer amCountDownTimer, pmCountDownTimer;
    private Timer timer;

    @Override
    protected void setupTopBarLayout() {

    }

    @Override
    protected void initData() {
        weakReferenceHandler = new WeakReferenceHandler(this);

        timer = new Timer();
        timer.schedule(new TimerTask() {
            @Override
            public void run() {
                String systemTime = TimeOrDateUtil.timestampToTime(System.currentTimeMillis());
                viewBinding.currentTime.post(() -> viewBinding.currentTime.setText(systemTime));
            }
        }, 0, 1000);
        viewBinding.startLayoutView.setOnClickListener(this);
        viewBinding.endLayoutView.setOnClickListener(this);
        viewBinding.endAmDuty.setOnClickListener(this);
        viewBinding.endPmDuty.setOnClickListener(this);
    }

    @Override
    protected void initEvent() {

    }

    public static void sendMessage(String value) {
        Message message = weakReferenceHandler.obtainMessage();
        //回到主页
        message.obj = value;
        message.what = 2022021401;
        weakReferenceHandler.sendMessage(message);
    }

    private static class WeakReferenceHandler extends Handler {

        private final WeakReference<AutoDingDingFragment> reference;

        private WeakReferenceHandler(AutoDingDingFragment fragment) {
            reference = new WeakReference<>(fragment);
        }

        @Override
        public void handleMessage(@NonNull Message msg) {
            super.handleMessage(msg);
            AutoDingDingFragment fragment = reference.get();
            Context context = fragment.getContext();
            assert context != null;
            if (msg.what == 2022021401) {
                Intent intent = new Intent(context, WelcomeActivity.class);
                intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                context.startActivity(intent);
                String emailAddress = Utils.readEmailAddress();
                if (emailAddress.equals("")) {
                    Log.d(TAG, "邮箱地址为空");
                } else {
                    String message = (String) msg.obj;
                    if (message == null || message.equals("")) {
                        Log.d(TAG, "邮件内容为空");
                    } else {
                        //发送打卡成功的邮件
                        SendMailUtil.send(emailAddress, message);
                    }
                }
            }
        }
    }

    @Override
    public void onClick(View v) {
        int id = v.getId();
        if (id == R.id.startLayoutView) {
            //设置上班时间
            new TimePickerDialog.Builder().setThemeColor(ColorUtil.getRandomColor())
                    .setWheelItemTextSize(15)
                    .setCyclic(false)
                    .setMinMillseconds(System.currentTimeMillis())
                    .setMaxMillseconds(System.currentTimeMillis() + Constant.ONE_WEEK)
                    .setType(Type.ALL)
                    .setCallBack((timePickerView, millSeconds) -> {
                        viewBinding.amTime.setText(TimeOrDateUtil.timestampToDate(millSeconds));
                        //计算时间差
                        onDuty(millSeconds);
                    }).build().show(getChildFragmentManager(), "year_month_day_hour_minute");
        } else if (id == R.id.endLayoutView) {
            //设置下班时间
            new TimePickerDialog.Builder().setThemeColor(ColorUtil.getRandomColor())
                    .setWheelItemTextSize(15)
                    .setCyclic(false)
                    .setMinMillseconds(System.currentTimeMillis())
                    .setMaxMillseconds(System.currentTimeMillis() + Constant.ONE_WEEK)
                    .setType(Type.ALL)
                    .setCallBack((timePickerView, millSeconds) -> {
                        viewBinding.pmTime.setText(TimeOrDateUtil.timestampToDate(millSeconds));
                        //计算时间差
                        offDuty(millSeconds);
                    }).build().show(getChildFragmentManager(), "year_month_day_hour_minute");
        } else if (id == R.id.endAmDuty) {
            if (amCountDownTimer != null) {
                amCountDownTimer.cancel();
                viewBinding.startTimeView.setText("--");
            }
        } else if (id == R.id.endPmDuty) {
            if (pmCountDownTimer != null) {
                pmCountDownTimer.cancel();
                viewBinding.endTimeView.setText("--");
            }
        }
    }

    private void onDuty(long millSeconds) {
        long deltaTime = TimeOrDateUtil.deltaTime(millSeconds / 1000);
        if (deltaTime == 0) {
            return;
        }
        //显示倒计时
        String text = viewBinding.startTimeView.getText().toString();

        if (text.equals("--")) {
            amCountDownTimer = new CountDownTimer(deltaTime * 1000, 1000) {
                @Override
                public void onTick(long l) {
                    viewBinding.startTimeView.setText(String.valueOf((int) (l / 1000)));
                }

                @Override
                public void onFinish() {
                    viewBinding.startTimeView.setText("--");
                    Utils.openDingDing(Constant.DINGDING);
                }
            };
            amCountDownTimer.start();
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
        String text = viewBinding.endTimeView.getText().toString();
        if (text.equals("--")) {
            pmCountDownTimer = new CountDownTimer(deltaTime * 1000, 1000) {
                @Override
                public void onTick(long l) {
                    viewBinding.endTimeView.setText(String.valueOf((int) (l / 1000)));
                }

                @Override
                public void onFinish() {
                    viewBinding.endTimeView.setText("--");
                    Utils.openDingDing(Constant.DINGDING);
                }
            };
            pmCountDownTimer.start();
        } else {
            EasyToast.showToast("已有任务在进行中", EasyToast.WARING);
        }
    }

    @Override
    public void onDestroyView() {
        if (timer != null) {
            timer.cancel();
        }
        super.onDestroyView();
    }
}