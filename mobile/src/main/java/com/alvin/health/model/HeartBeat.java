package com.alvin.health.model;

import com.alvin.health.base.BaseEntity;

/**
 * Created by alvin on 2016/1/29.
 */
public class HeartBeat extends BaseEntity {

    private static final long serialVersionUID = -4914945401904490L;
    private String phone;
    private float beat;
    private int mode;

    public int getMode() {
        return mode;
    }

    public void setMode(int mode) {
        this.mode = mode;
    }

    private String testtime;
    private String createtime;

    public String getPhone() {
        return phone;
    }

    public void setPhone(String phone) {
        this.phone = phone;
    }

    public float getBeat() {
        return beat;
    }

    public void setBeat(float beat) {
        this.beat = beat;
    }

    public String getTesttime() {
        return testtime;
    }

    public void setTesttime(String testtime) {
        this.testtime = testtime;
    }

    public String getCreatetime() {
        return createtime;
    }

    public void setCreatetime(String createtime) {
        this.createtime = createtime;
    }
}
