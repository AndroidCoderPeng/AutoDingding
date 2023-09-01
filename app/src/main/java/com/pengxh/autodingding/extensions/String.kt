package com.pengxh.autodingding.extensions

import com.pengxh.autodingding.bean.MailInfo
import com.pengxh.kt.lite.extensions.timestampToDate
import java.text.ParseException
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Locale
import kotlin.math.abs

fun String.convertToWeek(): String {
    val format = SimpleDateFormat("yyyy-MM-dd", Locale.CHINA)
    val calendar = Calendar.getInstance()
    try {
        calendar.time = format.parse(this)!!
    } catch (e: ParseException) {
        e.printStackTrace()
    }
    return when (calendar.get(Calendar.DAY_OF_WEEK)) {
        1 -> return "周日"
        2 -> return "周一"
        3 -> return "周二"
        4 -> return "周三"
        5 -> return "周四"
        6 -> return "周五"
        7 -> return "周六"
        else -> "错误"
    }
}

fun String.isEarlierThenCurrent(): Boolean {
    try {
        val dateFormat = SimpleDateFormat("yyyy-MM-dd HH:mm", Locale.CHINA)
        val date = dateFormat.parse(this)!!
        val t1 = date.time
        val t2 = System.currentTimeMillis()
        return (t1 - t2) < 0
    } catch (e: ParseException) {
        e.printStackTrace()
    }
    return false
}

/**
 * 时间差-秒
 * */
fun String.diffCurrentMillis(): Long {
    if (this.isBlank()) {
        return 0
    }
    val simpleDateFormat = SimpleDateFormat("yyyy-MM-dd HH:mm", Locale.CHINA)
    val date = simpleDateFormat.parse(this)!!
    return abs(System.currentTimeMillis() - date.time)
}

fun String.createMail(toAddress: String): MailInfo {
    val mailInfo = MailInfo()
    mailInfo.mailServerHost = "smtp.qq.com" //发送方邮箱服务器
    mailInfo.mailServerPort = "587" //发送方邮箱端口号
    mailInfo.isValidate = true
    mailInfo.userName = "290677893@qq.com" // 发送者邮箱地址
    mailInfo.password = "sgbozwzhkfvjcaie" //邮箱授权码，不是密码
    mailInfo.toAddress = toAddress // 接收者邮箱
    mailInfo.fromAddress = "290677893@qq.com" // 发送者邮箱
    mailInfo.subject = "自动打卡通知" // 邮件主题
    val content = if (this == "") {
        "未监听到打卡成功的通知，请手动登录检查" + System.currentTimeMillis().timestampToDate()
    } else {
        this
    }
    // 邮件文本
    mailInfo.content = content
    return mailInfo
}