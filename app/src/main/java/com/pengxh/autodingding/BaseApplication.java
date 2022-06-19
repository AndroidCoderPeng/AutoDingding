package com.pengxh.autodingding;

import android.app.Application;
import android.database.sqlite.SQLiteDatabase;

import com.pengxh.androidx.lite.utils.SaveKeyValues;
import com.pengxh.autodingding.greendao.DaoMaster;
import com.pengxh.autodingding.greendao.DaoSession;
import com.pengxh.autodingding.utils.Utils;
import com.tencent.bugly.crashreport.CrashReport;

/**
 * @author: Pengxh
 * @email: 290677893@qq.com
 * @date: 2019/12/25 13:19
 */
public class BaseApplication extends Application {

    private static DaoSession daoSession;
    private volatile static BaseApplication application;

    @Override
    public void onCreate() {
        super.onCreate();
        application = this;
        Utils.init(this);
        SaveKeyValues.initSharedPreferences(this);
        CrashReport.initCrashReport(this, "ce38195468", false);
        initDataBase();
    }

    /**
     * 双重锁单例
     */
    public static BaseApplication getInstance() {
        if (application == null) {
            synchronized (BaseApplication.class) {
                if (application == null) {
                    application = new BaseApplication();
                }
            }
        }
        return application;
    }

    private void initDataBase() {
        DaoMaster.DevOpenHelper helper = new DaoMaster.DevOpenHelper(this, "DingRecord.db");
        SQLiteDatabase db = helper.getWritableDatabase();
        DaoMaster daoMaster = new DaoMaster(db);
        daoSession = daoMaster.newSession();
    }

    public static DaoSession getDaoSession() {
        return daoSession;
    }
}
