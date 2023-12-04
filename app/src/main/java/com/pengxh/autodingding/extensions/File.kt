package com.pengxh.autodingding.extensions

import com.pengxh.autodingding.bean.MailInfo
import com.pengxh.autodingding.utils.Constant
import java.io.File

fun File.createAttachMail(toAddress: String): MailInfo {
    val mailInfo = MailInfo()
    mailInfo.mailServerHost = "smtp.qq.com" //发送方邮箱服务器
    mailInfo.mailServerPort = "587" //发送方邮箱端口号
    mailInfo.isValidate = true
    mailInfo.userName = Constant.USER_MAIL_ACCOUNT
    mailInfo.password = Constant.PERMISSION_CODE
    mailInfo.toAddress = toAddress // 接收者邮箱
    mailInfo.fromAddress = Constant.MAIL_FROM_ADDRESS
    mailInfo.subject = "打卡记录" // 邮件主题
    mailInfo.attachFile = this
    return mailInfo
}