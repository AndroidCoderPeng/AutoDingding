package com.pengxh.autodingding.utils;

import android.content.Context;
import android.util.Log;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

/**
 * @author: Pengxh
 * @email: 290677893@qq.com
 * @description: TODO
 * @date: 2020/4/23 15:36
 */
public class LogToFile {

    private static final String TAG = "LogToFile";
    private static final char VERBOSE = 'v';
    private static final char DEBUG = 'd';
    private static final char INFO = 'i';
    private static final char WARN = 'w';
    private static final char ERROR = 'e';
    private static SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd_HH-mm-ss", Locale.US);//日期格式;
    private static Date date = new Date();
    private static File file;

    public static void initLog(Context mContext) {
        Context context = mContext.getApplicationContext();
        String packageName = context.getPackageName();
        //获取到的包名带有“.”方便命名，取最后一个作为文件名，例如:com.pengxh.autodingding
        String[] split = packageName.split("\\.");//先转义.之后才能分割
        int length = split.length;
        String fileName = split[length - 1] + "_logcat";
        file = new File(context.getFilesDir(), fileName);
        if (!file.exists()) {
            try {
                file.createNewFile();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
        /**
         * 打印路径：/data/user/0/com.pengxh.autodingding/files/autodingding_logcat
         * 真实路径：/data/data/com.pengxh.autodingding/files/autodingding_logcat
         * */
        Log.d(TAG, "initLog: " + file.getAbsolutePath());
    }

    public static void v(String tag, String msg) {
        writeToFile(VERBOSE, tag, msg);
    }

    public static void d(String tag, String msg) {
        writeToFile(DEBUG, tag, msg);
    }

    public static void i(String tag, String msg) {
        writeToFile(INFO, tag, msg);
    }

    public static void w(String tag, String msg) {
        writeToFile(WARN, tag, msg);
    }

    public static void e(String tag, String msg) {
        writeToFile(ERROR, tag, msg);
    }

    private static void writeToFile(char type, String tag, String msg) {
        String log = dateFormat.format(date) + " " + type + " " + tag + " " + msg + "\n";//log日志内容，可以自行定制
        //准备写入
        FileOutputStream outputStream;
        BufferedWriter bufferedWriter = null;
        try {
            outputStream = new FileOutputStream(file, true);
            bufferedWriter = new BufferedWriter(new OutputStreamWriter(outputStream));
            bufferedWriter.write(log);
        } catch (IOException e) {
            e.printStackTrace();
        } finally {
            if (bufferedWriter != null) {
                try {
                    bufferedWriter.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }
    }
}
