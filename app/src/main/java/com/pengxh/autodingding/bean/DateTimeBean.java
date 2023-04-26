package com.pengxh.autodingding.bean;

import org.greenrobot.greendao.annotation.Entity;
import org.greenrobot.greendao.annotation.Generated;
import org.greenrobot.greendao.annotation.Id;

@Entity
public class DateTimeBean {
    @Id(autoincrement = true)
    private Long id;//主键ID

    private String uuid;
    private String date;
    private String time;
    private String weekDay;

    @Generated(hash = 590077470)
    public DateTimeBean(Long id, String uuid, String date, String time, String weekDay) {
        this.id = id;
        this.uuid = uuid;
        this.date = date;
        this.time = time;
        this.weekDay = weekDay;
    }

    @Generated(hash = 1790840121)
    public DateTimeBean() {
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

    public String getTime() {
        return this.time;
    }

    public void setTime(String time) {
        this.time = time;
    }

    public String getWeekDay() {
        return this.weekDay;
    }

    public void setWeekDay(String weekDay) {
        this.weekDay = weekDay;
    }
}
