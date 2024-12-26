package com.pengxh.daily.app.extensions

import android.content.Context
import com.pengxh.daily.app.BuildConfig
import com.pengxh.daily.app.ui.EmailConfigActivity
import com.pengxh.daily.app.utils.Constant
import com.pengxh.daily.app.utils.EmailAuthenticator
import com.pengxh.daily.app.utils.EmailConfigKit
import com.pengxh.kt.lite.extensions.show
import com.pengxh.kt.lite.extensions.timestampToDate
import java.util.Date
import java.util.Properties
import javax.mail.Message
import javax.mail.Session
import javax.mail.Transport
import javax.mail.internet.InternetAddress
import javax.mail.internet.MimeMessage

fun String.sendEmail(context: Context, title: String?, isTest: Boolean) {
    val config = EmailConfigKit.getConfig()

    if (config.inboxEmail.isEmpty()) {
        "邮箱地址为空".show(context)
        return
    }

    /*****************************************************************************************/
    /*********************************发送邮件*************************************************/
    /*****************************************************************************************/
    val authenticator = EmailAuthenticator(config.emailSender, config.permissionCode)
    val pro = Properties()
    pro["mail.smtp.host"] = config.senderServer
    pro["mail.smtp.port"] = config.emailPort
    pro["mail.smtp.auth"] = true
    pro["mail.smtp.starttls.enable"] = true
    pro["mail.smtp.starttls.required"] = true
    val sendMailSession = Session.getDefaultInstance(pro, authenticator)
    val mime = MimeMessage(sendMailSession)
    mime.setFrom(InternetAddress(config.emailSender))
    mime.setRecipient(Message.RecipientType.TO, InternetAddress(config.inboxEmail))
    if (title == null) {
        mime.subject = config.emailTitle
    } else {
        mime.subject = title
    }
    mime.sentDate = Date()
    val mailContent = if (this == "") {
        "未监听到打卡成功的通知，请手动登录检查" + System.currentTimeMillis().timestampToDate()
    } else {
        "${this}，版本号：${BuildConfig.VERSION_NAME}"
    }
    mime.setText(mailContent)
    Thread {
        try {
            Transport.send(mime)
            if (isTest) {
                EmailConfigActivity.weakReferenceHandler.sendEmptyMessage(Constant.SEND_EMAIL_SUCCESS_CODE)
            }
        } catch (e: Exception) {
            e.printStackTrace()
            if (isTest) {
                EmailConfigActivity.weakReferenceHandler.sendEmptyMessage(Constant.SEND_EMAIL_FAILED_CODE)
            }
        }
    }.start()
}