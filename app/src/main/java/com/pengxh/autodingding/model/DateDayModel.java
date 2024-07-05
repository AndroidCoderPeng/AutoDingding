package com.pengxh.autodingding.model;

public class DateDayModel {

    private int code;
    private DataModel data;
    private String msg;

    public int getCode() {
        return code;
    }

    public void setCode(int code) {
        this.code = code;
    }

    public DataModel getData() {
        return data;
    }

    public void setData(DataModel data) {
        this.data = data;
    }

    public String getMsg() {
        return msg;
    }

    public void setMsg(String msg) {
        this.msg = msg;
    }

    public static class DataModel {
        private String date;
        private boolean work;

        public String getDate() {
            return date;
        }

        public void setDate(String date) {
            this.date = date;
        }

        public boolean isWork() {
            return work;
        }

        public void setWork(boolean work) {
            this.work = work;
        }
    }
}
