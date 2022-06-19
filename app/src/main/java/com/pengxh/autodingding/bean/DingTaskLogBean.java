package com.pengxh.autodingding.bean;

import org.greenrobot.greendao.annotation.Entity;
import org.greenrobot.greendao.annotation.Generated;
import org.greenrobot.greendao.annotation.Id;

@Entity
public class DingTaskLogBean {

    @Id(autoincrement = true)
    private Long id;//主键ID

    private String title;
    private String time;

    @Generated(hash = 94429308)
    public DingTaskLogBean(Long id, String title, String time) {
        this.id = id;
        this.title = title;
        this.time = time;
    }

    @Generated(hash = 1372939801)
    public DingTaskLogBean() {
    }

    public Long getId() {
        return this.id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getTitle() {
        return this.title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getTime() {
        return this.time;
    }

    public void setTime(String time) {
        this.time = time;
    }
}
