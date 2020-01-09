package com.pengxh.autodingding.db;

import android.annotation.SuppressLint;
import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.util.Log;

import com.pengxh.autodingding.bean.ClockBean;
import com.pengxh.autodingding.bean.WorkDayBean;

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
     * 保存闹钟
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
     * 加载所有闹钟
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

    /*****************保存工作日*************************************************/
    /**
     * 保存星期
     */
    public void saveWeek(WorkDayBean workDayBean) {
        if (workDayBean != null) {
            ContentValues values = new ContentValues();
            String week = workDayBean.getWeek();
            int state = workDayBean.getState();
            Log.d(TAG, "准备插入数据库：" + week + "," + state);

            values.put("week", week);
            values.put("state", state);
            if (isWeekExist(week)) {
                Log.d(TAG, "重复数据");
                return;
            }
            sqlLiteDatabase.insert("WeekTable", null, values);
            Log.d(TAG, week + "插入数据库");
        }
    }

    /**
     * 查询
     *
     * @param week 需要查询的参数
     */
    private boolean isWeekExist(String week) {
        boolean result = false;
        Cursor cursor = null;
        try {
            cursor = sqlLiteDatabase.query("WeekTable", null, "week = ?", new String[]{week}, null, null, null);
            result = null != cursor && cursor.moveToFirst();
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            if (null != cursor && !cursor.isClosed()) {
                cursor.close();
            }
        }
        return result;
    }

    /**
     * 加载所有工作日
     */
    public List<WorkDayBean> loadAllWeekDay() {
        List<WorkDayBean> list = new ArrayList<>();
        Cursor cursor = sqlLiteDatabase
                .query("WeekTable", null, "state = ?", new String[]{String.valueOf(1)}, null, null, null);
        if (cursor.moveToFirst()) {
            do {
                WorkDayBean workDayBean = new WorkDayBean();
                workDayBean.setWeek(cursor.getString(cursor.getColumnIndex("week")));
                workDayBean.setState(cursor.getInt(cursor.getColumnIndex("state")));
                list.add(workDayBean);
            } while (cursor.moveToNext());
        }
        return list;
    }

    public void deleteWeek() {
        sqlLiteDatabase.delete("WeekTable", null, null);
        Log.d(TAG, "删除WeekTable");
    }

    public void deleteWeekByWeek(String week) {
        sqlLiteDatabase.delete("WeekTable", "week = ?", new String[]{week});
        Log.d(TAG, "删除" + week);
    }
}