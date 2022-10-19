package com.pengxh.autodingding.service;

import android.content.Context;

import androidx.annotation.NonNull;
import androidx.work.Worker;
import androidx.work.WorkerParameters;

import com.pengxh.androidx.lite.utils.TimeOrDateUtil;
import com.pengxh.autodingding.BaseApplication;
import com.pengxh.autodingding.bean.DingTaskLogBean;
import com.pengxh.autodingding.greendao.DingTaskLogBeanDao;

public class JobSchedulerWorker extends Worker {

    private static final String TAG = "JobSchedulerWorker";
    private final DingTaskLogBeanDao dingTaskLogBeanDao;

    public JobSchedulerWorker(@NonNull Context context, @NonNull WorkerParameters workerParams) {
        super(context, workerParams);
        dingTaskLogBeanDao = BaseApplication.getInstance().getDaoSession().getDingTaskLogBeanDao();
    }

    @NonNull
    @Override
    public Result doWork() {
        DingTaskLogBean bean = new DingTaskLogBean();
        bean.setTitle("doWork ===> 打卡");
        bean.setTime(TimeOrDateUtil.timestampToCompleteDate(System.currentTimeMillis()));
        dingTaskLogBeanDao.save(bean);
        return Result.success();
    }
}
