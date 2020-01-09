package com.pengxh.autodingding.db;

import android.annotation.SuppressLint;
import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.util.Log;

import com.pengxh.autodingding.bean.ClockBean;

import java.util.ArrayList;
import java.util.List;

public class SQLiteUtil {
    private static final String TAG = "SQLiteUtil";
    @SuppressLint({"StaticFieldLeak"})
    private static Context context;
    /**
     * 数据库名
     */
    private static final String DB_NAME = "ClockData.db";
    /**
     * 数据库版本
     */
    private static final int VERSION = 2;
    private SQLiteDatabase sqlLiteDatabase;
    @SuppressLint("StaticFieldLeak")
    private static SQLiteUtil sqLiteUtil = null;

    public static void initDataBase(Context mContext) {
        context = mContext.getApplicationContext();
    }

    private SQLiteUtil() {
        SQLiteUtilHelper mSqliteUtilHelper = new SQLiteUtilHelper(context, DB_NAME, null, VERSION);
        sqlLiteDatabase = mSqliteUtilHelper.getWritableDatabase();
    }

    public static SQLiteUtil getInstance() {
        if (null == sqLiteUtil) {
            synchronized (SQLiteUtil.class) {
                if (null == sqLiteUtil) {
                    sqLiteUtil = new SQLiteUtil();
                }
            }
        }
        return sqLiteUtil;
    }

    /**
     * 保存报警信息
     */
    public void saveClock(ClockBean clockBean) {
        if (clockBean != null) {
            ContentValues values = new ContentValues();
            String uuid = clockBean.getUuid();
            String clockTime = clockBean.getClockTime();
            int clockStatus = clockBean.getClockStatus();
            Log.d(TAG, "准备插入数据库：" + uuid + "," + clockTime + "," + clockStatus);

            values.put("uuid", uuid);
            values.put("clockTime", clockTime);
            values.put("clockStatus", clockStatus);
            sqlLiteDatabase.insert("ClockTable", null, values);
            Log.d(TAG, clockTime + "插入数据库");
        }
    }

    public void deleteClockByUUid(String id) {
        sqlLiteDatabase.delete("ClockTable", "uuid = ?", new String[]{id});
        Log.d(TAG, "删除闹钟" + id);
    }

    /**
     * 加载所有报警信息
     */
    public List<ClockBean> loadAllClock() {
        List<ClockBean> list = new ArrayList<>();
        Cursor cursor = sqlLiteDatabase
                .query("ClockTable", null, null, null, null, null, null);
        if (cursor.moveToFirst()) {
            do {
                ClockBean clockBean = new ClockBean();
                clockBean.setUuid(cursor.getString(cursor.getColumnIndex("uuid")));
                clockBean.setClockTime(cursor.getString(cursor.getColumnIndex("clockTime")));
                clockBean.setClockStatus(cursor.getInt(cursor.getColumnIndex("clockStatus")));
                list.add(clockBean);
            } while (cursor.moveToNext());
        }
        return list;
    }

    public void updateClockStatus(String uuid, int status) {
        if (uuid != null) {
            ContentValues values = new ContentValues();
            values.put("clockStatus", status);

            sqlLiteDatabase.update("ClockTable", values, "uuid = ?", new String[]{uuid});
            Log.d(TAG, uuid + "更新状态");
        }
    }

    public void updateClockTime(String uuid, String time) {
        if (uuid != null) {
            ContentValues values = new ContentValues();
            values.put("clockTime", time);

            sqlLiteDatabase.update("ClockTable", values, "uuid = ?", new String[]{uuid});
            Log.d(TAG, uuid + "更新状态");
        }
    }
}