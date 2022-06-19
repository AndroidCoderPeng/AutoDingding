package com.pengxh.autodingding.service;

import android.app.Service;
import android.content.Intent;
import android.os.IBinder;
import android.util.Log;

import androidx.annotation.Nullable;

import com.pengxh.androidx.lite.utils.TimeOrDateUtil;
import com.pengxh.autodingding.BaseApplication;
import com.pengxh.autodingding.bean.DingTaskLogBean;
import com.pengxh.autodingding.greendao.DingTaskLogBeanDao;

public class DingTaskService extends Service {

    private static final String TAG = "DingTaskService";
    private DingTaskLogBeanDao dingTaskLogBeanDao;

    @Override
    public void onCreate() {
        super.onCreate();
        dingTaskLogBeanDao = BaseApplication.getDaoSession().getDingTaskLogBeanDao();
        Log.d(TAG, "onCreate ===> DingTaskService");
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        Log.d(TAG, "onStartCommand ===> " + TimeOrDateUtil.timestampToCompleteDate(System.currentTimeMillis()));
        DingTaskLogBean bean = new DingTaskLogBean();
        bean.setTitle("onStartCommand ===> 打卡");
        bean.setTime(TimeOrDateUtil.timestampToCompleteDate(System.currentTimeMillis()));
        dingTaskLogBeanDao.save(bean);
        return super.onStartCommand(intent, flags, startId);
    }

    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }
}
