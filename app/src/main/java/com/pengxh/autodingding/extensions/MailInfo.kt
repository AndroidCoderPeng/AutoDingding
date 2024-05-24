package com.pengxh.autodingding.extensions

import com.pengxh.autodingding.BuildConfig
import com.pengxh.autodingding.bean.MailInfo
import com.pengxh.autodingding.utils.EmailAuthenticator
import java.util.Date
import javax.mail.Address
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
        // 根据session创建一个邮件消息
        val mailMessage = MimeMessage(sendMailSession)
        // 创建邮件发送者地址
        val from = InternetAddress(this.fromAddress)
        // 设置邮件消息的发送者
        mailMessage.setFrom(from)
        // 创建邮件的接收者地址，并设置到邮件消息中
        val to: Address = InternetAddress(this.toAddress)
        mailMessage.setRecipient(Message.RecipientType.TO, to)
        // 设置邮件消息的主题
        val mailSubject = this.subject
        mailMessage.subject = mailSubject
        mailMessage.sentDate = Date()
        // 设置邮件消息的主要内容
        val mailContent = "${this.content}，工具本版：${BuildConfig.VERSION_NAME}"
        mailMessage.setText(mailContent)
        Transport.send(mailMessage)
    } catch (ex: MessagingException) {
        ex.printStackTrace()
    }
}