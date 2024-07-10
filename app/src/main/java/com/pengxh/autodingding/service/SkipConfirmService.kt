package com.pengxh.autodingding.service

import android.accessibilityservice.AccessibilityService
import android.content.Intent
import android.util.Log
import android.view.accessibility.AccessibilityEvent
import android.view.accessibility.AccessibilityNodeInfo

/**
 * 辅助服务，跳过授权确认框，自动截屏
 * */
class SkipConfirmService : AccessibilityService() {

    private val kTag = "SkipConfirmService"

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
        if (event == null) {
            return
        }
        if (event.packageName.isNullOrBlank()) {
            return
        }
        if (event.eventType == AccessibilityEvent.TYPE_WINDOW_STATE_CHANGED) {
            val viewNodes =
                event.source?.findAccessibilityNodeInfosByViewId("android:id/button1") ?: return
            if (viewNodes.isNotEmpty()) {
                //找到一个节点即可
                val viewNode = viewNodes.first()
                if (viewNode.text == "立即开始") {
                    viewNode.performAction(AccessibilityNodeInfo.ACTION_CLICK)
                }
            }
        }
    }

    override fun onInterrupt() {

    }

    override fun onUnbind(intent: Intent?): Boolean {
        Log.d(kTag, "onUnbind: 辅助服务已断开")
        isServiceRunning = false
        return super.onUnbind(intent)
    }
}