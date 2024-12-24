package com.pengxh.autodingding.extensions

import com.pengxh.autodingding.BuildConfig
import com.pengxh.autodingding.bean.MailInfo
import com.pengxh.autodingding.utils.EmailAuthenticator
import java.util.Date
import javax.mail.Message
import javax.mail.MessagingException
import javax.mail.Session
import javax.mail.Transport
import javax.mail.internet.InternetAddress
import javax.mail.internet.MimeMessage

/**
 * 以文本格式发送邮件
 */
fun MailInfo.sendTextMail() {
    // 判断是否需要身份认证
    var authenticator: EmailAuthenticator? = null
    val pro = this.properties
    if (this.isValidate) {
        // 如果需要身份认证，则创建一个密码验证器
        authenticator = EmailAuthenticator(this.userName, this.password)
    }
    // 根据邮件会话属性和密码验证器构造一个发送邮件的session
    val sendMailSession = Session.getDefaultInstance(pro, authenticator)
    try {
        val mailMessage = MimeMessage(sendMailSession)
        val from = InternetAddress(this.fromAddress)
        mailMessage.setFrom(from)
        val to = InternetAddress(this.toAddress)
        mailMessage.setRecipient(Message.RecipientType.TO, to)
        val mailSubject = this.subject
        mailMessage.subject = mailSubject
        mailMessage.sentDate = Date()
        val mailContent = "${this.content}，版本号：${BuildConfig.VERSION_NAME}"
        mailMessage.setText(mailContent)
        Thread {
            Transport.send(mailMessage)
        }.start()
    } catch (ex: MessagingException) {
        ex.printStackTrace()
    }
}