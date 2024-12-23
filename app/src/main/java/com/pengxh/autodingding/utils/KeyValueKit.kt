package com.pengxh.autodingding.utils

import com.pengxh.kt.lite.utils.SaveKeyValues

object KeyValueKit {
    fun getEmailAddress(): String {
        return SaveKeyValues.getValue(Constant.EMAIL_ADDRESS, "") as String
    }
}