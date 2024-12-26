package com.pengxh.daily.app.utils

import com.pengxh.daily.app.model.EmailConfigModel
import com.pengxh.kt.lite.utils.SaveKeyValues

object EmailConfigKit {
    fun getConfig(): EmailConfigModel {
        val emailAddress = SaveKeyValues.getValue(Constant.EMAIL_SEND_ADDRESS_KEY, "") as String
        val emailCode = SaveKeyValues.getValue(Constant.EMAIL_SEND_CODE_KEY, "") as String
        val emailServer = SaveKeyValues.getValue(Constant.EMAIL_SEND_SERVER_KEY, "") as String
        val emailPort = SaveKeyValues.getValue(Constant.EMAIL_SEND_PORT_KEY, "") as String
        val emailInBox = SaveKeyValues.getValue(Constant.EMAIL_IN_BOX_KEY, "") as String
        val emailTitle = SaveKeyValues.getValue(Constant.EMAIL_TITLE_KEY, "打卡结果通知") as String
        return EmailConfigModel(
            emailAddress, emailCode, emailServer, emailPort, emailInBox, emailTitle
        )
    }

    fun isEmailConfigured(): Boolean {
        val config = getConfig()
        return config.emailSender.isNotEmpty() && config.permissionCode.isNotEmpty()
                && config.senderServer.isNotEmpty() && config.emailPort.isNotEmpty()
                && config.inboxEmail.isNotEmpty()
    }
}