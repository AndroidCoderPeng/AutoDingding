package com.pengxh.autodingding.utils;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.util.Log;

public class SQLiteUtilHelper extends SQLiteOpenHelper {
    private static final String TAG = "SQLiteUtilHelper";

    SQLiteUtilHelper(Context context, String name, SQLiteDatabase.CursorFactory factory, int version) {
        super(context, name, factory, version);
    }

    @Override
    public void onCreate(SQLiteDatabase db) {
        db.execSQL("create table TimeTable(id integer primary key autoincrement,uuid text, startTime text, endTime text,isStart text)");
        Log.d(TAG, "数据库创建成功");
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {

    }
}
