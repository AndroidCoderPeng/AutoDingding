package com.pengxh.autodingding.ui;

import android.annotation.SuppressLint;
import android.arch.lifecycle.MutableLiveData;
import android.arch.lifecycle.Observer;
import android.content.Intent;
import android.support.annotation.Nullable;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.gyf.immersionbar.ImmersionBar;
import com.jzxiang.pickerview.TimePickerDialog;
import com.jzxiang.pickerview.data.Type;
import com.jzxiang.pickerview.listener.OnDateSetListener;
import com.pengxh.app.multilib.base.BaseNormalActivity;
import com.pengxh.app.multilib.utils.ColorUtil;
import com.pengxh.app.multilib.utils.SaveKeyValues;
import com.pengxh.app.multilib.widget.EasyToast;
import com.pengxh.autodingding.R;
import com.pengxh.autodingding.service.AutoDingdingService;
import com.pengxh.autodingding.utils.Constant;
import com.pengxh.autodingding.utils.LiveDataBus;
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

    private Observer<Integer> amUpdateObserver, pmUpdateObserver;
    private MutableLiveData<Integer> amUpdateLiveData, pmUpdateLiveData;

    @Override
    public void initView() {
        setContentView(R.layout.activity_main);
        ImmersionBar.with(this).fitsSystemWindows(true).statusBarColor(R.color.colorAppThemeLight).init();
        imageViewTitleRight.setVisibility(View.GONE);
    }

    @Override
    public void initData() {
        amUpdateLiveData = LiveDataBus.get().with("amUpdate", int.class);
        pmUpdateLiveData = LiveDataBus.get().with("pmUpdate", int.class);
        String emailAddress = Utils.readEmailAddress();
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
        if (!emailAddress.equals("")) {
            textViewTitle.setText("打卡通知邮箱：" + emailAddress);
        }
    }

    @Override
    public void initEvent() {
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
    }

    @OnClick({R.id.startTimeBtn, R.id.endTimeBtn})
    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.startTimeBtn:
                //设置上班时间
                new TimePickerDialog.Builder().setThemeColor(ColorUtil.getRandomColor())
                        .setCyclic(false)
                        .setMinMillseconds(System.currentTimeMillis())
                        .setMaxMillseconds(System.currentTimeMillis() + Constant.ONE_MONTH)
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
                        .setCyclic(false)
                        .setMinMillseconds(System.currentTimeMillis())
                        .setMaxMillseconds(System.currentTimeMillis() + Constant.ONE_MONTH)
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
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        amUpdateLiveData.removeObserver(amUpdateObserver);
        pmUpdateLiveData.removeObserver(pmUpdateObserver);
    }
}