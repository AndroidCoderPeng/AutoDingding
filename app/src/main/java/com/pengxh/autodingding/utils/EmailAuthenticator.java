package com.pengxh.autodingding.utils;

import javax.mail.Authenticator;
import javax.mail.PasswordAuthentication;

/**
 * @author: Pengxh
 * @email: 290677893@qq.com
 * @description: TODO
 * @date: 2020/1/16 15:42
 */
public class EmailAuthenticator extends Authenticator {

    private String userName = null;
    private String password = null;

    public EmailAuthenticator(String username, String password) {
        this.userName = username;
        this.password = password;
    }

    protected PasswordAuthentication getPasswordAuthentication() {
        return new PasswordAuthentication(userName, password);
    }
}
