package com.pengxh.autodingding.service;

import android.app.Service;
import android.content.Intent;
import android.os.IBinder;
import android.util.Log;

/**
 * @description: TODO 钉钉自动打卡服务（暂时不用）
 * @author: Pengxh
 * @email: 290677893@qq.com
 * @date: 2019/12/25 23:17
 */
public class AutoDingDingService extends Service {

    private static final String TAG = "AutoDingdingService";

    @Override
    public void onCreate() {
        super.onCreate();
        Log.d(TAG, "onCreate: 自动打卡服务已启动");
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {

        return super.onStartCommand(intent, flags, startId);
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
    }

    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }
}