package com.pengxh.autodingding.utils;

import android.annotation.SuppressLint;
import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.util.Log;

import com.pengxh.autodingding.bean.TimeSetBean;

import java.util.ArrayList;
import java.util.List;

@SuppressLint({"StaticFieldLeak"})
public class SQLiteUtil {
    private static final String TAG = "SQLiteUtil";
    private static Context context;
    /**
     * 数据库名
     */
    private static final String DB_NAME = "Time.db";
    /**
     * 数据库版本
     */
    private static final int VERSION = 1;
    private SQLiteDatabase sqlLiteDatabase;
    private static SQLiteUtil sqLiteUtil = null;

    public static void initDataBase(Context mContext) {
        context = mContext.getApplicationContext();
    }

    private SQLiteUtil() {
        sqlLiteDatabase = new SQLiteUtilHelper(context, DB_NAME, null, VERSION).getWritableDatabase();
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
     * 保存时间设置
     */
    public void saveTime(TimeSetBean timeSetBean) {
        if (timeSetBean != null) {
            ContentValues values = new ContentValues();
            String uuid = timeSetBean.getUuid();
            String startTime = timeSetBean.getStartTime();
            String endTime = timeSetBean.getEndTime();
            String isStart = timeSetBean.getIsStart();
            Log.d(TAG, "准备插入数据库：" + uuid + "," + startTime + "," + endTime + "," + isStart);

            values.put("uuid", uuid);
            values.put("startTime", startTime);
            values.put("endTime", endTime);
            values.put("isStart", isStart);
            if (!isUuidExist(uuid)) {
                sqlLiteDatabase.insert("TimeTable", null, values);
                Log.d(TAG, uuid + "新数据，直接插入数据库");
            } else {
                Log.d(TAG, "重复数据，更新");
            }
        }
    }

    /**
     * 删除所有信息
     */
    public void deleteAll() {
        sqlLiteDatabase.delete("TimeTable", null, null);
        Log.d(TAG, "数据删除成功");
    }

    /**
     * 根据uuid删除记录
     */
    public void deleteByUuid(String uuid) {
        sqlLiteDatabase.delete("TimeTable", "uuid = ?", new String[]{uuid});
        Log.d(TAG, uuid + "数据删除成功");
    }

    /**
     * 加载所有时间设置
     */
    public List<TimeSetBean> loadAllTimeSet() {
        List<TimeSetBean> list = new ArrayList<>();
        Cursor cursor = sqlLiteDatabase
                .query("TimeTable", null, null, null, null, null, null);
        if (cursor.moveToFirst()) {
            do {
                TimeSetBean timeSetBean = new TimeSetBean();
                timeSetBean.setUuid(cursor.getString(cursor.getColumnIndex("uuid")));
                timeSetBean.setStartTime(cursor.getString(cursor.getColumnIndex("startTime")));
                timeSetBean.setEndTime(cursor.getString(cursor.getColumnIndex("endTime")));
                timeSetBean.setIsStart(cursor.getString(cursor.getColumnIndex("isStart")));
                list.add(timeSetBean);
            } while (cursor.moveToNext());
        }
        return list;
    }

    /**
     * 更新
     * <p>
     * 更新闹钟状态
     */
    public void updateClockStatus(String uuid, String status) {
        if (isUuidExist(uuid)) {
            ContentValues values = new ContentValues();
            values.put("isStart", status);//value为将要替换更改的值
            //uuid = ?的isStart更新
            sqlLiteDatabase.update("TimeTable", values, "uuid = ?", new String[]{uuid});
            Log.d(TAG, "闹钟状态更新成功" + status);
        } else {
            Log.d(TAG, uuid + "没有旧数据，不能更新");
        }
    }

    /**
     * 查询
     *
     * @param uuid 需要查询的参数
     */
    private boolean isUuidExist(String uuid) {
        boolean result = false;
        Cursor cursor = null;
        try {
            cursor = sqlLiteDatabase.query("TimeTable", null, "uuid = ?", new String[]{uuid}, null, null, null);
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
}