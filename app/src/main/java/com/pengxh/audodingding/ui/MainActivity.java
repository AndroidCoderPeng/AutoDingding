package com.pengxh.audodingding.ui;

import android.annotation.SuppressLint;
import android.content.Intent;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.Build;
import android.os.Environment;
import android.os.Handler;
import android.os.Message;
import android.provider.MediaStore;
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
import com.pengxh.app.multilib.utils.BroadcastManager;
import com.pengxh.app.multilib.utils.ColorUtil;
import com.pengxh.app.multilib.utils.SaveKeyValues;
import com.pengxh.app.multilib.widget.EasyToast;
import com.pengxh.audodingding.R;
import com.pengxh.audodingding.service.AutoDingdingService;
import com.pengxh.audodingding.utils.BroadcastAction;
import com.pengxh.audodingding.utils.Utils;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;

import butterknife.BindView;
import butterknife.OnClick;

public class MainActivity extends BaseNormalActivity implements View.OnClickListener {

    private static final String TAG = "MainActivity";
    private AlertView alertView;

    @BindView(R.id.startTimeBtn)
    Button startTimeBtn;
    @BindView(R.id.endTimeBtn)
    Button endTimeBtn;
    @BindView(R.id.mTextViewStart)
    TextView mTextViewStart;
    @BindView(R.id.mTextViewEnd)
    TextView mTextViewEnd;
    private BroadcastManager broadcastManager;

    @Override
    public void initView() {
        setContentView(R.layout.activity_main);
    }

    @Override
    public void initData() {
        broadcastManager = BroadcastManager.getInstance(this);
        String amKaoQin = (String) SaveKeyValues.getValue("amKaoQin", "");
        String pmKaoQin = (String) SaveKeyValues.getValue("pmKaoQin", "");
        if (!amKaoQin.equals("")) {
            startTimeBtn.setText(amKaoQin);
        }
        if (!pmKaoQin.equals("")) {
            endTimeBtn.setText(pmKaoQin);
        }
    }

    @Override
    public void initEvent() {
        if (Utils.isAppAvailable(this, BroadcastAction.DINGDING)) {
            //后期优化到服务里面执行倒计时任务
            startService(new Intent(this, AutoDingdingService.class));
        } else {
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
                                String amKaoQin = Utils.timestampToDate(millSeconds);
                                Log.d(TAG, "onDateSet: " + amKaoQin);
                                SaveKeyValues.putValue("amKaoQin", amKaoQin);
                                startTimeBtn.setText(amKaoQin);
                                //计算时间差
                                long deltaTime = Utils.deltaTime(millSeconds / 1000);
                                if (deltaTime == 0) {
                                    return;
                                }
                                broadcastManager.sendBroadcast(BroadcastAction.ACTION_KAOQIN_AM, String.valueOf(deltaTime));
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
                                String pmKaoQin = Utils.timestampToDate(millSeconds);
                                Log.d(TAG, "onDateSet: " + pmKaoQin);
                                SaveKeyValues.putValue("pmKaoQin", pmKaoQin);
                                endTimeBtn.setText(pmKaoQin);
                                broadcastManager.sendBroadcast(BroadcastAction.ACTION_KAOQIN_PM, pmKaoQin);
                                //计算时间差
                                long deltaTime = Utils.deltaTime(millSeconds / 1000);
                                if (deltaTime == 0) {
                                    return;
                                }
                                broadcastManager.sendBroadcast(BroadcastAction.ACTION_KAOQIN_PM, String.valueOf(deltaTime));
                            }
                        });
                break;
        }
        builder.build().show(getSupportFragmentManager(), "month_day_hour_minute");
    }

    @SuppressLint("HandlerLeak")
    private Handler handler = new Handler() {
        @SuppressLint({"SetTextI18n", "WrongConstant"})
        @Override
        public void handleMessage(Message msg) {
            switch (msg.what) {
                case 100:
                    mTextViewStart.setText("倒计时：" + msg.obj + "秒");
                    break;
                case 101:
                    mTextViewEnd.setText("倒计时：" + msg.obj + "秒");
                    break;
                case 102:
                    EasyToast.showToast("开始截屏", EasyToast.DEFAULT);
                    //截屏后将图片发送到指定邮箱
                    if (Build.VERSION.SDK_INT >= 21) {
                        startScreenShot();
                    } else {
                        Log.e(TAG, "版本过低,无法截屏");
                    }
                    break;
                default:
                    break;
            }
        }
    };

    private void startScreenShot() {
        String shotCmd = "screencap -p " + sdCardDir + "temp.jpg" + " \n";
        try {
            Runtime.getRuntime().exec(shotCmd);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    /**
     * 保存文件
     */
    public String sdCardDir = Environment.getExternalStorageDirectory() + "/ScreenShot/";

    private void saveBitmap(Bitmap bitmap) {
        try {
            File dirFile = new File(sdCardDir);
            if (!dirFile.exists()) {              //如果不存在，那就建立这个文件夹
                dirFile.mkdirs();
            }
            File file = new File(sdCardDir, "temp.jpg");
            FileOutputStream fos = new FileOutputStream(file);
            bitmap.compress(Bitmap.CompressFormat.JPEG, 100, fos);
            fos.flush();
            fos.close();
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }
        // 把文件插入到系统图库
        MediaStore.Images.Media.insertImage(this.getContentResolver(), bitmap, "temp.jpg", null);
        // 通知图库更新
        sendBroadcast(new Intent(Intent.ACTION_MEDIA_SCANNER_SCAN_FILE, Uri.parse("file://" + "/sdcard/namecard/")));
    }
}