package com.pengxh.autodingding.utils

import android.Manifest
import android.os.Build

/**
 * @author: Pengxh
 * @email: 290677893@qq.com
 * @date: 2019/12/29 12:42
 */
object Constant {
    val USER_PERMISSIONS = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
        arrayOf(
            Manifest.permission.QUERY_ALL_PACKAGES,
            Manifest.permission.SYSTEM_ALERT_WINDOW
        )
    } else {
        arrayOf(
            Manifest.permission.SYSTEM_ALERT_WINDOW
        )
    }

    const val PERMISSIONS_CODE = 999
    const val EMAIL_ADDRESS = "emailAddress"

    const val DING_DING = "com.alibaba.android.rimet"
    const val WECHAT = "com.tencent.wework"
    const val QQ = "com.tencent.mobileqq"
}