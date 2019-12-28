package com.pengxh.autodingding.service;

import android.annotation.SuppressLint;
import android.app.Service;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.os.CountDownTimer;
import android.os.Environment;
import android.os.Handler;
import android.os.IBinder;
import android.os.Message;
import android.telephony.SmsMessage;
import android.util.Log;

import com.pengxh.app.multilib.utils.BroadcastManager;
import com.pengxh.app.multilib.widget.EasyToast;
import com.pengxh.autodingding.utils.BroadcastAction;
import com.pengxh.autodingding.utils.Utils;

import java.util.Timer;
import java.util.TimerTask;

/**
 * @description: TODO
 * @author: Pengxh
 * @email: 290677893@qq.com
 * @date: 2019/12/25 23:17
 */
public class AutoDingdingService extends Service {

    private static final String TAG = "AutoDingdingService";
    private String sdCardDir = Environment.getExternalStorageDirectory() + "/ScreenShot/";
    private BroadcastManager broadcastManager;

    @Override
    public void onCreate() {
        super.onCreate();
        Log.d(TAG, "onCreate: 自动打卡服务已启动");
        broadcastManager = BroadcastManager.getInstance(this);
        broadcastManager.addAction(BroadcastAction.ACTIONS, new BroadcastReceiver() {
            @SuppressLint("SetTextI18n")
            @Override
            public void onReceive(final Context context, Intent intent) {
                //更新UI
                String action = intent.getAction();
                if (action != null) {
                    Utils.createNotification(context);
                    if (action.equals(BroadcastAction.ACTIONS[0])) {
                        String data = intent.getStringExtra("data");
                        long deltaTime = Long.parseLong(data) * 1000;
                        new CountDownTimer(deltaTime, 1000) {
                            @Override
                            public void onTick(long l) {
                                int tickTime = (int) (l / 1000);
                                //更新UI
                                broadcastManager.sendBroadcast(BroadcastAction.ACTIONS[2], String.valueOf(tickTime));
                            }

                            @Override
                            public void onFinish() {
                                Utils.openDingding(context, BroadcastAction.DINGDING);
                                captureHandler.sendEmptyMessage(1);
                            }
                        }.start();
                    } else if (action.equals(BroadcastAction.ACTIONS[1])) {
                        String data = intent.getStringExtra("data");
                        long deltaTime = Long.parseLong(data) * 1000;
                        new CountDownTimer(deltaTime, 1000) {
                            @Override
                            public void onTick(long l) {
                                int tickTime = (int) (l / 1000);
                                //更新UI
                                broadcastManager.sendBroadcast(BroadcastAction.ACTIONS[3], String.valueOf(tickTime));
                            }

                            @Override
                            public void onFinish() {
                                Utils.openDingding(context, BroadcastAction.DINGDING);
                                captureHandler.sendEmptyMessage(1);
                            }
                        }.start();
                    } else if (action.equals(BroadcastAction.ACTIONS[4])) {
                        //短信监听
                        StringBuilder content = new StringBuilder();//用于存储短信内容
                        Bundle bundle = intent.getExtras();//获取短信内容
                        String format = intent.getStringExtra("format");
                        if (bundle != null) {
                            Object[] pduObjects = (Object[]) bundle.get("pdus");//根据pdus关键字获取短信字节数组，数组内的每个元素都是一条短信
                            for (Object object : pduObjects) {
                                SmsMessage message = SmsMessage.createFromPdu((byte[]) object, format);//将字节数组转化为Message对象
                                content.append(message.getMessageBody());//获取短信内容
                            }
                            String sms = content.toString();
                            Log.d(TAG, "收到短信: " + sms);
                            if (sms.equals("签到打卡")) {
                                Utils.openDingding(context, BroadcastAction.DINGDING);
                                captureHandler.sendEmptyMessage(1);
                            }
                        }
                    }
                } else {
                    Log.e(TAG, "onReceive: ", new Throwable());
                }
            }
        });
    }

    @SuppressLint("HandlerLeak")
    private Handler captureHandler = new Handler() {
        @Override
        public void handleMessage(Message msg) {
            if (msg.what == 1) {
                EasyToast.showToast("开始截屏", EasyToast.DEFAULT);
                new Timer().schedule(new TimerTask() {
                    @Override
                    public void run() {
                        captureBitmap();
                    }
                }, 10 * 1000);
            }
        }
    };

    private void captureBitmap() {

    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        broadcastManager.destroy(BroadcastAction.ACTIONS);
    }

    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }
}
