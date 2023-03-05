package com.pengxh.autodingding.utils

import android.Manifest

/**
 * @author: Pengxh
 * @email: 290677893@qq.com
 * @date: 2019/12/29 12:42
 */
object Constant {
    val USER_PERMISSIONS = arrayOf(Manifest.permission.READ_EXTERNAL_STORAGE)

    const val PERMISSIONS_CODE = 999
    const val EMAIL_ADDRESS = "emailAddress"

    //钉钉包名：com.alibaba.android.rimet
    //打卡页面类名：com.alibaba.lightapp.runtime.activity.CommonWebViewActivity
    const val DINGDING = "com.alibaba.android.rimet"
    const val WECHAT = "com.tencent.wework"
    const val ONE_WEEK = 5 * 24 * 60 * 60 * 1000L
}