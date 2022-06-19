package com.pengxh.autodingding.ui.fragment;

import android.os.CountDownTimer;

import com.jzxiang.pickerview.TimePickerDialog;
import com.jzxiang.pickerview.data.Type;
import com.pengxh.androidx.lite.base.AndroidxBaseFragment;
import com.pengxh.androidx.lite.utils.ColorUtil;
import com.pengxh.androidx.lite.utils.TimeOrDateUtil;
import com.pengxh.androidx.lite.widget.EasyToast;
import com.pengxh.autodingding.databinding.FragmentDayBinding;
import com.pengxh.autodingding.utils.Constant;
import com.pengxh.autodingding.utils.Utils;

import java.util.Timer;
import java.util.TimerTask;

public class AutoDingDingFragment extends AndroidxBaseFragment<FragmentDayBinding> {

    private static final String TAG = "AutoDingDingFragment";
    private CountDownTimer amCountDownTimer, pmCountDownTimer;
    private Timer timer;

    @Override
    protected void setupTopBarLayout() {

    }

    @Override
    protected void initData() {
        timer = new Timer();
        timer.schedule(new TimerTask() {
            @Override
            public void run() {
                String systemTime = TimeOrDateUtil.timestampToTime(System.currentTimeMillis());
                viewBinding.currentTime.post(() -> viewBinding.currentTime.setText(systemTime));
            }
        }, 0, 1000);
    }

    @Override
    protected void initEvent() {
        viewBinding.startLayoutView.setOnClickListener(v -> {
            //设置上班时间
            new TimePickerDialog.Builder().setThemeColor(ColorUtil.randomColor())
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
        });
        viewBinding.endLayoutView.setOnClickListener(v -> {
            //设置下班时间
            new TimePickerDialog.Builder().setThemeColor(ColorUtil.randomColor())
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
        });
        viewBinding.endAmDuty.setOnClickListener(v -> {
            if (amCountDownTimer != null) {
                amCountDownTimer.cancel();
                viewBinding.startTimeView.setText("--");
            }
        });
        viewBinding.endPmDuty.setOnClickListener(v -> {
            if (pmCountDownTimer != null) {
                pmCountDownTimer.cancel();
                viewBinding.endTimeView.setText("--");
            }
        });
    }

    private void onDuty(long millSeconds) {
        long deltaTime = deltaTime(millSeconds / 1000);
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
            EasyToast.show(requireContext(), "已有任务在进行中");
        }
    }

    private void offDuty(long millSeconds) {
        long deltaTime = deltaTime(millSeconds / 1000);
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
            EasyToast.show(requireContext(), "已有任务在进行中");
        }
    }

    /**
     * 计算时间差
     *
     * @param fixedTime 结束时间
     */
    private long deltaTime(long fixedTime) {
        long currentTime = (System.currentTimeMillis() / 1000);
        if (fixedTime > currentTime) {
            return (fixedTime - currentTime);
        } else {
            EasyToast.show(requireContext(), "时间设置异常");
        }
        return 0L;
    }

    @Override
    public void onDestroyView() {
        if (timer != null) {
            timer.cancel();
        }
        super.onDestroyView();
    }
}