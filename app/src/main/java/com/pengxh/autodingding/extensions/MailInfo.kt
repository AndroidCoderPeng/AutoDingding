package com.pengxh.autodingding.extensions

import android.util.Log
import com.pengxh.autodingding.BuildConfig
import com.pengxh.autodingding.bean.MailInfo
import com.pengxh.autodingding.utils.EmailAuthenticator
import java.util.Date
import javax.activation.DataHandler
import javax.activation.FileDataSource
import javax.mail.Address
import javax.mail.Message
import javax.mail.MessagingException
import javax.mail.Session
import javax.mail.Transport
import javax.mail.internet.InternetAddress
import javax.mail.internet.MimeBodyPart
import javax.mail.internet.MimeMessage
import javax.mail.internet.MimeMultipart
import javax.mail.internet.MimeUtility

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
        val to: Address = InternetAddress(this.toAddress)
        mailMessage.setRecipient(Message.RecipientType.TO, to)
        val mailSubject = this.subject
        mailMessage.subject = mailSubject
        mailMessage.sentDate = Date()
        val mailContent = "${this.content}，工具本版：${BuildConfig.VERSION_NAME}"
        mailMessage.setText(mailContent)
        Transport.send(mailMessage)
        Log.d("kTag", "sendTextMail: 成功")
    } catch (ex: MessagingException) {
        ex.printStackTrace()
    }
}

fun MailInfo.sendAttachFileMail() {
    var authenticator: EmailAuthenticator? = null
    val p = this.properties
    if (this.isValidate) {
        authenticator = EmailAuthenticator(this.userName, this.password)
    }
    val sendMailSession = Session.getDefaultInstance(p, authenticator)
    try {
        val mailMessage = MimeMessage(sendMailSession)
        val fromAddress = InternetAddress(this.fromAddress)
        mailMessage.setFrom(fromAddress)
        val toAddress = InternetAddress(this.toAddress)
        mailMessage.setRecipient(Message.RecipientType.TO, toAddress)
        mailMessage.subject = this.subject
        mailMessage.sentDate = Date()
        val mainPart = MimeMultipart()
        val file = this.attachFile
        if (!file.exists()) {
            return
        }
        val bodyPart = MimeBodyPart()
        val source = FileDataSource(file)
        bodyPart.dataHandler = DataHandler(source)
        bodyPart.fileName = MimeUtility.encodeWord(file.name)
        mainPart.addBodyPart(bodyPart)
        mailMessage.setContent(mainPart)
        Transport.send(mailMessage)
    } catch (e: Exception) {
        e.printStackTrace()
    }
}