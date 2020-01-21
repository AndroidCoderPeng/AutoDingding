package com.pengxh.autodingding.service;

import android.app.AlarmManager;
import android.app.PendingIntent;
import android.app.Service;
import android.content.Intent;
import android.os.IBinder;
import android.os.SystemClock;
import android.support.annotation.Nullable;
import android.util.Log;

import com.pengxh.autodingding.utils.AlarmReceiver;

import java.util.Date;

/**
 * @author: Pengxh
 * @email: 290677893@qq.com
 * @description: TODO
 * @date: 2020/1/19 15:59
 */
public class BackgroundService extends Service {
    private static final String TAG = "LongRunningService";

    /**
     * SystemClock.elapsedRealtime()方法可以获取到系统开机至今所经历时间的毫秒数
     * ELAPSED_REALTIME、ELAPSED_REALTIME_WAKEUP
     * <p>
     * System.currentTimeMillis()方法可以获取到1970 年1 月1 日0 点至今所经历时间的毫秒数
     * RTC、RTC_WAKEUP
     */
    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        new Thread(new Runnable() {
            @Override
            public void run() {
                Log.d(TAG, "run: executed at " + new Date().toString());
            }
        }).start();
        AlarmManager alarmManager = (AlarmManager) getSystemService(ALARM_SERVICE);

        int pendingTime = 60 * 1000;
        long triggerAtTime = SystemClock.elapsedRealtime() + pendingTime;

        Intent alarmIntent = new Intent(this, AlarmReceiver.class);
        PendingIntent pendingIntent = PendingIntent.getBroadcast(this, 0, alarmIntent, 0);
        alarmManager.setExact(AlarmManager.ELAPSED_REALTIME_WAKEUP, triggerAtTime, pendingIntent);
        return super.onStartCommand(intent, flags, startId);
    }

    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }
}
