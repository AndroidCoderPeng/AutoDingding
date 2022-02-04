package com.pengxh.autodingding.utils;

import android.Manifest;

import com.pengxh.autodingding.R;

/**
 * @description: TODO
 * @author: Pengxh
 * @email: 290677893@qq.com
 * @date: 2019/12/29 12:42
 */
public class Constant {
    public static final int PERMISSIONS_CODE = 999;
    public static final String[] USER_PERMISSIONS = {Manifest.permission.WRITE_EXTERNAL_STORAGE,
            Manifest.permission.READ_EXTERNAL_STORAGE};

    //钉钉包名：com.alibaba.android.rimet
    //打卡页面类名：com.alibaba.lightapp.runtime.activity.CommonWebViewActivity
    public static final String DINGDING = "com.alibaba.android.rimet";
    public static final String DINGDING_ACTION = "action.sendNotification";

    public static final int[] images = {R.mipmap.delete, R.mipmap.output};

    public static final long ONE_WEEK = 5 * 24 * 60 * 60 * 1000L;

    public static final String ACTION_UPDATE = "action_updateRecord";
}
