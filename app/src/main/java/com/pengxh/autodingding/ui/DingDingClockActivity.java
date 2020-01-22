package com.pengxh.autodingding.ui;

import android.annotation.SuppressLint;
import android.app.Dialog;
import android.content.Intent;
import android.os.Handler;
import android.os.Message;
import android.util.Log;
import android.view.KeyEvent;
import android.view.View;
import android.widget.CompoundButton;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.Switch;
import android.widget.TextView;

import com.aihook.alertview.library.AlertView;
import com.gyf.immersionbar.ImmersionBar;
import com.pengxh.app.multilib.base.BaseNormalActivity;
import com.pengxh.app.multilib.utils.SaveKeyValues;
import com.pengxh.app.multilib.widget.EasyToast;
import com.pengxh.app.multilib.widget.dialog.InputDialog;
import com.pengxh.autodingding.R;
import com.pengxh.autodingding.utils.Constant;
import com.pengxh.autodingding.utils.SendMailUtil;
import com.pengxh.autodingding.utils.TimeOrDateUtil;
import com.pengxh.autodingding.utils.Utils;
import com.pengxh.autodingding.widgets.EasyPopupWindow;

import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Timer;
import java.util.TimerTask;

import butterknife.BindView;
import butterknife.OnClick;

/**
 * @author: Pengxh
 * @email: 290677893@qq.com
 * @description: TODO
 * @date: 2020/1/19 16:19
 */
public class DingDingClockActivity extends BaseNormalActivity implements View.OnClickListener {

    private static final String TAG = "DingDingClockActivity";
    private static final List<String> items = Arrays.asList("打卡一天", "邮箱设置");
    private Map<Integer, String> timeMap = new HashMap<>();

    @BindView(R.id.titleLayout)
    RelativeLayout titleLayout;
    @BindView(R.id.textViewTitle)
    TextView textViewTitle;
    @BindView(R.id.imageViewTitleRight)
    ImageView imageViewTitleRight;
    @BindView(R.id.currentTime)
    TextView currentTime;
    @BindView(R.id.startWorkTextView)
    TextView startWorkTextView;
    @BindView(R.id.endWorkTextView)
    TextView endWorkTextView;
    @BindView(R.id.startWorkSwitch)
    Switch startWorkSwitch;
    @BindView(R.id.endWorkSwitch)
    Switch endWorkSwitch;

    @Override
    public void initView() {
        setContentView(R.layout.activity_clock);
        ImmersionBar.with(this).fitsSystemWindows(true).statusBarColor(R.color.colorAppThemeLight).init();
    }

    @Override
    public void initData() {
        boolean isFirst = (boolean) SaveKeyValues.getValue("isFirst", true);
        if (isFirst) {
            SaveKeyValues.putValue("isFirst", false);
            new AlertView("※温馨提醒※", "本软件仅供内部使用，严禁商用或者用作其他非法用途", null, new String[]{"确定"}, null, this, AlertView.Style.Alert, null).setCancelable(false).show();
        }
        String emailAddress = Utils.readEmailAddress();
        if (!emailAddress.equals("")) {
            textViewTitle.setText("打卡通知邮箱：" + emailAddress);
        }
    }

