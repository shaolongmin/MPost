package com.zoneol.mpost.activity;

import android.content.Intent;
import android.os.Bundle;
import android.support.v4.app.FragmentManager;
import android.support.v7.app.AppCompatActivity;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.ListView;
import android.widget.Toast;

import com.popsecu.sdk.CommInteface;
import com.popsecu.sdk.Controller;
import com.popsecu.sdk.Event;
import com.popsecu.sdk.EventCenter;
import com.popsecu.sdk.Misc;
import com.popsecu.sdk.TreeInfoImp;
import com.zoneol.mpost.R;
import com.zoneol.mpost.fragment.SettingAppDialogFragment;

import java.util.ArrayList;

public class SettingActivity extends AppCompatActivity implements AdapterView.OnItemClickListener, EventCenter.Receiver{

    private ListView sListView;
    private ArrayList<String> sList = new ArrayList<>();
    private SettingAppDialogFragment dialog = null ;
    private FragmentManager fm = null ;
    private ArrayAdapter<String> adapter ;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        this.setTitle(R.string.main_tab_setting);
        setContentView(R.layout.activity_setting);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setDisplayShowHomeEnabled(false);
        EventCenter.getInstance().register(this);
        init();
    }

    public void init() {
        if (!Controller.getInstance().getTreeInfoImp().getMHwTreeInfo().name.isEmpty()) {
            sList.add(Controller.getInstance().getTreeInfoImp().getMHwTreeInfo().name) ;
            sList.add(Controller.getInstance().getTreeInfoImp().getAppTreeInfo().name) ;
        }

        sList.add(Controller.getInstance().getTreeInfoImp().getAppTreeInfo().name) ;
        Misc.logd("treeName:" + Controller.getInstance().getTreeInfoImp().getMHwTreeInfo().name +", appName:" + Controller.getInstance().getTreeInfoImp().getAppTreeInfo().name);
        sListView = (ListView)findViewById(R.id.setting_listView) ;
        adapter = new ArrayAdapter<>(
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
        } else if (id == R.id.setting_menu_get) {
            //setting get
            if (dialog == null ) {
                fm = getSupportFragmentManager() ;
                dialog = SettingAppDialogFragment.newInstance(1 , 0 , "") ;
            }
            dialog.show(fm, "");
            CommInteface.getInstance().getUserConfig();
        } else if (id == R.id.setting_menu_upload) {
            //setting load
            if (dialog == null ) {
                fm = getSupportFragmentManager() ;
                dialog = SettingAppDialogFragment.newInstance(1 , 0 , "") ;
            }
            dialog.show(fm, "");
            TreeInfoImp imp = Controller.getInstance().getTreeInfoImp() ;
            if (imp.serializationAllCfg() == null) {
                Toast.makeText(this , "load 失败" , Toast.LENGTH_SHORT).show();
                dialog.dismiss();
                return true;
            }
            CommInteface.getInstance().setUserConfig(imp.serializationAllCfg());
        }

        return super.onOptionsItemSelected(item);
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        EventCenter.getInstance().unregister(this);
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

    @Override
    public void onEvent(Event event) {
        Event.EventType type = event.getType() ;
        if (type == Event.EventType.GET_USER_CFG) {
            if (dialog != null) {
                dialog.dismiss();
            }
            int parm = event.getIntParam() ;
            if (parm == 0) {
                byte[] result = (byte[])event.getObjectParam() ;
                Controller.getInstance().getTreeInfoImp().loadCfgFromDev(result) ;
                if (!Controller.getInstance().getTreeInfoImp().getMHwTreeInfo().name.isEmpty()) {
                    sList.clear();
                    sList.add(Controller.getInstance().getTreeInfoImp().getMHwTreeInfo().name) ;
                    sList.add(Controller.getInstance().getTreeInfoImp().getAppTreeInfo().name) ;
                }
                adapter.notifyDataSetChanged();
            } else {
                Toast.makeText(this , "upload 失败" , Toast.LENGTH_SHORT).show();
            }
        } else if (type ==Event.EventType.SET_USER_CFG ) {
            if (dialog != null) {
                dialog.dismiss();
            }
            Toast.makeText(this , "set成功" , Toast.LENGTH_SHORT).show();
        }
    }
}
