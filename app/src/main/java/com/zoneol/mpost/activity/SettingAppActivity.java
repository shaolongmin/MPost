package com.zoneol.mpost.activity;

import android.os.Bundle;
import android.support.v4.app.FragmentManager;
import android.support.v7.app.AppCompatActivity;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ListView;

import com.popsecu.sdk.CfgInfo;
import com.popsecu.sdk.Controller;
import com.popsecu.sdk.Misc;
import com.zoneol.mpost.R;
import com.zoneol.mpost.fragment.KeyValueDialogFragment;
import com.zoneol.mpost.fragment.SettingAppDialogFragment;

import java.util.ArrayList;
import java.util.List;

public class SettingAppActivity extends AppCompatActivity implements AdapterView.OnItemLongClickListener , KeyValueDialogFragment.KeyValueListener , SettingAppDialogFragment.AppToastListener {

    private List<CfgInfo.CfgKeyValue> list = new ArrayList<>();
    private SettingKeyValueAdapter mSettingKeyValueAdapter ;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_setting_app);
        this.setTitle("APP");
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setDisplayShowHomeEnabled(false);
        init();
    }

        public void init() {

        list = Controller.getInstance().getTreeInfoImp().getAppTreeInfo().keyValueList ;

        ListView listView = (ListView)findViewById(R.id.setting_app_listview) ;

        mSettingKeyValueAdapter = new SettingKeyValueAdapter(this , list , true) ;

        listView.setAdapter(mSettingKeyValueAdapter);
        listView.setOnItemLongClickListener(this);
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
            addAppItem();
            return true ;
        }

        return super.onOptionsItemSelected(item);
    }

    public void addAppItem() {
        Controller.getInstance().getTreeInfoImp().addAppKv();
        mSettingKeyValueAdapter.notifyDataSetChanged();
    }

    @Override
    public boolean onItemLongClick(AdapterView<?> parent, View view, int position, long id) {
        Misc.logd("app long click");
        FragmentManager fm = getSupportFragmentManager() ;
        SettingAppDialogFragment dialog = SettingAppDialogFragment.newInstance(0 , position) ;
        dialog.show(fm, "");
        return false;
    }

    @Override
    public void onKeyValueListener(int position, String value, boolean isKey) {
        if (isKey) {
            Controller.getInstance().getTreeInfoImp().updateAppKvItem(position , value);
            mSettingKeyValueAdapter.notifyDataSetChanged();
        } else {
            list.get(position).defaultValue = value ;
            mSettingKeyValueAdapter.notifyDataSetChanged();
        }
    }

    @Override
    public void onAppToastListener(int position) {
        Controller.getInstance().getTreeInfoImp().delAppKv(position);
        mSettingKeyValueAdapter.notifyDataSetChanged();
    }
}
