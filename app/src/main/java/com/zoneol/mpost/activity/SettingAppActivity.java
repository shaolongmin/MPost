package com.zoneol.mpost.activity;

import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.ListView;

import com.popsecu.sdk.CfgInfo;
import com.popsecu.sdk.Controller;
import com.zoneol.mpost.R;

import java.util.ArrayList;
import java.util.List;

public class SettingAppActivity extends AppCompatActivity {

    private List<CfgInfo.CfgKeyValue> list = new ArrayList<>();
    private SettingKeyValueAdapter mSettingKeyValueAdapter ;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_setting_app);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setDisplayShowHomeEnabled(false);
        init();
    }

        public void init() {

        list = Controller.getInstance().getTreeInfoImp().getAppTreeInfo().keyValueList ;

        ListView listView = (ListView)findViewById(R.id.setting_app_listview) ;

        mSettingKeyValueAdapter = new SettingKeyValueAdapter(this , list , true) ;

        listView.setAdapter(mSettingKeyValueAdapter);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_setting_app, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        if (id == android.R.id.home) {
            finish();
            return true;
        } else if (id == R.id.app_add) {
            return true ;
        }

        return super.onOptionsItemSelected(item);
    }
}
