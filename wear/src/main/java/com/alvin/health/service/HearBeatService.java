package com.alvin.health.service;

import android.app.Service;
import android.content.Intent;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.os.Bundle;
import android.os.Handler;
import android.os.IBinder;
import android.util.Log;

import com.alvin.health.data.AppData;
import com.alvin.health.utils.AppTool;
import com.alvin.health.utils.MyFile;
import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.common.api.PendingResult;
import com.google.android.gms.common.api.ResultCallback;
import com.google.android.gms.wearable.Asset;
import com.google.android.gms.wearable.DataApi;
import com.google.android.gms.wearable.DataEventBuffer;
import com.google.android.gms.wearable.MessageApi;
import com.google.android.gms.wearable.MessageEvent;
import com.google.android.gms.wearable.Node;
import com.google.android.gms.wearable.NodeApi;
import com.google.android.gms.wearable.PutDataMapRequest;
import com.google.android.gms.wearable.PutDataRequest;
import com.google.android.gms.wearable.Wearable;

import java.util.List;

/**
 * Created by alvin on 2015/11/17.
 * Get Heart Beat Data Service
 */
public class HearBeatService extends Service implements SensorEventListener
        , GoogleApiClient.ConnectionCallbacks, MessageApi.MessageListener
        , GoogleApiClient.OnConnectionFailedListener, NodeApi.NodeListener
        , DataApi.DataListener {
    private final static String TAG = "Alvin";
    //the handle to control how long time to catch data one time
    private Handler mTimeHandler;
    private Runnable mTimeRunnable;
    //the handle to control what space time to catch data
    private Handler mSensorHandler;
    private Runnable mSensorRunnable;
    //control run time to catch data
    private int mStepTime = 0;
    private SensorManager mSensorManager;
    private Sensor mHeartRateSensor;
    //calculate average of heart beat
    private int mTotall = 0;
    //the count of one time to catch data
    private int count = 0;
    //the flag of catch data, false is no catching, true is catching
    private boolean mRunFlag = false;
    private int mPerValue = 0;
    private MyFile file;
    //catch data enable
    private boolean mCatchEnable = false;
    //Client connection
    private GoogleApiClient mGoogleApiClient;
    private List<Node> mConnectedNodes;
    private final String mFileName = "heart.csv";
    private int mMode = HearBeatMode.INTERVAL;

    @Override
    public IBinder onBind(Intent intent) {
        Log.d(TAG, "onBind");
        return null;
    }

    @Override
    public void onCreate() {
        Log.d(TAG, "onCreate->SensorManager Init");
        // register us as a sensor listener
        mSensorManager = (SensorManager) getSystemService(SENSOR_SERVICE);
        mHeartRateSensor = mSensorManager.getDefaultSensor(Sensor.TYPE_HEART_RATE);
        //File to storage data
        file = new MyFile();
        //Connection
        mGoogleApiClient = new GoogleApiClient.Builder(this)
                .addApi(Wearable.API)
                .addConnectionCallbacks(this)
                .addOnConnectionFailedListener(this)
                .build();
        super.onCreate();
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        Log.d(TAG, "onStartCommand");
        if (null != mGoogleApiClient) {
            if (!mGoogleApiClient.isConnected()) {
                mGoogleApiClient.connect();
                Log.d(TAG, "start to connect");
            }
        }
        mStepTime = 0;
        if (null == mTimeHandler) {
            mSensorHandler = new Handler();
            mTimeHandler = new Handler();

            mSensorRunnable = new Runnable() {
                @Override
                public void run() {
                    mPerValue = 0;
                    mRunFlag = false;
                    int avg = mTotall / count;
                    syncData(String.valueOf(avg));
                    mSensorManager.unregisterListener(HearBeatService.this);
                    Log.d(TAG, "UnRegister Sensor");
                    mTimeHandler.postDelayed(mTimeRunnable, 20000);
                }
            };

            mTimeRunnable = new Runnable() {
                @Override
                public void run() {
                    mStepTime++;
                    Log.d(TAG, "Call time:" + mStepTime);
                    boolean res = mSensorManager.registerListener(HearBeatService.this,
                            mHeartRateSensor, SensorManager.SENSOR_DELAY_NORMAL);
                    Log.d(TAG, "Sensor Register " + (res ? "Success" : "Fail"));
                    mSensorHandler.postDelayed(mSensorRunnable, 20000);
                }
            };
        }
        return super.onStartCommand(intent, flags, startId);
    }

    @Override
    public void onConnected(Bundle bundle) {
        Log.d(TAG, "GoogleConnection Connected");
        Wearable.MessageApi.addListener(mGoogleApiClient, this);
        Wearable.NodeApi.addListener(mGoogleApiClient, this);
        Wearable.DataApi.addListener(mGoogleApiClient, this);
        new Thread(new Runnable() {
            @Override
            public void run() {
                mConnectedNodes = AppTool.getNodes(mGoogleApiClient);
                if (mConnectedNodes.size() > 0) {
                    for (Node node : mConnectedNodes) {
                        Log.d(TAG, "Connected Node->ID:" + node.getId() + " Name:"
                                + node.getDisplayName());
                    }
                    if (mCatchEnable) {
                        sendMessage(AppData.MSG_PATH_HEART_BEAT_RES, "enable");
                    } else {
                        sendMessage(AppData.MSG_PATH_HEART_BEAT_RES, "disable");
                    }
                    sendMessage(AppData.MSG_PATH_MODE,String.valueOf(mMode));
                } else {
                    Log.d(TAG, "No Connected Nodes");
                }
            }
        }).start();
    }

    @Override
    public void onPeerConnected(Node node) {
        Log.d(TAG, "onPeerConnected:" + node.getDisplayName());
    }

    @Override
    public void onPeerDisconnected(Node node) {
        Log.d(TAG, "onPeerDisconnected:" + node.getDisplayName());
    }

    @Override
    public void onConnectionSuspended(int i) {
        Log.d(TAG, "onConnectionSuspended");
    }

    @Override
    public void onConnectionFailed(ConnectionResult connectionResult) {
        Log.d(TAG, "GoogleConnection Failed");
    }

    @Override
    public void onMessageReceived(MessageEvent messageEvent) {
        String msgReceiver = new String(messageEvent.getData());
        Log.d(TAG, "onMessageReceived:" + messageEvent.getPath() + " Msg:" + msgReceiver);
        if (messageEvent.getPath().equals(AppData.MSG_PATH_SERVICE_STOP)) {
            //String receiver = new String(messageEvent.getData());
            Log.d(TAG, "msg stop service");
            sendMessage(AppData.MSG_PATH_HEART_BEAT_RES, "service");
            stopSelf();
        } else if (messageEvent.getPath().equals(AppData.MSG_PATH_HEART_BEAT_START)) {
            Log.d(TAG, "data catch start");
            mCatchEnable = true;
            mSensorManager.unregisterListener(HearBeatService.this);

            if (HearBeatMode.INTERVAL == mMode) {
                mPerValue = 0;
                mRunFlag = false;
                mTotall = 0;
                mTimeHandler.post(mTimeRunnable);
            } else {
                mTimeHandler.removeCallbacks(mTimeRunnable);
                mSensorHandler.removeCallbacks(mSensorRunnable);
                boolean res = mSensorManager.registerListener(HearBeatService.this,
                        mHeartRateSensor, SensorManager.SENSOR_DELAY_NORMAL);
                Log.d(TAG, "Sensor Register " + (res ? "Success" : "Fail"));
            }
            sendMessage(AppData.MSG_PATH_HEART_BEAT_RES, "enable");
            sendMessage(AppData.MSG_PATH_MODE, String.valueOf(mMode));

        } else if (messageEvent.getPath().equals(AppData.MSG_PATH_HEART_BEAT_STOP)) {
            Log.d(TAG, "data catch stop");
            mCatchEnable = false;
            sendMessage(AppData.MSG_PATH_HEART_BEAT_RES, "disable");
            sendMessage(AppData.MSG_PATH_MODE, String.valueOf(mMode));
            mSensorManager.unregisterListener(this);
            mTimeHandler.removeCallbacks(mTimeRunnable);
            mSensorHandler.removeCallbacks(mSensorRunnable);
        } else if (messageEvent.getPath().equals(AppData.MSG_PATH_SERVICE_STATUES)) {
            Log.d(TAG, "service statues");
            if (mCatchEnable) {
                sendMessage(AppData.MSG_PATH_HEART_BEAT_RES, "enable");
            } else {
                sendMessage(AppData.MSG_PATH_HEART_BEAT_RES, "disable");
            }
            sendMessage(AppData.MSG_PATH_MODE,String.valueOf(mMode));
        } else if (messageEvent.getPath().equals(AppData.MSG_PATH_FILE)) {
            switch (msgReceiver) {
                case "Delete":
                    if (file.deleteFile(mFileName)) {
                        Log.d(TAG, "Clean Data Success");
                        sendMessage(AppData.MSG_PATH_HEART_BEAT_RES, "delete");
                    }
                    break;
                case "Get":
                    Asset mAsset = file.getFile(mFileName);
                    if (null != mAsset) {
                        Log.d(TAG, "send asset data:"+new String(mAsset.getData()));
                        syncData(mAsset);
                    } else {
                        Log.d(TAG, "file asset is null");
                    }
                    break;
                default:
                    break;
            }

        } else if (messageEvent.getPath().equals(AppData.MSG_PATH_MODE)) {
            switch (msgReceiver) {
                case "1":
                    mMode = HearBeatMode.INTERVAL;
                    break;
                case "2":
                    mMode = HearBeatMode.CONTINUOUS;
                    break;
                default:
                    break;
            }
        }
    }

    /**
     * send file context data to handle plat
     * @param data file array byte asset
     */
    private void syncData(Asset data) {
        PutDataMapRequest dataMap = PutDataMapRequest.create(AppData.DATA_PATH_FILE);
        dataMap.getDataMap().putAsset("data", data);
        dataMap.getDataMap().putString("time", AppTool.getTime());
        dataMap.getDataMap().putInt("mode", mMode);
        PutDataRequest request = dataMap.asPutDataRequest();
        PendingResult<DataApi.DataItemResult> pendingResult = Wearable.DataApi
                .putDataItem(mGoogleApiClient, request);
    }

    /**
     * send heart beat data to handle plat
     * @param text heart beat
     */
    private void syncData(String text) {
        String time = AppTool.getTime();
        PutDataMapRequest dataMap = PutDataMapRequest.create(AppData.DATA_PATH_HEART_BEAT);
        dataMap.getDataMap().putString("data", text);
        dataMap.getDataMap().putString("time", time);
        dataMap.getDataMap().putInt("mode", mMode);
        file.write(mFileName, time + "," + text + "," + mMode+"\n");
        PutDataRequest request = dataMap.asPutDataRequest();
        PendingResult<DataApi.DataItemResult> pendingResult = Wearable.DataApi
                .putDataItem(mGoogleApiClient, request);
    }

    /**
     * Use MessageApi to send message
     *
     * @param msgPath MessageApi path
     * @param context the string message to send
     */
    private void sendMessage(String msgPath, final String context) {
        if ((null != mConnectedNodes) && (mConnectedNodes.size() > 0)) {
            //the message send to wear
            byte[] sendDatas = context.getBytes();
            //deliver message
            Wearable.MessageApi.sendMessage(mGoogleApiClient, mConnectedNodes.get(0).getId()
                    , msgPath, sendDatas)
                    .setResultCallback(new ResultCallback<MessageApi.SendMessageResult>() {
                        @Override
                        public void onResult(MessageApi.SendMessageResult sendMessageResult) {
                            if (sendMessageResult.getStatus().isSuccess()) {
                                Log.d(TAG, "sendMessage Success:" + context);
                            } else {
                                Log.d(TAG, "sendMessage Fail:" + context + "->"
                                        + sendMessageResult.getStatus()
                                        .getStatusMessage());
                            }

                        }
                    });
        }
    }

    @Override
    public void onDataChanged(DataEventBuffer dataEventBuffer) {

    }

    @Override
    public void onSensorChanged(SensorEvent sensorEvent) {
        // is this a heartbeat event and does it have data?
        if (sensorEvent.sensor.getType() == Sensor.TYPE_HEART_RATE && sensorEvent.values.length > 0) {
            int newValue = Math.round(sensorEvent.values[0]);
            // Log.d(TAG, "HearBeat->" + newValue);
            switch (mMode) {
                case HearBeatMode.INTERVAL:
                    if ((0 == mPerValue) && (0 != newValue)) {
                        mRunFlag = true;
                        mTotall = 0;
                        count = 0;
                    }
                    if (mRunFlag && (0 != newValue)) {
                        count++;
                        mTotall += newValue;
                    }
                    mPerValue = newValue;
                    break;
                case HearBeatMode.CONTINUOUS:
                    mPerValue = 0;
                    mRunFlag = false;
                    syncData(String.valueOf(newValue));
                    break;
                default:
                    break;
            }

        }
    }

    @Override
    public void onAccuracyChanged(Sensor sensor, int accuracy) {

    }

    @Override
    public void onDestroy() {
        Log.d(TAG, "onDestroy");
        mSensorManager.unregisterListener(this);
        mTimeHandler.removeCallbacks(mTimeRunnable);
        mSensorHandler.removeCallbacks(mSensorRunnable);
        Wearable.NodeApi.removeListener(mGoogleApiClient, this);
        Wearable.MessageApi.removeListener(mGoogleApiClient, this);
        Wearable.DataApi.removeListener(mGoogleApiClient, this);
        if ((null != mGoogleApiClient) && mGoogleApiClient.isConnected()) {
            mGoogleApiClient.disconnect();
        }
        super.onDestroy();
    }

    abstract class HearBeatMode {
        public static final int INTERVAL = 0x0001;
        public static final int CONTINUOUS = 0x0002;
    }
}
