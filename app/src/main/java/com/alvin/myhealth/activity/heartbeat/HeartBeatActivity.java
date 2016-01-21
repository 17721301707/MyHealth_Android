package com.alvin.myhealth.activity.heartbeat;

import android.app.Activity;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.ScrollView;
import android.widget.Switch;
import android.widget.TextView;
import android.widget.Toast;

import com.alvin.myhealth.R;
import com.alvin.myhealth.Util.AppTool;
import com.alvin.myhealth.data.AppData;
import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.common.api.ResultCallback;
import com.google.android.gms.wearable.Asset;
import com.google.android.gms.wearable.DataApi;
import com.google.android.gms.wearable.DataEvent;
import com.google.android.gms.wearable.DataEventBuffer;
import com.google.android.gms.wearable.DataItem;
import com.google.android.gms.wearable.DataMap;
import com.google.android.gms.wearable.DataMapItem;
import com.google.android.gms.wearable.MessageApi;
import com.google.android.gms.wearable.MessageEvent;
import com.google.android.gms.wearable.Node;
import com.google.android.gms.wearable.NodeApi;
import com.google.android.gms.wearable.Wearable;

import java.io.InputStream;
import java.util.List;

/**
 * Created by alvin on 2016/1/20.
 * A screen show heart beat operation
 */
