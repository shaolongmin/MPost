package com.zoneol.mpost.activity;

import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.ListView;

import com.popsecu.sdk.CfgInfo;
import com.popsecu.sdk.Controller;
import com.zoneol.mpost.R;
import com.zoneol.mpost.fragment.KeyValueDialogFragment;

import java.util.ArrayList;
import java.util.List;

public class SettingKeyValueActivity extends AppCompatActivity implements KeyValueDialogFragment.KeyValueListener{
    public static final String GROUP_ID = "group_id" ;
    public static final String CHILD_ID = "child_id" ;
    public static final String KEYVALUE_NAME = "keyvalue_name" ;

    private List<CfgInfo.CfgKeyValue> list = new ArrayList<>();
    private SettingKeyValueAdapter mSettingKeyValueAdapter ;

    private int groupId = -1 ;
    private int childId = -1 ;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        this.setTitle("KeyValue设置");
        setContentView(R.layout.activity_setting_key_value);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setDisplayShowHomeEnabled(false);
        init();
    }

    public void init() {
        groupId = getIntent().getIntExtra(GROUP_ID , -1) ;
        childId = getIntent().getIntExtra(CHILD_ID , -1) ;

        if (groupId == -1 || childId == -1) {
            return ;
        }

        String title = getIntent().getStringExtra(KEYVALUE_NAME) ;
        this.setTitle(title);
        list = Controller.getInstance().getTreeInfoImp().getMHwTreeInfo().childList.get(groupId).childList.get(childId).keyValueList ;

        ListView listView = (ListView)findViewById(R.id.setting_keyvalue_listview) ;

        mSettingKeyValueAdapter = new SettingKeyValueAdapter(this , list , false) ;

        listView.setAdapter(mSettingKeyValueAdapter);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_setting_key_value, menu);
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
    public void onKeyValueListener(int position, String value , boolean isKey) {
        list.get(position).defaultValue = value ;
        mSettingKeyValueAdapter.notifyDataSetChanged();
    }
}
