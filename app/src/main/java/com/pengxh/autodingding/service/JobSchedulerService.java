package com.pengxh.autodingding.service;

import android.app.job.JobParameters;
import android.app.job.JobService;
import android.util.Log;

import com.pengxh.androidx.lite.utils.TimeOrDateUtil;
import com.pengxh.autodingding.BaseApplication;
import com.pengxh.autodingding.bean.DingTaskLogBean;
import com.pengxh.autodingding.greendao.DingTaskLogBeanDao;

public class JobSchedulerService extends JobService {

    private static final String TAG = "JobSchedulerService";
    private DingTaskLogBeanDao dingTaskLogBeanDao;

    @Override
    public void onCreate() {
        super.onCreate();
        Log.d(TAG, "onCreate ===> " + TimeOrDateUtil.timestampToCompleteDate(System.currentTimeMillis()));
        dingTaskLogBeanDao = BaseApplication.getDaoSession().getDingTaskLogBeanDao();
    }

    @Override
    public boolean onStartJob(JobParameters params) {
        Log.d(TAG, "onStartJob ===> ");
        DingTaskLogBean bean = new DingTaskLogBean();
        bean.setTitle("onStartCommand ===> 打卡");
        bean.setTime(TimeOrDateUtil.timestampToCompleteDate(System.currentTimeMillis()));
        dingTaskLogBeanDao.save(bean);
        return true;
    }

    @Override
    public boolean onStopJob(JobParameters params) {
        Log.d(TAG, "onStopJob ===> " + TimeOrDateUtil.timestampToCompleteDate(System.currentTimeMillis()));
        return false;
    }
}
