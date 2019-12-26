package com.pengxh.audodingding.service;

import android.app.Service;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.CountDownTimer;
import android.os.Environment;
import android.os.IBinder;
import android.provider.MediaStore;
import android.util.Log;

import com.pengxh.app.multilib.utils.BroadcastManager;
import com.pengxh.audodingding.utils.BroadcastAction;
import com.pengxh.audodingding.utils.Utils;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;

/**
 * @description: TODO
 * @author: Pengxh
 * @email: 290677893@qq.com
 * @date: 2019/12/25 23:17
 */
public class AutoDingdingService extends Service {

    private static final String TAG = "AutoDingdingService";
    private BroadcastManager broadcastManager;

    @Override
    public void onCreate() {
        super.onCreate();
        Log.d(TAG, "onCreate: 自动打卡服务已启动");
        broadcastManager = BroadcastManager.getInstance(this);
        broadcastManager.addAction(BroadcastAction.ACTION_KAOQIN_AM, new BroadcastReceiver() {
            @Override
            public void onReceive(final Context context, Intent intent) {
                //上班参数设置广播
                String action = intent.getAction();
                if (action != null && action.equals(BroadcastAction.ACTION_KAOQIN_AM)) {
                    String data = intent.getStringExtra("data");
                    long deltaTime = Long.parseLong(data) * 1000;
                    new CountDownTimer(deltaTime, 1000) {
                        @Override
                        public void onTick(long l) {
                            int tickTime = (int) (l / 1000);
                            //更新UI
                            broadcastManager.sendBroadcast(BroadcastAction.ACTION_UPDATE_AM, String.valueOf(tickTime));
                        }

                        @Override
                        public void onFinish() {
                            Utils.openDingding(context, BroadcastAction.DINGDING);
                        }
                    }.start();
                }
            }
        });
        broadcastManager.addAction(BroadcastAction.ACTION_KAOQIN_PM, new BroadcastReceiver() {
            @Override
            public void onReceive(final Context context, Intent intent) {
                //下班参数设置广播
                String action = intent.getAction();
                if (action != null && action.equals(BroadcastAction.ACTION_KAOQIN_PM)) {
                    String data = intent.getStringExtra("data");
                    long deltaTime = Long.parseLong(data) * 1000;
                    new CountDownTimer(deltaTime, 1000) {
                        @Override
                        public void onTick(long l) {
                            int tickTime = (int) (l / 1000);
                            //更新UI
                            broadcastManager.sendBroadcast(BroadcastAction.ACTION_UPDATE_PM, String.valueOf(tickTime));
                        }

                        @Override
                        public void onFinish() {
                            Utils.openDingding(context, BroadcastAction.DINGDING);
                        }
                    }.start();
                }
            }
        });
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        broadcastManager.destroy(BroadcastAction.ACTION_KAOQIN_AM, BroadcastAction.ACTION_KAOQIN_PM);
    }

    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }
}
