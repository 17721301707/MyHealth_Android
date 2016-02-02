package com.alvin.health.magager;

import android.content.Context;

import com.alvin.health.data.ConnectData;
import com.alvin.health.model.HeartBeat;
import com.alvin.health.model.ResultModel;
import com.alvin.health.net.ConnectClient;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;

import java.io.InputStream;
import java.util.LinkedList;
import java.util.List;
import java.util.Queue;

/**
 * Created by alvin on 2016/1/8.
 * User info manager to login or verify or register
 */
public class HeartBeatManager {
    private   static HeartBeatManager mInstance;
    private  Context mContext;
    private Queue<HeartBeat> datas;

    public static synchronized HeartBeatManager getInstance(Context context)
    {
        if(mInstance == null){
            mInstance = new HeartBeatManager(context);
        }
        return mInstance;
    }

    private HeartBeatManager(Context context)
    {
        mContext = context;
        datas = new LinkedList<HeartBeat>();
    }

    public Queue<HeartBeat> getDatas() {
        return datas;
    }

    public void setDatas(Queue<HeartBeat> datas) {
        this.datas = datas;
    }

    public boolean addData(HeartBeat beat){
        return datas.offer(beat);
    }

    /**
     *Verify Phone is register or not
     * @param
     * @return
     */
    public ResultModel submit(List<HeartBeat> beats) {
        ResultModel result = null;
        String reqUrl = ConnectData.BEAT;
        GsonBuilder builder = new GsonBuilder();
        Gson gson = builder.create();
        InputStream is;
        try {
            is = ConnectClient._post(mContext, reqUrl, gson.toJson(beats), null);
            byte[] data = ConnectClient.readStream(is);
            String str = new String(data);
            result = gson.fromJson(str, ResultModel.class);
        } catch (Exception e) {
            e.printStackTrace();
        }
        return result;
    }
}
