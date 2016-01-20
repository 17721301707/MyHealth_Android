package com.alvin.myhealth.model;

import com.alvin.myhealth.base.BaseEntity;

/**
 * Created by alvin on 2016/1/8.
 * Http return parameter type
 */
public class ResultModel extends BaseEntity{

    private static final long serialVersionUID = 8964927114876769600L;
    /**
     * 返回状态成功 失败
     */
    private Boolean flag;

    /**
     * 返回操作内容
     */
    private String content;

    public Boolean getFlag() {
        return flag;
    }

    public void setFlag(Boolean flag) {
        this.flag = flag;
    }

    public String getContent() {
        return content;
    }

    public void setContent(String content) {
        this.content = content;
    }
}
