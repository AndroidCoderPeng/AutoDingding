package com.pengxh.autodingding.bean;

import org.greenrobot.greendao.annotation.Entity;
import org.greenrobot.greendao.annotation.Generated;
import org.greenrobot.greendao.annotation.Id;

@Entity
public class HistoryRecordBean {
    @Id(autoincrement = true)
    private Long id;//主键ID

    private String uuid;
    private String date;
    private String message;

    @Generated(hash = 1681950394)
    public HistoryRecordBean(Long id, String uuid, String date, String message) {
        this.id = id;
        this.uuid = uuid;
        this.date = date;
        this.message = message;
    }

    @Generated(hash = 1791356846)
    public HistoryRecordBean() {
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

    public String getDate() {
        return this.date;
    }

    public void setDate(String date) {
        this.date = date;
    }

    public String getMessage() {
        return this.message;
    }

    public void setMessage(String message) {
        this.message = message;
    }
}
