package com.pengxh.autodingding.utils;

/**
 * @description: TODO
 * @author: Pengxh
 * @email: 290677893@qq.com
 * @date: 2019/12/26 23:17
 */
public class BroadcastAction {
    //钉钉包名：com.alibaba.android.rimet
    //打卡页面类名：com.alibaba.lightapp.runtime.activity.CommonWebViewActivity
    public static final String DINGDING = "com.alibaba.android.rimet";

    //广播action统一监听
    public static final String[] ACTIONS = {
            "action.startWork.am", "action.endWork.pm",
            "action.update.am", "action.update.pm"};
}
