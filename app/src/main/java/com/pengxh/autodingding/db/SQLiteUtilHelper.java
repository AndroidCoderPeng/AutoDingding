package com.pengxh.autodingding.db;

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
        db.execSQL("create table ClockTable(id integer primary key autoincrement,uuid integer,clockTime text,clockStatus integer)");
        db.execSQL("create table WeekTable(id integer primary key autoincrement,week text,state integer)");
        Log.d(TAG, "数据库创建成功");
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {

    }
}
