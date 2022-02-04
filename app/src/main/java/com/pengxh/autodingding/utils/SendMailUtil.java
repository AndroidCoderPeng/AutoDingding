package com.pengxh.autodingding.utils;

import android.annotation.SuppressLint;
import android.os.Handler;
import android.os.Message;

import com.pengxh.app.multilib.widget.EasyToast;
import com.pengxh.autodingding.bean.MailInfo;

import java.io.File;

import androidx.annotation.NonNull;

/**
 * @author: Pengxh
 * @email: 290677893@qq.com
 * @description: TODO
 * @date: 2020/1/16 15:39
 */
public class SendMailUtil {

    public static void send(String toAddress, String emailMessage) {
        new Thread(() -> new MailSender().sendTextMail(createMail(toAddress, emailMessage))).start();
    }

    static void sendAttachFileEmail(String toAddress, String filePath) {
        File file = new File(filePath);
        if (!file.exists()) {
            EasyToast.showToast("打卡记录不存在，请检查", EasyToast.ERROR);
            return;
        }
        new Thread(() -> {
            boolean isSendSuccess = new MailSender().sendAccessoryMail(createAttachMail(toAddress, file));
            if (isSendSuccess) {
                handler.sendEmptyMessage(0);
            } else {
                handler.sendEmptyMessage(1);
            }
        }).start();
    }

    @SuppressLint("HandlerLeak")
    private static Handler handler = new Handler() {
        @Override
        public void handleMessage(Message msg) {
            switch (msg.what) {
                case 0:
                    EasyToast.showToast("导出到邮箱成功", EasyToast.SUCCESS);
                    break;
                case 1:
                    EasyToast.showToast("导出到邮箱失败", EasyToast.ERROR);
                    break;
            }
        }
    };

    @NonNull
    private static MailInfo createMail(String toAddress, String emailMessage) {
        MailInfo mailInfo = new MailInfo();
        mailInfo.setMailServerHost("smtp.qq.com");//发送方邮箱服务器
        mailInfo.setMailServerPort("587");//发送方邮箱端口号
        mailInfo.setValidate(true);
        mailInfo.setUserName("290677893@qq.com"); // 发送者邮箱地址
        mailInfo.setPassword("gqvwykjvpnvfbjid");//邮箱授权码，不是密码
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
    private static MailInfo createAttachMail(String toAddress, File file) {
        MailInfo mailInfo = new MailInfo();
        mailInfo.setMailServerHost("smtp.qq.com");//发送方邮箱服务器
        mailInfo.setMailServerPort("587");//发送方邮箱端口号
        mailInfo.setValidate(true);
        mailInfo.setUserName("290677893@qq.com"); // 发送者邮箱地址
        mailInfo.setPassword("gqvwykjvpnvfbjid");//邮箱授权码，不是密码
        mailInfo.setToAddress(toAddress); // 接收者邮箱
        mailInfo.setFromAddress("290677893@qq.com"); // 发送者邮箱
        mailInfo.setSubject("打卡记录"); // 邮件主题
        mailInfo.setAttachFile(file);
        return mailInfo;
    }
}
