package com.pengxh.autodingding.utils;

import android.support.annotation.NonNull;
import android.util.Log;

import com.pengxh.autodingding.bean.MailInfo;

import java.text.SimpleDateFormat;
import java.util.Date;

/**
 * @author: Pengxh
 * @email: 290677893@qq.com
 * @description: TODO
 * @date: 2020/1/16 15:39
 */
public class SendMailUtil {

    private static final String TAG = "SendMailUtil";

    public static void send(String toAddress) {
        final MailInfo mailInfo = createMail(toAddress);
        new Thread(new Runnable() {
            @Override
            public void run() {
                boolean isSendSuccess = new MailSender().sendTextMail(mailInfo);
                Log.d(TAG, "run: 邮件发送成功？--->" + isSendSuccess);
            }
        }).start();
    }

    @NonNull
    private static MailInfo createMail(String toAddress) {
        MailInfo mailInfo = new MailInfo();
        mailInfo.setMailServerHost("smtp.qq.com");//发送方邮箱服务器
        mailInfo.setMailServerPort("587");//发送方邮箱端口号
        mailInfo.setValidate(true);
        mailInfo.setUserName("290677893@qq.com"); // 发送者邮箱地址
        mailInfo.setPassword("gqvwykjvpnvfbjid");//邮箱授权码，不是密码
        mailInfo.setToAddress(toAddress); // 接收者邮箱
        mailInfo.setFromAddress("290677893@qq.com"); // 发送者邮箱
        mailInfo.setSubject("自动打卡通知"); // 邮件主题
        String date = rTimeMillisToDate(System.currentTimeMillis());
        mailInfo.setContent(date + "打卡成功"); // 邮件文本
        return mailInfo;
    }

    private static String rTimeMillisToDate(long millis) {
        SimpleDateFormat simpleDateFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
        Date date = new Date(millis);
        return simpleDateFormat.format(date);
    }
}
