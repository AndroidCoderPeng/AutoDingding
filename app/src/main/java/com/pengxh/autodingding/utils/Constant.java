package com.pengxh.autodingding.utils;

import com.pengxh.autodingding.R;

/**
 * @description: TODO
 * @author: Pengxh
 * @email: 290677893@qq.com
 * @date: 2019/12/29 12:42
 */
public class Constant {
    //钉钉包名：com.alibaba.android.rimet
    //打卡页面类名：com.alibaba.lightapp.runtime.activity.CommonWebViewActivity
    public static final String DINGDING = "com.alibaba.android.rimet";
    public static final String DINGDING_ACTION = "action.sendNotification";

    public static final int[] images = {R.drawable.settings, R.drawable.about};

    /**
     * type
     * 0-工作日
     * 1-周末
     * 2-节假日
     */
    public static final String BASE_URL = "http://timor.tech/api/holiday/info/";

    public static final long ONE_MONTH = 31 * 24 * 60 * 60 * 1000L;
}
