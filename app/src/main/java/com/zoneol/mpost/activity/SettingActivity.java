package com.zoneol.mpost.activity;

import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.ListView;

import com.popsecu.sdk.Controller;
import com.popsecu.sdk.Misc;
import com.zoneol.mpost.R;

import java.util.ArrayList;

public class SettingActivity extends AppCompatActivity implements AdapterView.OnItemClickListener{

    private ListView sListView;
    private ArrayList<String> sList = new ArrayList<>();
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        this.setTitle(R.string.main_tab_setting);
        setContentView(R.layout.activity_setting);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setDisplayShowHomeEnabled(false);
        init();
    }

    public void init() {
        sList.add(Controller.getInstance().getTreeInfoImp().getMHwTreeInfo().name) ;
        sList.add(Controller.getInstance().getTreeInfoImp().getAppTreeInfo().name) ;
        Misc.logd("treeName:" + Controller.getInstance().getTreeInfoImp().getMHwTreeInfo().name +", appName:" + Controller.getInstance().getTreeInfoImp().getAppTreeInfo().name);
        sListView = (ListView)findViewById(R.id.setting_listView) ;
        ArrayAdapter<String> adapter = new ArrayAdapter<>(
                this,
                android.R.layout.simple_expandable_list_item_1,
                sList);
        sListView.setAdapter(adapter);
        sListView.setOnItemClickListener(this);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_setting, menu);
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
        }

        return super.onOptionsItemSelected(item);
    }

    @Override
    public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
        if (position == 0) {
            Intent intent = new Intent(this , SettingExpandableListViewActivity.class) ;
            startActivity(intent);
        } else if(position == 1) {
            Intent intent = new Intent(this , SettingAppActivity.class) ;
            startActivity(intent);
        } else {

        }
    }
}
