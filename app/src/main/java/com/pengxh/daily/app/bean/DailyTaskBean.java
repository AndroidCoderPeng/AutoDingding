package com.pengxh.daily.app.bean;

import org.greenrobot.greendao.annotation.Entity;
import org.greenrobot.greendao.annotation.Generated;
import org.greenrobot.greendao.annotation.Id;

@Entity
public class DailyTaskBean {
    @Id(autoincrement = true)
    private Long id;//主键ID

    private String uuid;
    private String time;

    @Generated(hash = 547825290)
    public DailyTaskBean(Long id, String uuid, String time) {
        this.id = id;
        this.uuid = uuid;
        this.time = time;
    }

    @Generated(hash = 542839617)
    public DailyTaskBean() {
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

    public String getTime() {
        return this.time;
    }

    public void setTime(String time) {
        this.time = time;
    }
}
