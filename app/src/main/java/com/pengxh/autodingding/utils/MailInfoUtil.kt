package com.pengxh.autodingding.utils

import com.pengxh.autodingding.bean.MailInfo
import com.pengxh.kt.lite.extensions.timestampToDate
import java.io.File

/**
 * @author: Pengxh
 * @email: 290677893@qq.com
 * @date: 2020/1/16 15:39
 */
object MailInfoUtil {

    fun createMail(toAddress: String?, emailMessage: String): MailInfo {
        val mailInfo = MailInfo()
        mailInfo.mailServerHost = "smtp.qq.com" //发送方邮箱服务器
        mailInfo.mailServerPort = "587" //发送方邮箱端口号
        mailInfo.isValidate = true
        mailInfo.userName = "290677893@qq.com" // 发送者邮箱地址
        mailInfo.password = "xaabdfhhvqrfcajj" //邮箱授权码，不是密码
        mailInfo.toAddress = toAddress // 接收者邮箱
        mailInfo.fromAddress = "290677893@qq.com" // 发送者邮箱
        mailInfo.subject = "自动打卡通知" // 邮件主题
        if (emailMessage == "") {
            mailInfo.content =
                "未监听到打卡成功的通知，请手动登录检查" + System.currentTimeMillis().timestampToDate() // 邮件文本
        } else {
            mailInfo.content = emailMessage // 邮件文本
        }
        return mailInfo
    }

    fun createAttachMail(toAddress: String?, file: File?): MailInfo {
        val mailInfo = MailInfo()
        mailInfo.mailServerHost = "smtp.qq.com" //发送方邮箱服务器
        mailInfo.mailServerPort = "587" //发送方邮箱端口号
        mailInfo.isValidate = true
        mailInfo.userName = "290677893@qq.com" // 发送者邮箱地址
        mailInfo.password = "xaabdfhhvqrfcajj" //邮箱授权码，不是密码
        mailInfo.toAddress = toAddress // 接收者邮箱
        mailInfo.fromAddress = "290677893@qq.com" // 发送者邮箱
        mailInfo.subject = "打卡记录" // 邮件主题
        mailInfo.attachFile = file
        return mailInfo
    }
}