package com.pengxh.autodingding.utils;

import android.annotation.SuppressLint;
import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.text.TextUtils;
import android.util.Log;

import com.pengxh.autodingding.bean.HistoryBean;

import java.util.ArrayList;
import java.util.List;

@SuppressLint({"StaticFieldLeak"})
public class SQLiteUtil {
    private static final String TAG = "SQLiteUtil";
    private static Context context;
    /**
     * 数据库名
     */
    private static final String DB_NAME = "History.db";
    /**
     * 数据库版本
     */
    private static final int VERSION = 1;
    private SQLiteDatabase db;
    private static SQLiteUtil sqLiteUtil = null;

    public static void initDataBase(Context mContext) {
        context = mContext.getApplicationContext();
    }

    private SQLiteUtil() {
        db = new SQLiteUtilHelper(context, DB_NAME, null, VERSION).getWritableDatabase();
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

    //存记录
    public void saveHistory(String uuid, String date, String message) {
        Log.d(TAG, "saveHistory: 开始保存数据");
        if (!TextUtils.isEmpty(uuid)) {
            ContentValues values = new ContentValues();
            if (isUuidExist(uuid)) {
                Log.d(TAG, uuid + "已经存在了");
            } else {
                values.put("uuid", uuid);
                values.put("date", date);
                values.put("message", message);
                db.insert("HistoryTable", null, values);
                Log.d(TAG, "saveHistory: 保存成功");
            }
        }
    }

    //取记录
    public List<HistoryBean> loadHistory() {
        List<HistoryBean> list = new ArrayList<>();
        Cursor cursor = db
                .query("HistoryTable", null, null, null, null, null, "id DESC");//id倒序
        if (cursor.moveToFirst()) {
            do {
                HistoryBean resultBean = new HistoryBean();
                resultBean.setUuid(cursor.getString(cursor.getColumnIndex("uuid")));
                resultBean.setDate(cursor.getString(cursor.getColumnIndex("date")));
                resultBean.setMessage(cursor.getString(cursor.getColumnIndex("message")));
                list.add(resultBean);
            } while (cursor.moveToNext());
        }
        return list;
    }

    //根据uuid查询记录
    private boolean isUuidExist(String uuid) {
        boolean result = false;
        Cursor cursor = null;
        try {
            cursor = db.query("HistoryTable", null, "uuid = ?", new String[]{uuid}, null, null, null);
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


    //删除记录，清空表格
    public void deleteAll() {
        db.delete("HistoryTable", null, null);
    }
}