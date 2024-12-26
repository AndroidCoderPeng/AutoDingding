package com.pengxh.daily.app.extensions

import android.app.Activity
import android.view.ViewGroup
import androidx.annotation.ColorRes
import com.gyf.immersionbar.ImmersionBar
import com.pengxh.kt.lite.extensions.convertColor
import com.pengxh.kt.lite.extensions.getStatusBarHeight

fun ViewGroup.initImmersionBar(activity: Activity, isDarkFont: Boolean, @ColorRes color: Int) {
    ImmersionBar.with(activity)
        .statusBarDarkFont(isDarkFont)
        .statusBarColorInt(color.convertColor(activity))
        .init()
    //根据不同设备状态栏高度设置statusBarView高度
    val statusBarHeight = activity.getStatusBarHeight()
    this.setPadding(0, statusBarHeight, 0, 0)
    this.requestLayout()
}