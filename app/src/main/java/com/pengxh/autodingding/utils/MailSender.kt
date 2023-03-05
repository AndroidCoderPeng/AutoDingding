package com.pengxh.autodingding.utils

import android.util.Log
import com.pengxh.autodingding.bean.MailInfo
import java.util.*
import javax.activation.DataHandler
import javax.activation.DataSource
import javax.activation.FileDataSource
import javax.mail.*
import javax.mail.internet.*

/**
 * @author: Pengxh
 * @email: 290677893@qq.com
 * @date: 2020/1/16 15:41
 */
object MailSender {

    /**
     * 以文本格式发送邮件
     *
     * @param mailInfo 待发送的邮件的信息
     */
    fun sendTextMail(mailInfo: MailInfo) {
        // 判断是否需要身份认证
        var authenticator: EmailAuthenticator? = null
        val pro = mailInfo.properties
        if (mailInfo.isValidate) {
            // 如果需要身份认证，则创建一个密码验证器
            authenticator = EmailAuthenticator(mailInfo.userName, mailInfo.password)
        }
        // 根据邮件会话属性和密码验证器构造一个发送邮件的session
        val sendMailSession = Session.getDefaultInstance(pro, authenticator)
        try {
            // 根据session创建一个邮件消息
            val mailMessage: Message = MimeMessage(sendMailSession)
            // 创建邮件发送者地址
            val from: Address = InternetAddress(mailInfo.fromAddress)
            // 设置邮件消息的发送者
            mailMessage.setFrom(from)
            // 创建邮件的接收者地址，并设置到邮件消息中
            val to: Address = InternetAddress(mailInfo.toAddress)
            mailMessage.setRecipient(Message.RecipientType.TO, to)
            // 设置邮件消息的主题
            val mailSubject = mailInfo.subject
            mailMessage.subject = mailSubject
            // 设置邮件消息发送的时间
            mailMessage.sentDate = Date()
            // 设置邮件消息的主要内容
            val mailContent = mailInfo.content
            mailMessage.setText(mailContent)
            // 发送邮件
            Transport.send(mailMessage)
        } catch (ex: MessagingException) {
            ex.printStackTrace()
        }
    }

    // 发送带附件的邮件
    fun sendAccessoryMail(mailInfo: MailInfo) {
        Log.d("MailSender", "sendAccessoryMail: 发送带附件的邮件")
        // 判断是否需要身份验证
        var authenticator: EmailAuthenticator? = null
        val p = mailInfo.properties
        // 如果需要身份验证，则创建一个密码验证器
        if (mailInfo.isValidate) {
            authenticator = EmailAuthenticator(mailInfo.userName, mailInfo.password)
        }
        // 根据邮件会话属性和密码验证器构造一个发送邮件的session
        val sendMailSession = Session.getDefaultInstance(p, authenticator)
        try {
            // 根据session创建一个邮件消息
            val mailMessage: Message = MimeMessage(sendMailSession)
            // 创建邮件发送者的地址
            val fromAddress: Address = InternetAddress(mailInfo.fromAddress)
            // 设置邮件消息的发送者
            mailMessage.setFrom(fromAddress)
            // 创建邮件接收者的地址
            val toAddress: Address = InternetAddress(mailInfo.toAddress)
            // 设置邮件消息的接收者
            mailMessage.setRecipient(Message.RecipientType.TO, toAddress)
            // 设置邮件消息的主题
            mailMessage.subject = mailInfo.subject
            // 设置邮件消息的发送时间
            mailMessage.sentDate = Date()
            // MimeMultipart类是一个容器类，包含MimeBodyPart类型的对象
            val mainPart: Multipart = MimeMultipart()
            val file = mailInfo.attachFile
            if (!file.exists()) {
                return
            }
            // 创建一个MimeBodyPart来包含附件
            val bodyPart: BodyPart = MimeBodyPart()
            val source: DataSource = FileDataSource(file)
            bodyPart.dataHandler = DataHandler(source)
            bodyPart.fileName = MimeUtility.encodeWord(file.name)
            mainPart.addBodyPart(bodyPart)
            // 将MimeMultipart对象设置为邮件内容
            mailMessage.setContent(mainPart)
            // 发送邮件
            Transport.send(mailMessage)
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }
}