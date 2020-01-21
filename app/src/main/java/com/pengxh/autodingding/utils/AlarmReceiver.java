package com.pengxh.autodingding.utils;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;

import com.pengxh.autodingding.service.BackgroundService;

/**
 * @author: Pengxh
 * @email: 290677893@qq.com
 * @description: TODO
 * @date: 2020/1/19 16:00
 */
public class AlarmReceiver extends BroadcastReceiver {

    @Override
    public void onReceive(Context context, Intent intent) {
        context.startService(new Intent(context, BackgroundService.class));
    }
}
