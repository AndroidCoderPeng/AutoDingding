package com.pengxh.autodingding.utils;

import androidx.annotation.NonNull;

import com.pengxh.androidx.lite.utils.TimeOrDateUtil;
import com.pengxh.autodingding.bean.MailInfo;

import java.io.File;

/**
 * @author: Pengxh
 * @email: 290677893@qq.com
 * @date: 2020/1/16 15:39
 */
public class MailInfoUtil {

    @NonNull
    public static MailInfo createMail(String toAddress, String emailMessage) {
        MailInfo mailInfo = new MailInfo();
        mailInfo.setMailServerHost("smtp.qq.com");//发送方邮箱服务器
        mailInfo.setMailServerPort("587");//发送方邮箱端口号
        mailInfo.setValidate(true);
        mailInfo.setUserName("290677893@qq.com"); // 发送者邮箱地址
        mailInfo.setPassword("xaabdfhhvqrfcajj");//邮箱授权码，不是密码
        mailInfo.setToAddress(toAddress); // 接收者邮箱
        mailInfo.setFromAddress("290677893@qq.com"); // 发送者邮箱
        mailInfo.setSubject("自动打卡通知"); // 邮件主题
        if (emailMessage.equals("")) {
            mailInfo.setContent("未监听到打卡成功的通知，请手动登录检查" + TimeOrDateUtil.timestampToDate(System.currentTimeMillis())); // 邮件文本
        } else {
            mailInfo.setContent(emailMessage); // 邮件文本
        }
        return mailInfo;
    }

    @NonNull
    public static MailInfo createAttachMail(String toAddress, File file) {
        MailInfo mailInfo = new MailInfo();
        mailInfo.setMailServerHost("smtp.qq.com");//发送方邮箱服务器
        mailInfo.setMailServerPort("587");//发送方邮箱端口号
        mailInfo.setValidate(true);
        mailInfo.setUserName("290677893@qq.com"); // 发送者邮箱地址
        mailInfo.setPassword("xaabdfhhvqrfcajj");//邮箱授权码，不是密码
        mailInfo.setToAddress(toAddress); // 接收者邮箱
        mailInfo.setFromAddress("290677893@qq.com"); // 发送者邮箱
        mailInfo.setSubject("打卡记录"); // 邮件主题
        mailInfo.setAttachFile(file);
        return mailInfo;
    }
}
