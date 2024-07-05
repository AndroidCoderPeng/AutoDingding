package com.pengxh.autodingding.bean;

import org.greenrobot.greendao.annotation.Entity;
import org.greenrobot.greendao.annotation.Generated;
import org.greenrobot.greendao.annotation.Id;

@Entity
public class TaskTimeBean {
    @Id(autoincrement = true)
    private Long id;//主键ID

    private String uuid;

    private String startTime;
    private String endTime;

    @Generated(hash = 482492813)
    public TaskTimeBean(Long id, String uuid, String startTime, String endTime) {
        this.id = id;
        this.uuid = uuid;
        this.startTime = startTime;
        this.endTime = endTime;
    }

    @Generated(hash = 322859731)
    public TaskTimeBean() {
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

    public String getStartTime() {
        return this.startTime;
    }

    public void setStartTime(String startTime) {
        this.startTime = startTime;
    }

    public String getEndTime() {
        return this.endTime;
    }

    public void setEndTime(String endTime) {
        this.endTime = endTime;
    }
}
