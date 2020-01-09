package com.pengxh.autodingding.bean;

/**
 * @author: Pengxh
 * @email: 290677893@qq.com
 * @description: TODO
 * @date: 2020/1/9 13:11
 */
public class ClockBean {
    private String uuid;//唯一标识码
    private String clockTime;//闹钟时间
    private int clockStatus;//闹钟状态，0/1

    public String getUuid() {
        return uuid;
    }

    public void setUuid(String uuid) {
        this.uuid = uuid;
    }

    public String getClockTime() {
        return clockTime;
    }

    public void setClockTime(String clockTime) {
        this.clockTime = clockTime;
    }

    public int getClockStatus() {
        return clockStatus;
    }

    public void setClockStatus(int clockStatus) {
        this.clockStatus = clockStatus;
    }
}
