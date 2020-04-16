package com.pengxh.autodingding.utils;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.util.Log;

/**
 * @description: TODO
 * @author: Pengxh
 * @email: 290677893@qq.com
 * @date: 2020/4/16 20:08
  */
public class TimeChangeReceiver extends BroadcastReceiver {
    @Override
    public void onReceive(Context context, Intent intent) {
        String action = intent.getAction();
        if (action != null && action.equals(Intent.ACTION_TIME_TICK)) {
            //1min接收到消息一次
            Log.d("TimeChangeReceiver", "onReceive: ");
        }
    }
}
