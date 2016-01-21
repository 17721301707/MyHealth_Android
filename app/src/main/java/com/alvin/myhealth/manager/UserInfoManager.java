package com.alvin.myhealth.manager;

import android.content.Context;

import com.alvin.myhealth.base.BaseEntity;
import com.alvin.myhealth.data.ConnectData;
import com.alvin.myhealth.model.ResultModel;
import com.alvin.myhealth.model.UserInfo;
import com.alvin.myhealth.net.ConnectClient;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;

import java.io.InputStream;

/**
 * Created by alvin on 2016/1/8.
 * User info manager to login or verify or register
 */
public class UserInfoManager extends BaseEntity{
    private static final long serialVersionUID = -3870945070411352043L;
    private   static UserInfoManager mInstance;
    private  Context mContext;
    private UserInfo userInfo;

    public static synchronized UserInfoManager getInstance(Context context)
    {
        if(mInstance == null){
            mInstance = new UserInfoManager(context);
        }
        return mInstance;
    }

    private UserInfoManager(Context context)
    {
        mContext = context;
    }

    public UserInfo getUserInfo() {
        return userInfo;
    }

    public void setUserInfo(UserInfo userInfo) {
        this.userInfo = userInfo;
    }

    /**
     *Verify Phone is register or not
     * @param phone
     * @param phone
     * @return
     */
    public  ResultModel verifyPhone( String phone) {
        ResultModel result = null;
        String reqUrl = ConnectData.ISUSEREXIST;
        GsonBuilder builder = new GsonBuilder();
        Gson gson = builder.create();
        InputStream is;
        try {
            is = ConnectClient._post(mContext,reqUrl,phone,null);
            byte[] data = ConnectClient.readStream(is);
            String str = new String(data);
            result = gson.fromJson(str, ResultModel.class);
        } catch (Exception e) {
            e.printStackTrace();
        }
        return result;
    }

    /**
     * 登录
     * @param userInfo
     * @return
     */
    public UserInfo login(UserInfo userInfo)
    {
        UserInfo returnInfo = null;
        String reqUrl = ConnectData.LOGIN;
        GsonBuilder builder = new GsonBuilder();
        Gson gson = builder.create();
        InputStream is;
        try {
            is = ConnectClient._post(mContext,reqUrl,gson.toJson(userInfo),null);
            byte[] data = ConnectClient.readStream(is);
            String str = new String(data);
            returnInfo = gson.fromJson(str, UserInfo.class);
        } catch (Exception e) {
            e.printStackTrace();
        }
        return  returnInfo;
    }
}
