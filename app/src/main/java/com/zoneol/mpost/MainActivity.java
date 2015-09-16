package com.zoneol.mpost;

import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;

import com.popsecu.sdk.Controller;
import com.popsecu.sdk.Event;
import com.popsecu.sdk.EventCenter;
import com.zoneol.mpost.activity.DealActivity;
import com.zoneol.mpost.activity.SettingActivity;

public class MainActivity extends AppCompatActivity implements View.OnClickListener , EventCenter.Receiver{

    private Menu main_menu_status ;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        this.setTitle("POST");
        setContentView(R.layout.activity_main);
        Controller.getInstance().initThreeInfo(this);
        EventCenter.getInstance().register(this);
        init();
    }

    private void init() {
        findViewById(R.id.main_tab_deal).setOnClickListener(this);
        findViewById(R.id.main_tab_setting).setOnClickListener(this);
        findViewById(R.id.main_tab_update).setOnClickListener(this);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_main, menu);
        main_menu_status = menu ;
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        return super.onOptionsItemSelected(item);
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        EventCenter.getInstance().unregister(this);
    }

    @Override
    public void onClick(View v) {
        int id = v.getId() ;
        if (id == R.id.main_tab_deal) {
            Intent intent = new Intent(this , DealActivity.class) ;
            startActivity(intent);
        } else if (id == R.id.main_tab_setting) {
            Intent intent = new Intent(this , SettingActivity.class) ;
            startActivity(intent);
        } else if (id == R.id.main_tab_update) {

        }
    }

    @Override
    public void onEvent(Event event) {
        Event.EventType type = event.getType() ;
        if (type == Event.EventType.BLE_STATUS_CHANGED) {
            int parm = event.getIntParam() ;
            if (parm == 0) {
//                main_menu_status.findItem(R.id.main_menu_status).setIcon(R.drawable.icon_device) ;
            } else {
//                main_menu_status.findItem(R.id.main_menu_status).setIcon(R.drawable.icon_device_disconnect);
            }
        }
    }
}
