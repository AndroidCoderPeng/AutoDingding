package com.pengxh.daily.app.model;

public class EmailConfigModel {
    private String emailSender;
    private String permissionCode;
    private String SenderServer;
    private String emailPort;
    private String inboxEmail;
    private String emailTitle;

    public EmailConfigModel(String emailSender, String permissionCode, String senderServer, String emailPort, String inboxEmail, String emailTitle) {
        this.emailSender = emailSender;
        this.permissionCode = permissionCode;
        this.SenderServer = senderServer;
        this.emailPort = emailPort;
        this.inboxEmail = inboxEmail;
        this.emailTitle = emailTitle;
    }

    public String getEmailSender() {
        return emailSender;
    }

    public void setEmailSender(String emailSender) {
        this.emailSender = emailSender;
    }

    public String getPermissionCode() {
        return permissionCode;
    }

    public void setPermissionCode(String permissionCode) {
        this.permissionCode = permissionCode;
    }

    public String getSenderServer() {
        return SenderServer;
    }

    public void setSenderServer(String senderServer) {
        SenderServer = senderServer;
    }

    public String getEmailPort() {
        return emailPort;
    }

    public void setEmailPort(String emailPort) {
        this.emailPort = emailPort;
    }

    public String getInboxEmail() {
        return inboxEmail;
    }

    public void setInboxEmail(String inboxEmail) {
        this.inboxEmail = inboxEmail;
    }

    public String getEmailTitle() {
        return emailTitle;
    }

    public void setEmailSubject(String emailTitle) {
        this.emailTitle = emailTitle;
    }
}