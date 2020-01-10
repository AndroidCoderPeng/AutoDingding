package com.pengxh.autodingding.utils;

import android.annotation.SuppressLint;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.os.Handler;
import android.os.Message;
import android.util.Log;

import com.pengxh.autodingding.ui.DingdingClockActivity;

/**
 * @author: Pengxh
 * @email: 290677893@qq.com
 * @description: TODO
 * @date: 2020/1/10 14:53
 */
public class AlarmReceiver extends BroadcastReceiver {

    private static final String TAG = "AlarmReceiver";
    private Context mContext;

    @Override
    public void onReceive(Context context, Intent intent) {
        this.mContext = context;
        String action = intent.getAction();
        if (action != null) {
            Log.d(TAG, "onReceive: 启动钉钉");
            Utils.openDingding(Constant.DINGDING);
            handler.sendEmptyMessageDelayed(101, 10 * 1000);
        }
    }

    @SuppressLint("HandlerLeak")
    private Handler handler = new Handler() {
        @Override
        public void handleMessage(Message msg) {
            if (msg.what == 101) {
                mContext.startActivity(new Intent(mContext, DingdingClockActivity.class));
            }
        }
    };
}