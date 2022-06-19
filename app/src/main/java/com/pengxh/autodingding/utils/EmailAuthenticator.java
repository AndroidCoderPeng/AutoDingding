package com.pengxh.autodingding.utils;

import javax.mail.Authenticator;
import javax.mail.PasswordAuthentication;

/**
 * @author: Pengxh
 * @email: 290677893@qq.com
 * @date: 2020/1/16 15:42
 */
public class EmailAuthenticator extends Authenticator {

    private final String userName;
    private final String password;

    public EmailAuthenticator(String username, String password) {
        this.userName = username;
        this.password = password;
    }

    protected PasswordAuthentication getPasswordAuthentication() {
        return new PasswordAuthentication(userName, password);
    }
}
