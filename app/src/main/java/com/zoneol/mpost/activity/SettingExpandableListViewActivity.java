package com.zoneol.mpost.activity;

import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.ExpandableListView;

import com.popsecu.sdk.Controller;
import com.zoneol.mpost.R;

public class SettingExpandableListViewActivity extends AppCompatActivity {

    private ExpandableListView mSettingExpandableListView ;
    private SettingExpandableAdapter mSettingExpandableAdapter ;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        this.setTitle(Controller.getInstance().getTreeInfoImp().getMHwTreeInfo().name);
        setContentView(R.layout.activity_setting_expandable_list_view);
        init();
    }

    public void init() {
        mSettingExpandableListView = (ExpandableListView)findViewById(R.id.setting_expandablelistView) ;
        mSettingExpandableAdapter = new SettingExpandableAdapter(this) ;
        mSettingExpandableListView.setAdapter(mSettingExpandableAdapter);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_setting_expandable_list_view, menu);
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
}
