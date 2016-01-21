package com.alvin.myhealth.model;

import com.alvin.myhealth.base.BaseEntity;

/**
 * Created by alvin on 2016/1/19.
 * User information Model
 */
public class UserInfo extends BaseEntity {

    private static final long serialVersionUID = -7513365813596633643L;
    private  String phone;
    private String password;
    private String createtime;
    public String getPhone() {
        return phone;
    }

    public void setPhone(String phone) {
        this.phone = phone;
    }

    public String getPassword() {
        return password;
    }

    public void setPassword(String password) {
        this.password = password;
    }

    public String getCreatetime() {
        return createtime;
    }

    public void setCreatetime(String createtime) {
        this.createtime = createtime;
    }
}
