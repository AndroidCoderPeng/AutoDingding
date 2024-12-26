package com.pengxh.daily.app.bean;

import org.greenrobot.greendao.annotation.Entity;
import org.greenrobot.greendao.annotation.Generated;
import org.greenrobot.greendao.annotation.Id;

@Entity
public class NotificationBean {
    @Id(autoincrement = true)
    private Long id;//主键ID

    private String uuid;
    private String packageName;
    private String notificationTitle;
    private String notificationMsg;
    private String postTime;

    @Generated(hash = 2089076920)
    public NotificationBean(Long id, String uuid, String packageName,
                            String notificationTitle, String notificationMsg, String postTime) {
        this.id = id;
        this.uuid = uuid;
        this.packageName = packageName;
        this.notificationTitle = notificationTitle;
        this.notificationMsg = notificationMsg;
        this.postTime = postTime;
    }

    @Generated(hash = 1804399548)
    public NotificationBean() {
    }

    public Long getId() {
        return this.id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getUuid() {
        return this.uuid;
    }

    public void setUuid(String uuid) {
        this.uuid = uuid;
    }

    public String getPackageName() {
        return this.packageName;
    }

    public void setPackageName(String packageName) {
        this.packageName = packageName;
    }

    public String getNotificationTitle() {
        return this.notificationTitle;
    }

    public void setNotificationTitle(String notificationTitle) {
        this.notificationTitle = notificationTitle;
    }

    public String getNotificationMsg() {
        return this.notificationMsg;
    }

    public void setNotificationMsg(String notificationMsg) {
        this.notificationMsg = notificationMsg;
    }

    public String getPostTime() {
        return this.postTime;
    }

    public void setPostTime(String postTime) {
        this.postTime = postTime;
    }
}
