package com.pengxh.autodingding.extensions

import com.pengxh.autodingding.bean.MailInfo
import com.pengxh.autodingding.utils.EmailAuthenticator
import java.util.*
import javax.activation.DataHandler
import javax.activation.FileDataSource
import javax.mail.*
import javax.mail.internet.*

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
        val mailContent = this.content
        mailMessage.setText(mailContent)
        Transport.send(mailMessage)
    } catch (ex: MessagingException) {
        ex.printStackTrace()
    }
}

// 发送带附件的邮件
fun MailInfo.sendAccessoryMail() {
    // 判断是否需要身份验证
    var authenticator: EmailAuthenticator? = null
    val p = this.properties
    // 如果需要身份验证，则创建一个密码验证器
    if (this.isValidate) {
        authenticator = EmailAuthenticator(this.userName, this.password)
    }
    // 根据邮件会话属性和密码验证器构造一个发送邮件的session
    val sendMailSession = Session.getDefaultInstance(p, authenticator)
    try {
        // 根据session创建一个邮件消息
        val mailMessage = MimeMessage(sendMailSession)
        // 创建邮件发送者的地址
        val fromAddress = InternetAddress(this.fromAddress)
        // 设置邮件消息的发送者
        mailMessage.setFrom(fromAddress)
        // 创建邮件接收者的地址
        val toAddress = InternetAddress(this.toAddress)
        // 设置邮件消息的接收者
        mailMessage.setRecipient(Message.RecipientType.TO, toAddress)
        // 设置邮件消息的主题
        mailMessage.subject = this.subject
        mailMessage.sentDate = Date()
        // MimeMultipart类是一个容器类，包含MimeBodyPart类型的对象
        val mainPart = MimeMultipart()
        val file = this.attachFile
        if (!file.exists()) {
            return
        }
        // 创建一个MimeBodyPart来包含附件
        val bodyPart = MimeBodyPart()
        val source = FileDataSource(file)
        bodyPart.dataHandler = DataHandler(source)
        bodyPart.fileName = MimeUtility.encodeWord(file.name)
        mainPart.addBodyPart(bodyPart)
        // 将MimeMultipart对象设置为邮件内容
        mailMessage.setContent(mainPart)
        Transport.send(mailMessage)
    } catch (e: Exception) {
        e.printStackTrace()
    }
}