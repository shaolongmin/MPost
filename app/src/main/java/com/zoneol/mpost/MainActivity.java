package com.zoneol.mpost;

import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;

import com.popsecu.sdk.Controller;
import com.zoneol.mpost.activity.DealActivity;
import com.zoneol.mpost.activity.SettingActivity;

public class MainActivity extends AppCompatActivity implements View.OnClickListener{

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        this.setTitle("POST");
        setContentView(R.layout.activity_main);
        Controller.getInstance().initThreeInfo(this);
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
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        if (id == R.id.action_settings) {
            return true;
        }

        return super.onOptionsItemSelected(item);
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
}
