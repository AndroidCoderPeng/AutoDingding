package com.pengxh.autodingding.utils

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import com.pengxh.autodingding.fragment.AutoDingDingFragment

class DateChangeReceiver : BroadcastReceiver() {
    override fun onReceive(context: Context?, intent: Intent?) {
        val action = intent?.action ?: return
        if (action == Intent.ACTION_DATE_CHANGED) {
            AutoDingDingFragment.weakReferenceHandler.sendEmptyMessage(2024070801)
        }
    }
}