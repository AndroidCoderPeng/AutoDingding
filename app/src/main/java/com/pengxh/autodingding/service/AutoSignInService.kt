package com.pengxh.autodingding.service

import android.accessibilityservice.AccessibilityService
import android.util.Log
import android.view.accessibility.AccessibilityEvent
import android.view.accessibility.AccessibilityNodeInfo
import com.pengxh.autodingding.utils.Constant


/**
 * 钉钉打卡辅助服务，模拟手指点击屏幕，绕过作弊检测
 * */
class AutoSignInService : AccessibilityService() {

    private val kTag = "AutoSignInService"

    companion object {
        var isServiceRunning = false
    }

    override fun onServiceConnected() {
        super.onServiceConnected()
        Log.d(kTag, "onServiceConnected: 辅助服务已启动")
        isServiceRunning = true
    }

    /**
     * 响应各种事件
     * */
    override fun onAccessibilityEvent(event: AccessibilityEvent?) {
        event?.apply {
            if (!packageName.isNullOrBlank() && packageName == Constant.DING_DING) {
                //捕获窗口内容改变事件
                if (eventType == AccessibilityEvent.TYPE_WINDOW_CONTENT_CHANGED) {
                    //获取顶部[打卡]按钮节点树
                    val topFunctionNodeTree = source?.findAccessibilityNodeInfosByViewId(
                        "com.alibaba.android.rimet:id/im_ding_kit_item_txt"
                    ) ?: return

                    if (topFunctionNodeTree.size < 4) {
                        return
                    }

                    val targetNode = topFunctionNodeTree[3]
                    if (targetNode.text.toString() == "打卡") {
                        targetNode.parent?.performAction(AccessibilityNodeInfo.ACTION_CLICK)
                    }
                }
            }
        }
    }

    override fun onInterrupt() {
        Log.d(kTag, "onInterrupt: 辅助服务已断开")
        isServiceRunning = false
    }
}