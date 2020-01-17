package com.pengxh.autodingding.service;

import android.annotation.SuppressLint;
import android.app.Service;
import android.arch.lifecycle.MutableLiveData;
import android.arch.lifecycle.Observer;
import android.content.Intent;
import android.os.CountDownTimer;
import android.os.Handler;
import android.os.IBinder;
import android.os.Message;
import android.support.annotation.Nullable;
import android.util.Log;

import com.pengxh.autodingding.ui.MainActivity;
import com.pengxh.autodingding.utils.Constant;
import com.pengxh.autodingding.utils.LiveDataBus;
import com.pengxh.autodingding.utils.SendMailUtil;
import com.pengxh.autodingding.utils.Utils;

import java.util.Arrays;

/**
 * @description: TODO 钉钉自动打卡服务
 * @author: Pengxh
 * @email: 290677893@qq.com
 * @date: 2019/12/25 23:17
 */
public class AutoDingdingService extends Service {

    private static final String TAG = "AutoDingdingService";
    private Observer<Long> amKaoQinObserver, pmKaoQinObserver;
    private MutableLiveData<Long> amKaoQinLiveData, pmKaoQinLiveData;
    private Observer<String> notificationObserver;
    private MutableLiveData<String> notifyMessageLiveData;

    @Override
    public void onCreate() {
        super.onCreate();
        Log.d(TAG, "onCreate: 自动打卡服务已启动");
        amKaoQinLiveData = LiveDataBus.get().with("amKaoQin", Long.class);
        pmKaoQinLiveData = LiveDataBus.get().with("pmKaoQin", Long.class);
        notifyMessageLiveData = LiveDataBus.get().with("notifyMessage", String.class);
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        amKaoQinObserver = new Observer<Long>() {
            @Override
            public void onChanged(@Nullable Long aLong) {
                new CountDownTimer(aLong * 1000, 1000) {
                    @Override
                    public void onTick(long l) {
                        int tickTime = (int) (l / 1000);
                        //更新UI
                        LiveDataBus.get().with("amUpdate").setValue(tickTime);
                    }

                    @Override
                    public void onFinish() {
                        Utils.openDingding(Constant.DINGDING);
                        handler.sendEmptyMessageDelayed(1, 10 * 1000);
                    }
                }.start();
            }
        };
        amKaoQinLiveData.observeForever(amKaoQinObserver);

        pmKaoQinObserver = new Observer<Long>() {

            @Override
            public void onChanged(@Nullable Long aLong) {
                new CountDownTimer(aLong * 1000, 1000) {
                    @Override
                    public void onTick(long l) {
                        int tickTime = (int) (l / 1000);
                        //更新UI
                        LiveDataBus.get().with("pmUpdate").setValue(tickTime);
                    }

                    @Override
                    public void onFinish() {
                        Utils.openDingding(Constant.DINGDING);
                        handler.sendEmptyMessageDelayed(1, 10 * 1000);
                    }
                }.start();
            }
        };
        pmKaoQinLiveData.observeForever(pmKaoQinObserver);

        notificationObserver = new Observer<String>() {
            @Override
            public void onChanged(String s) {
                String[] split = s.split(",");
                Log.d(TAG, "onReceive: " + Arrays.toString(split));
                if (split[1].contains("上班打卡") || split[1].contains("下班打卡")) {
                    Utils.openDingding(Constant.DINGDING);
                    handler.sendEmptyMessageDelayed(1, 10 * 1000);
                } else {
                    Log.i(TAG, "onReceive: 普通消息，不处理");
                }
            }
        };
        notifyMessageLiveData.observeForever(notificationObserver);
        return super.onStartCommand(intent, flags, startId);
    }

    @SuppressLint("HandlerLeak")
    private Handler handler = new Handler() {
        @Override
        public void handleMessage(Message msg) {
            if (msg.what == 1) {
                Intent intent = new Intent(getApplicationContext(), MainActivity.class);
                intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                startActivity(intent);

                String qqEmail = Utils.readEmailAddress();
                //发送打卡成功的邮件
                Log.d(TAG, "handleMessage: " + qqEmail);
                if (qqEmail.equals("")) {
                    return;
                }
                SendMailUtil.send(qqEmail);
            }
        }
    };

    @Override
    public void onDestroy() {
        super.onDestroy();
        amKaoQinLiveData.removeObserver(amKaoQinObserver);
        pmKaoQinLiveData.removeObserver(pmKaoQinObserver);
        notifyMessageLiveData.removeObserver(notificationObserver);
    }

    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }
}