package com.alvin.health;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.support.wearable.view.WatchViewStub;
import android.util.Log;
import android.widget.TextView;

import com.alvin.health.service.HearBeatService;

public class WearMainActivity extends Activity {

    private TextView mButtonView;
    private final String TAG = "Alvin";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_wear_main);
        final WatchViewStub stub = (WatchViewStub) findViewById(R.id.watch_view_stub);
        stub.setOnLayoutInflatedListener(new MyLayoutInflatedListener());
    }

    private class MyLayoutInflatedListener implements WatchViewStub.OnLayoutInflatedListener {
        @Override
        public void onLayoutInflated(WatchViewStub watchViewStub) {
            mButtonView = (TextView) watchViewStub.findViewById(R.id.text);
            Intent it = new Intent(WearMainActivity.this, HearBeatService.class);
            startService(it);
            Log.d(TAG, "Start Service");
        }
    }

    @Override
    protected void onStart() {
        super.onStart();
    }

    @Override
    protected void onStop() {
        super.onStop();
    }
}
