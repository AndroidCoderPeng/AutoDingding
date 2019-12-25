package com.pengxh.audodingding;

import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;

import com.aihook.alertview.library.AlertView;
import com.aihook.alertview.library.OnItemClickListener;
import com.jzxiang.pickerview.TimePickerDialog;
import com.jzxiang.pickerview.data.Type;
import com.jzxiang.pickerview.listener.OnDateSetListener;
import com.pengxh.app.multilib.base.BaseNormalActivity;
import com.pengxh.app.multilib.utils.ColorUtil;
import com.pengxh.app.multilib.utils.SaveKeyValues;

import butterknife.BindView;
import butterknife.OnClick;

public class MainActivity extends BaseNormalActivity implements View.OnClickListener {

    private static final String TAG = "MainActivity";
    //钉钉包名：com.alibaba.android.rimet
    //启动类名：com.alibaba.android.rimet.biz.home.activity.HomeActivity
    private static final String DINGDING = "com.alibaba.android.rimet";
    private AlertView alertView;
    private boolean hasSetTime = false;

    @BindView(R.id.startTimeBtn)
    Button startTimeBtn;
    @BindView(R.id.endTimeBtn)
    Button endTimeBtn;
    @BindView(R.id.mTextViewStart)
    TextView mTextViewStart;
    @BindView(R.id.mTextViewEnd)
    TextView mTextViewEnd;
    private String startTime;
    private String endTime;

    @Override
    public void initView() {
        setContentView(R.layout.activity_main);
    }

    @Override
    public void initData() {
        startTime = (String) SaveKeyValues.getValue("startTime", "");
        endTime = (String) SaveKeyValues.getValue("endTime", "");
        if (!startTime.equals("")) {
            startTimeBtn.setText(startTime);
        }
        if (!endTime.equals("")) {
            endTimeBtn.setText(endTime);
        }
    }

    @Override
    public void initEvent() {
        if (!Utils.isAppAvilible(this, DINGDING)) {
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

    @OnClick({R.id.startTimeBtn, R.id.endTimeBtn})
    @Override
    public void onClick(View v) {
        TimePickerDialog.Builder builder = new TimePickerDialog.Builder();
        switch (v.getId()) {
            case R.id.startTimeBtn:
                //设置上班时间
                builder.setThemeColor(ColorUtil.getRandomColor())
                        .setType(Type.MONTH_DAY_HOUR_MIN)
                        .setCallBack(new OnDateSetListener() {
                            @Override
                            public void onDateSet(TimePickerDialog timePickerView, long millSeconds) {
                                String startTime = Utils.timestampToDate(millSeconds);
                                Log.d(TAG, "onDateSet: " + startTime);
                                SaveKeyValues.putValue("startTime", startTime);
                                hasSetTime = true;
                            }
                        });
                break;
            case R.id.endTimeBtn:
                //设置下班时间
                builder.setThemeColor(ColorUtil.getRandomColor())
                        .setType(Type.MONTH_DAY_HOUR_MIN)
                        .setCallBack(new OnDateSetListener() {
                            @Override
                            public void onDateSet(TimePickerDialog timePickerView, long millSeconds) {
                                String endTime = Utils.timestampToDate(millSeconds);
                                Log.d(TAG, "onDateSet: " + endTime);
                                SaveKeyValues.putValue("endTime", endTime);
                                hasSetTime = true;
                            }
                        });
                break;
        }
        builder.build().show(getSupportFragmentManager(), "month_day_hour_minute");
    }
}