public class HeartBeatActivity extends Activity implements DataApi.DataListener
        , MessageApi.MessageListener, GoogleApiClient.ConnectionCallbacks
        , GoogleApiClient.OnConnectionFailedListener
        , View.OnClickListener, NodeApi.NodeListener
        , RadioGroup.OnCheckedChangeListener {
    /**
     * UI
     **/
    private Switch mNodeSwitch;
    private Button mCatchStart;
    private Button mCatchStop;
    private Button mServiceStop;
    private TextView mNodeInfo;
    private Switch mClientSwitch;
    private TextView mNodeName;
    private Switch mCatchStatues;
    private Switch mServiceStatues;
    private TextView mBeatData;
    private Button mCleanData;
    private Button mGetData;
    private RadioGroup mGroup;
    private RadioButton mModeInterval;
    private RadioButton mModeContinuous;
    private ScrollView mScrollView;

    private Handler mHandle = new Handler() {
        @Override
        public void handleMessage(Message msg) {
            switch (msg.what) {
                case 1:
                    Bundle bd = msg.getData();
                    mNodeName.setText(bd.getString("name"));
                    mNodeSwitch.setChecked(true);
                    mNodeSwitch.setText(getResources().getText(R.string.connected));
                    mNodeInfo.setText("Id:" + bd.getString("id"));
                    break;
                case 2:
                    Bundle info = msg.getData();
                    mBeatData.setText(info.getString("file"));
                    mScrollView.fullScroll(ScrollView.FOCUS_DOWN);
            }
            super.handleMessage(msg);
        }
    };

    private final String TAG = "Alvin";
    //Connection Client,Connect to Google Play services
    private GoogleApiClient mGoogleApiClient;
    private List<Node> mConnectedNodes;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_heartbeat);
        mNodeSwitch = (Switch) findViewById(R.id.node_statues);
        mCatchStart = (Button) findViewById(R.id.catch_start);
        mCatchStop = (Button) findViewById(R.id.catch_end);
        mServiceStop = (Button) findViewById(R.id.service_stop);
        mNodeInfo = (TextView) findViewById(R.id.node_info);
        mNodeName = (TextView) findViewById(R.id.node_name);
        mClientSwitch = (Switch) findViewById(R.id.client_statues);
        mCatchStatues = (Switch) findViewById(R.id.catch_statues);
        mServiceStatues = (Switch) findViewById(R.id.service_statues);
        mBeatData = (TextView) findViewById(R.id.beat_value);
        mCleanData = (Button) findViewById(R.id.clean_data);
        mGetData = (Button) findViewById(R.id.get_data);
        mGroup = (RadioGroup) findViewById(R.id.mode_group);
        mModeInterval = (RadioButton) findViewById(R.id.mode_interval);
        mModeContinuous = (RadioButton) findViewById(R.id.mode_continuous);
        mScrollView = (ScrollView) findViewById(R.id.scrollView);
        mGroup.setOnCheckedChangeListener(this);
        mCatchStart.setOnClickListener(this);
        mCatchStop.setOnClickListener(this);
        mServiceStop.setOnClickListener(this);
        mNodeSwitch.setClickable(false);
        mCleanData.setOnClickListener(this);
        mGetData.setOnClickListener(this);

        mGoogleApiClient = new GoogleApiClient.Builder(this)
                .addApi(Wearable.API)
                .addConnectionCallbacks(this)
                .addOnConnectionFailedListener(this)
                .build();
    }
    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.service_stop:
                sendMessage(AppData.MSG_PATH_SERVICE_STOP, "ServiceStop");
                break;
            case R.id.catch_start:
                sendMessage(AppData.MSG_PATH_HEART_BEAT_START, "CatchStart");
                break;
            case R.id.catch_end:
                sendMessage(AppData.MSG_PATH_HEART_BEAT_STOP, "CatchStop");
                break;
            case R.id.clean_data:
                sendMessage(AppData.MSG_PATH_FILE, "Delete");
                break;
            case R.id.get_data:
                sendMessage(AppData.MSG_PATH_FILE, "Get");
                break;
            default:
                break;
        }
    }

    /**
     * Use MessageApi to send message
     *
     * @param msgPath MessageApi Path
     * @param context The String message  to send
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
                                Toast.makeText(HeartBeatActivity.this, "Send Message Success"
                                        , Toast.LENGTH_SHORT).show();
                            } else {
                                Log.d(TAG, "sendMessage Fail:" + context + "->"
                                        + sendMessageResult.getStatus()
                                        .getStatusMessage());
                                Toast.makeText(HeartBeatActivity.this, "Send Message Fail"
                                        , Toast.LENGTH_SHORT).show();
                            }

                        }
                    });
        }
    }

    @Override
    protected void onStart() {
        //start connect the node you want
        mGoogleApiClient.connect();
        super.onStart();
    }

    @Override
    protected void onStop() {
        Log.d(TAG, "Activity onStop");
        Wearable.DataApi.removeListener(mGoogleApiClient, this);
        Wearable.NodeApi.removeListener(mGoogleApiClient, this);
        Wearable.MessageApi.removeListener(mGoogleApiClient, this);
        //if client is connected , release resource
        if ((null != mGoogleApiClient) && (mGoogleApiClient.isConnected())) {
            Log.d(TAG, "onStop,GoogleApiClient is disConnected");
            mGoogleApiClient.disconnect();
        }
        super.onStop();
    }

    @Override
    public void onCheckedChanged(RadioGroup group, int checkedId) {
        Log.d(TAG, "check changed:" + checkedId);
        switch (checkedId) {
            case R.id.mode_interval:
                sendMessage(AppData.MSG_PATH_MODE, "1");
                break;
            case R.id.mode_continuous:
                sendMessage(AppData.MSG_PATH_MODE, "2");
                break;
            default:
                break;
        }
    }

    @Override
    public void onDataChanged(DataEventBuffer dataEventBuffer) {
        for (DataEvent event : dataEventBuffer) {
            if (event.getType() == DataEvent.TYPE_DELETED) {
                Log.d(TAG, "data deleted");
            } else if (event.getType() == DataEvent.TYPE_CHANGED) {
                DataItem item = event.getDataItem();
                Log.d(TAG, "data changed:" + item.getUri().getPath());
                DataMap dataMap = DataMapItem.fromDataItem(item).getDataMap();
                switch (item.getUri().getPath()) {
                    case AppData.DATA_PATH_HEART_BEAT:
                        String data = dataMap.getString("data");
                        String time = dataMap.getString("time");
                        int mode = dataMap.getInt("mode");
                        mBeatData.append(time + " " + data + " " + mode + " " + "\n");
                        mScrollView.fullScroll(ScrollView.FOCUS_DOWN);
                        //                        mChart.updateChart(Integer.valueOf(data));
                        break;
                    case AppData.DATA_PATH_FILE:
                        final Asset asset = dataMap.getAsset("data");
                        if (null != asset) {
                            new Thread(new Runnable() {
                                String FileContext = "";
                                @Override
                                public void run() {
                                    try {
                                        int len = 0;
                                        InputStream assetInputStream = Wearable.DataApi.getFdForAsset(
                                                mGoogleApiClient, asset).await().getInputStream();
                                        byte[] buffer = new byte[255];
                                        while ((len = assetInputStream.read(buffer)) != -1) {
                                            FileContext += new String(buffer);
                                        }
                                        Log.d(TAG, "asset read end:\n" + FileContext);
                                        Message msg = new Message();
                                        msg.what = 2;
                                        Bundle bd = new Bundle();
                                        bd.putString("file", FileContext);
                                        msg.setData(bd);
                                        mHandle.sendMessage(msg);
                                    } catch (Exception e) {
                                        Log.d(TAG, "thread exception:" + e.toString());
                                    }
                                }
                            }).start();
                        } else {
                            Log.d(TAG, "file context is null");
                        }

                        break;
                    default:
                        break;
                }

            } else {
                Log.d(TAG, "data unknown:" + event.getType());
            }
        }
    }

    @Override
    public void onConnected(final Bundle bundle) {
        Log.d(TAG, "onConnected:" + bundle);
        mClientSwitch.setChecked(true);
        mClientSwitch.setText(getResources().getText(R.string.connected));
        //register client listener
        Wearable.DataApi.addListener(mGoogleApiClient, this);
        Wearable.MessageApi.addListener(mGoogleApiClient, this);
        Wearable.NodeApi.addListener(mGoogleApiClient, this);
        new Thread(new Runnable() {
            @Override
            public void run() {
                mConnectedNodes = AppTool.getNodes(mGoogleApiClient);
                if (mConnectedNodes.size() > 0) {
                    String showInfo = "";
                    for (Node node : mConnectedNodes) {
                        showInfo += "ID:" + node.getId() + " Name:" + node.getDisplayName();
                        Log.d(TAG, showInfo);
                    }
                    Message msg = new Message();
                    msg.what = 1;
                    Bundle bd = new Bundle();
                    bd.putString("name", mConnectedNodes.get(0).getDisplayName());
                    bd.putString("id", mConnectedNodes.get(0).getId());
                    msg.setData(bd);
                    mHandle.sendMessage(msg);
                    sendMessage(AppData.MSG_PATH_SERVICE_STATUES, "statues");
                } else {
                    Log.d(TAG, "No Connected Nodes");
                }
            }
        }).start();
    }

    @Override
    public void onConnectionSuspended(int i) {
        Log.d(TAG, "onConnectionSuspend:" + i);
        mNodeSwitch.setChecked(false);
        mNodeSwitch.setText(getResources().getText(R.string.suspend));
    }

    @Override
    public void onConnectionFailed(ConnectionResult connectionResult) {
        Log.d(TAG, "ConnectionResult:" + connectionResult.getErrorCode());
        mNodeSwitch.setChecked(false);
        mNodeSwitch.setText(getResources().getText(R.string.disconnected));
    }

    @Override
    public void onMessageReceived(MessageEvent messageEvent) {
        String msgInfo = new String(messageEvent.getData());
        Log.d(TAG, "onMessageReceived:" + messageEvent.getPath() + " Msg:" + msgInfo);
        if (messageEvent.getPath().equals(AppData.MSG_PATH_HEART_BEAT_RES)) {
            switch (msgInfo) {
                case "disable":
                    setSwitch(R.id.service_statues, true);
                    setSwitch(R.id.catch_statues, false);
                    mServiceStop.setEnabled(true);
                    mCatchStart.setEnabled(true);
                    mCatchStop.setEnabled(false);
                    mModeContinuous.setEnabled(true);
                    mModeInterval.setEnabled(true);
                    mCleanData.setEnabled(true);
                    mGetData.setEnabled(true);
                    break;
                case "service":
                    setSwitch(R.id.service_statues, false);
                    setSwitch(R.id.catch_statues, false);
                    mCatchStart.setEnabled(false);
                    mCatchStop.setEnabled(false);
                    mServiceStop.setEnabled(false);
                    mModeContinuous.setEnabled(false);
                    mModeInterval.setEnabled(false);
                    mCleanData.setEnabled(false);
                    mGetData.setEnabled(false);
                    break;
                case "delete":
                    mBeatData.append("Clean Data Success" + "\n");
                    mScrollView.fullScroll(ScrollView.FOCUS_DOWN);
                    break;
                case "enable":
                    setSwitch(R.id.service_statues, true);
                    setSwitch(R.id.catch_statues, true);
                    mServiceStop.setEnabled(true);
                    mCatchStart.setEnabled(false);
                    mCatchStop.setEnabled(true);

                    mModeContinuous.setEnabled(false);
                    mModeInterval.setEnabled(false);
                    mCleanData.setEnabled(false);
                    mGetData.setEnabled(false);
                    break;
                default:
                    break;
            }
        } else if (messageEvent.getPath().equals(AppData.MSG_PATH_MODE)) {
            switch (msgInfo) {
                case "1":
                    mModeInterval.setChecked(true);
                    break;
                case "2":
                    mModeContinuous.setChecked(true);
                    break;
                default:
                    break;
            }
        }
    }

    /**
     * set switch show checkable and text
     *
     * @param in   the id of view
     * @param flag true or false
     */
    private void setSwitch(int in, boolean flag) {
        switch (in) {
            case R.id.catch_statues:
                if (flag) {
                    mCatchStatues.setChecked(true);
                    mCatchStatues.setText(getResources().getText(R.string.enable));
                } else {
                    mCatchStatues.setChecked(false);
                    mCatchStatues.setText(getResources().getText(R.string.disable));
                }
                break;
            case R.id.service_statues:
                if (flag) {
                    mServiceStatues.setChecked(true);
                    mServiceStatues.setText(getText(R.string.enable));
                } else {
                    mServiceStatues.setChecked(false);
                    mServiceStatues.setText(getText(R.string.disable));
                }
                break;
            case R.id.node_statues:
                if (flag) {
                    mNodeSwitch.setChecked(true);
                    mNodeSwitch.setText(R.string.connected);
                } else {
                    mNodeSwitch.setChecked(false);
                    mNodeSwitch.setText(R.string.disconnected);
                }
        }
    }

    @Override
    public void onPeerConnected(Node node) {
        Log.d(TAG, "onPeerConnected:" + node.getDisplayName());
        setSwitch(R.id.node_statues, true);
    }

    @Override
    public void onPeerDisconnected(Node node) {
        Log.d(TAG, "onPeerDisconnected:" + node.getDisplayName());
        setSwitch(R.id.node_statues, false);
    }

}
