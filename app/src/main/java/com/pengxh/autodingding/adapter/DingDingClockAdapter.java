package com.pengxh.autodingding.adapter;

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.Intent;
import android.os.CountDownTimer;
import android.os.Handler;
import android.os.Message;
import android.support.annotation.NonNull;
import android.support.v4.app.FragmentManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Switch;
import android.widget.TextView;

import com.jzxiang.pickerview.TimePickerDialog;
import com.jzxiang.pickerview.data.Type;
import com.pengxh.app.multilib.utils.ColorUtil;
import com.pengxh.app.multilib.widget.EasyToast;
import com.pengxh.autodingding.R;
import com.pengxh.autodingding.bean.TimeSetBean;
import com.pengxh.autodingding.ui.DingDingClockActivity;
import com.pengxh.autodingding.utils.Constant;
import com.pengxh.autodingding.utils.SQLiteUtil;
import com.pengxh.autodingding.utils.SendMailUtil;
import com.pengxh.autodingding.utils.TimeOrDateUtil;
import com.pengxh.autodingding.utils.Utils;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * @author: Pengxh
 * @email: 290677893@qq.com
 * @description: TODO
 * @date: 2020/1/19 16:49
 */
public class DingDingClockAdapter extends RecyclerView.Adapter {

    private static final String TAG = "DingDingClockAdapter";
    private Context mContext;
    private List<String> dateList;
    private FragmentManager fragmentManager;
    private LayoutInflater inflater;
    private SQLiteUtil sqLiteUtil;

    public DingDingClockAdapter(Context context, List<String> list, FragmentManager manager) {
        this.mContext = context;
        this.dateList = list;
        this.fragmentManager = manager;
        sqLiteUtil = SQLiteUtil.getInstance();
        inflater = LayoutInflater.from(context);
    }

