package com.pengxh.autodingding.utils;

import android.annotation.SuppressLint;
import android.content.Context;
import android.database.sqlite.SQLiteDatabase;

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
}