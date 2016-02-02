package com.alvin.health;

import android.os.Bundle;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.ListView;

public class HomeActivity extends AppCompatActivity implements View.OnClickListener{
    private DrawerLayout mDrawerLayout;
    private ListView mDrawerList;
    private String[] mItemsString = {"Heart Beat", "Camera Show"};
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_home);
        mDrawerLayout = (DrawerLayout) findViewById(R.id.drawer_layout);
        mDrawerList = (ListView) findViewById(R.id.left_drawer);
       // set up the drawer's list view with items and click listener
      mDrawerList.setAdapter(new ArrayAdapter<String>(this, R.layout.list_item_home_left,mItemsString));
    }

    @Override
    public void onClick(View v) {
//        switch (v.getId()){
//            case R.id.heart_beat:
//                startActivity(new Intent(HomeActivity.this,PhoneMainActivity.class));
//                finish();
//                break;
//            default:
//                break;
//        }
    }
}
