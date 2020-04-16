package com.pengxh.autodingding.service;

import android.app.Service;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.IBinder;
import android.support.annotation.Nullable;
import android.util.Log;

import com.pengxh.autodingding.utils.TimeChangeReceiver;

/**
 * @description: TODO
 * @author: Pengxh
 * @email: 290677893@qq.com
 * @date: 2020/4/16 20:01
 */
public class TimeService extends Service {

    private static final String TAG = "TimeService";
    private TimeChangeReceiver timeChangeReceiver;


    @Override
    public void onCreate() {
        super.onCreate();
        Log.d(TAG, "onCreate: 系统时间监听服务启动");
        timeChangeReceiver = new TimeChangeReceiver();
        registerReceiver(timeChangeReceiver, new IntentFilter(Intent.ACTION_TIME_TICK));
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        Log.d(TAG, "onStartCommand: 系统时间监听中");
        return super.onStartCommand(intent, flags, startId);
    }

    @Override
    public void onDestroy() {
        Log.d(TAG, "系统时间监听服务被销毁");
        unregisterReceiver(timeChangeReceiver);
        super.onDestroy();
    }

    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }
}
