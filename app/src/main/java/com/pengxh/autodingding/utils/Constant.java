package com.pengxh.autodingding.utils;

import android.Manifest;

/**
 * @author: Pengxh
 * @email: 290677893@qq.com
 * @date: 2019/12/29 12:42
 */
public class Constant {
    public static final int PERMISSIONS_CODE = 999;
    public static final String[] USER_PERMISSIONS = {Manifest.permission.READ_EXTERNAL_STORAGE};

    public static final String EMAIL_ADDRESS = "emailAddress";

    //钉钉包名：com.alibaba.android.rimet
    //打卡页面类名：com.alibaba.lightapp.runtime.activity.CommonWebViewActivity
    public static final String DINGDING = "com.alibaba.android.rimet";

    public static final long ONE_WEEK = 5 * 24 * 60 * 60 * 1000L;
}