    @NonNull
    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int i) {
        return new DingDingViewHolder(inflater.inflate(R.layout.item_clock_gridview, parent, false));
    }

    @Override
    public void onBindViewHolder(@NonNull RecyclerView.ViewHolder viewHolder, int position) {
        DingDingViewHolder itemHolder = (DingDingViewHolder) viewHolder;
        long delta = position * 24 * 60 * 60 * 1000L;
        itemHolder.bindHolder(dateList.get(position), Utils.uuid(), delta);
    }

    @Override
    public int getItemCount() {
        return dateList == null ? 0 : dateList.size();
    }

    class DingDingViewHolder extends RecyclerView.ViewHolder {

        private TextView weekTextView;
        private Switch startWorkSwitch;
        private TextView startWorkTextView;
        private TextView endWorkTextView;
        private TextView openStartTimeTick;
        private TextView openEndTimeTick;

        private DingDingViewHolder(View itemView) {
            super(itemView);
            weekTextView = itemView.findViewById(R.id.weekTextView);
            startWorkSwitch = itemView.findViewById(R.id.startWorkSwitch);
            startWorkTextView = itemView.findViewById(R.id.startWorkTextView);
            endWorkTextView = itemView.findViewById(R.id.endWorkTextView);
            openStartTimeTick = itemView.findViewById(R.id.openStartTimeTick);
            openEndTimeTick = itemView.findViewById(R.id.openEndTimeTick);
        }

        private String amKaoQin;
        private String pmKaoQin;
        private CountDownTimer startTimer;
        private CountDownTimer endTimer;
        private Map<String, Long> deltaTimeMap = new HashMap<>();

        void bindHolder(String date, String uuid, long positionTime) {
            weekTextView.setText(date);
            //设置上班打卡时间
            startWorkTextView.setOnClickListener(view -> new TimePickerDialog.Builder()
                    .setMinMillseconds(System.currentTimeMillis())
                    .setMaxMillseconds(System.currentTimeMillis() + Constant.ONE_MONTH)
                    .setThemeColor(ColorUtil.getRandomColor())
                    .setType(Type.MONTH_DAY_HOUR_MIN)
                    .setCyclic(false)
                    .setCallBack((timePickerView, millSeconds) -> {
                        //计算时间差
                        long deltaTime = TimeOrDateUtil.deltaTime(millSeconds / 1000);
                        Log.d(TAG, "amKaoQin: " + deltaTime);
                        if (deltaTime == 0) {
                            Log.w(TAG, "", new Throwable());
                            return;
                        }
                        deltaTimeMap.put(uuid + "amKaoQin", deltaTime);

                        amKaoQin = TimeOrDateUtil.timestampToTime(millSeconds);
                        startWorkTextView.setText(amKaoQin);
                    }).build().show(fragmentManager, "hour_minute"));

            //设置下班打卡时间
            endWorkTextView.setOnClickListener(view -> new TimePickerDialog.Builder()
                    .setMinMillseconds(System.currentTimeMillis() + positionTime)
                    .setMaxMillseconds(System.currentTimeMillis() + Constant.ONE_MONTH)
                    .setThemeColor(ColorUtil.getRandomColor())
                    .setType(Type.MONTH_DAY_HOUR_MIN)
                    .setCyclic(false)
                    .setCallBack((timePickerView, millSeconds) -> {
                        //计算时间差
                        long deltaTime = TimeOrDateUtil.deltaTime(millSeconds / 1000);
                        Log.d(TAG, "pmKaoQin: " + deltaTime);
                        if (deltaTime == 0) {
                            Log.w(TAG, "", new Exception());
                            return;
                        }
                        deltaTimeMap.put(uuid + "pmKaoQin", deltaTime);

                        pmKaoQin = TimeOrDateUtil.timestampToTime(millSeconds);
                        endWorkTextView.setText(pmKaoQin);
                    }).build().show(fragmentManager, "hour_minute"));

            startWorkSwitch.setOnCheckedChangeListener((compoundButton, b) -> {
                if (deltaTimeMap.isEmpty()) {
                    Log.w(TAG, "bindHolder: ", new NullPointerException());
                    compoundButton.setChecked(false);
                    EasyToast.showToast("任务时间未设置，无法开始任务", EasyToast.WARING);
                } else {
                    if (b) {
                        openStartTimeTick.setVisibility(View.VISIBLE);
                        openEndTimeTick.setVisibility(View.VISIBLE);

                        TimeSetBean timeSetBean = new TimeSetBean();
                        timeSetBean.setUuid(uuid);
                        timeSetBean.setStartTime(amKaoQin);
                        timeSetBean.setEndTime(pmKaoQin);

                        timeSetBean.setIsStart("T");
                        sqLiteUtil.saveTime(timeSetBean);

                        long startDeltaTime = deltaTimeMap.get(uuid + "amKaoQin");
                        long endDeltaTime = deltaTimeMap.get(uuid + "pmKaoQin");

                        //启动倒计时
                        startTimer = new CountDownTimer(startDeltaTime * 1000, 1000) {
                            @Override
                            public void onTick(long millisUntilFinished) {
                                openStartTimeTick.setText(String.valueOf(millisUntilFinished / 1000));
                            }

                            @Override
                            public void onFinish() {
                                Utils.openDingding(Constant.DINGDING);
                                handler.sendEmptyMessageDelayed(10, 10 * 1000);
                            }
                        }.start();

                        endTimer = new CountDownTimer(endDeltaTime * 1000, 1000) {
                            @Override
                            public void onTick(long millisUntilFinished) {
                                openEndTimeTick.setText(String.valueOf(millisUntilFinished / 1000));
                            }

                            @Override
                            public void onFinish() {
                                Utils.openDingding(Constant.DINGDING);
                                handler.sendEmptyMessageDelayed(10, 10 * 1000);
                            }
                        }.start();
                    } else {
                        openStartTimeTick.setVisibility(View.INVISIBLE);
                        openEndTimeTick.setVisibility(View.INVISIBLE);
                        startWorkTextView.setText("--:--");
                        endWorkTextView.setText("--:--");
                        startTimer.cancel();
                        endTimer.cancel();
                        deltaTimeMap.remove(uuid + "amKaoQin");
                        deltaTimeMap.remove(uuid + "pmKaoQin");
                        sqLiteUtil.deleteByUuid(uuid);
                    }
                }
            });
        }
    }

    @SuppressLint("HandlerLeak")
    private Handler handler = new Handler() {
        @Override
        public void handleMessage(Message msg) {
            if (msg.what == 10) {
                Intent intent = new Intent(mContext, DingDingClockActivity.class);
                intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                mContext.startActivity(intent);

                String emailAddress = Utils.readEmailAddress();
                //发送打卡成功的邮件
                Log.d(TAG, "handleMessage: " + emailAddress);
                if (emailAddress.equals("")) {
                    return;
                }
                SendMailUtil.send(emailAddress);
            }
        }
    };
}