package com.pengxh.autodingding.extensions

import android.content.Context
import com.pengxh.autodingding.BuildConfig
import com.pengxh.autodingding.utils.EmailAuthenticator
import com.pengxh.autodingding.utils.EmailConfigKit
import com.pengxh.kt.lite.extensions.show
import com.pengxh.kt.lite.extensions.timestampToDate
import java.util.Date
import java.util.Properties
import javax.mail.Message
import javax.mail.MessagingException
import javax.mail.Session
import javax.mail.Transport
import javax.mail.internet.InternetAddress
import javax.mail.internet.MimeMessage

fun String.sendEmail(context: Context, title: String?) {
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
    try {
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
            Transport.send(mime)
        }.start()
    } catch (ex: MessagingException) {
        ex.printStackTrace()
    }
}