    @Override
    public void initEvent() {
        new Timer().schedule(new TimerTask() {
            @Override
            public void run() {
                String systemTime = TimeOrDateUtil.timestampToTime(System.currentTimeMillis());
//                currentTime.setTextColor(ColorUtil.getRandomColor());
                currentTime.setText(systemTime);
            }
        }, 0, 1000);
        if (!Utils.isAppAvailable(Constant.DINGDING)) {
            new AlertView("温馨提示", "手机没有安装钉钉软件，无法自动打卡", null, new String[]{"确定"}, null, this, AlertView.Style.Alert,
                    (o, position) -> DingDingClockActivity.this.finish()).setCancelable(false).show();
        }

        startWorkSwitch.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                if (timeMap.get(R.id.startLayout) == null) {
                    buttonView.setChecked(false);
                    EasyToast.showToast("还未设置时间", EasyToast.WARING);
                } else {
                    Timer startTimer = new Timer();
                    if (isChecked) {
                        final String startTime = timeMap.get(R.id.startLayout);
                        startTimer.schedule(new TimerTask() {
                            @Override
                            public void run() {
                                String systemTime = TimeOrDateUtil.timestampToTime(System.currentTimeMillis());
                                if (startTime.equals(systemTime)) {
                                    Utils.openDingding(Constant.DINGDING);
                                    handler.sendEmptyMessageDelayed(10, 10 * 1000);
                                }
                            }
                        }, 0, 1000);//1s检查一次
                    } else {
                        startTimer.cancel();
                        startWorkTextView.setText("打卡时间：--:--:--");
                        timeMap.remove(R.id.startLayout);
                    }
                }
            }
        });

        endWorkSwitch.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                if (timeMap.get(R.id.endLayout) == null) {
                    buttonView.setChecked(false);
                    EasyToast.showToast("还未设置时间", EasyToast.WARING);
                } else {
                    Timer endTimer = new Timer();
                    if (isChecked) {
                        final String endTime = timeMap.get(R.id.endLayout);
                        endTimer.schedule(new TimerTask() {
                            @Override
                            public void run() {
                                String systemTime = TimeOrDateUtil.timestampToTime(System.currentTimeMillis());
                                if (endTime.equals(systemTime)) {
                                    Utils.openDingding(Constant.DINGDING);
                                    handler.sendEmptyMessageDelayed(10, 10 * 1000);
                                }
                            }
                        }, 0, 1000);
                    } else {
                        endTimer.cancel();
                        endWorkTextView.setText("打卡时间：--:--:--");
                        timeMap.remove(R.id.endLayout);
                    }
                }
            }
        });
    }

    @OnClick({R.id.imageViewTitleRight, R.id.introduceText, R.id.startLayout, R.id.endLayout})
    @Override
    public void onClick(View v) {
        if (v.getId() == R.id.imageViewTitleRight) {
            EasyPopupWindow easyPopupWindow = new EasyPopupWindow(this, items);
            easyPopupWindow.setPopupWindowClickListener(new EasyPopupWindow.PopupWindowClickListener() {
                @Override
                public void popupWindowClick(int position) {
                    if (position == 0) {
                        startMainActivity();
                    } else if (position == 1) {
                        setEmailAddress();
                    }
                }
            });
            easyPopupWindow.showAsDropDown(titleLayout, titleLayout.getWidth(), 0);
        } else if (v.getId() == R.id.introduceText) {
            new AlertView("功能介绍", getResources().getString(R.string.about),
                    null, new String[]{"确定"}, null,
                    DingDingClockActivity.this, AlertView.Style.Alert,
                    null).setCancelable(false).show();
        } else if (v.getId() == R.id.startLayout) {
            String currentDate = TimeOrDateUtil.getCurrentDate();
            long startTimeMillis = TimeOrDateUtil.dateToTimestamp(currentDate + " 8:00:00");//8:00:00
            long endTimeMillis = TimeOrDateUtil.dateToTimestamp(currentDate + " 9:00:00");//9:00:00
            //在起始时间和结束时间之间取随机数，然后转为真实时间作为随机打卡时间
            String randomStartTime = TimeOrDateUtil.getRandomTime(startTimeMillis, endTimeMillis);

            timeMap.put(R.id.startLayout, randomStartTime);
            startWorkTextView.setText("打卡时间：" + randomStartTime);
        } else if (v.getId() == R.id.endLayout) {
            String currentDate = TimeOrDateUtil.getCurrentDate();
            long startTimeMillis = TimeOrDateUtil.dateToTimestamp(currentDate + " 17:30:00");//17:30:00
            long endTimeMillis = TimeOrDateUtil.dateToTimestamp(currentDate + " 18:30:00");//18:30:00
            //在起始时间和结束时间之间取随机数，然后转为真实时间作为随机打卡时间
            String randomEndTime = TimeOrDateUtil.getRandomTime(startTimeMillis, endTimeMillis);

            timeMap.put(R.id.endLayout, randomEndTime);//以View的ID作为key
            endWorkTextView.setText("打卡时间：" + randomEndTime);
        }
    }

    private void startMainActivity() {
        startActivity(new Intent(this, MainActivity.class));//一天版
    }

    private void setEmailAddress() {
        new InputDialog.Builder().setContext(this).setTitle("设置邮箱").setNegativeButton("取消").setPositiveButton("确定").setOnDialogClickListener(new InputDialog.onDialogClickListener() {
            @Override
            public void onConfirmClick(Dialog dialog, String input) {
                if (!input.isEmpty()) {
                    Utils.saveEmailAddress(input);
                    textViewTitle.setText("打卡通知邮箱：" + input);
                    dialog.dismiss();
                } else {
                    EasyToast.showToast("什么都还没输入呢！", EasyToast.ERROR);
                }
            }

            @Override
            public void onCancelClick(Dialog dialog) {
                dialog.dismiss();
            }
        }).build().show();
    }

    //屏蔽返回键
    @Override
    public boolean onKeyDown(int keyCode, KeyEvent event) {
        if (keyCode == KeyEvent.KEYCODE_BACK) {
            if (timeMap.size() == 0) {
                finish();
            } else {
                showMessageDialog();
            }
            return true;
        }
        return false;
    }

    private void showMessageDialog() {
        new AlertView("温馨提示", "当前有正在进行中的任务...",
                null, new String[]{"确定"}, null,
                this, AlertView.Style.Alert, null).setCancelable(false).show();
    }

    @SuppressLint("HandlerLeak")
    private Handler handler = new Handler() {
        @Override
        public void handleMessage(Message msg) {
            if (msg.what == 10) {
                Intent intent = new Intent(getApplicationContext(), DingDingClockActivity.class);
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