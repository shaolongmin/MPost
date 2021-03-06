package com.zoneol.mpost.activity;

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
import com.popsecu.sdk.Event;
import com.popsecu.sdk.EventCenter;
import com.zoneol.mpost.R;
import com.zoneol.mpost.fragment.KeyValueDialogFragment;
import com.zoneol.mpost.fragment.SettingAppDialogFragment;

import java.util.ArrayList;

public class DealActivity extends AppCompatActivity implements AdapterView.OnItemClickListener , EventCenter.Receiver ,KeyValueDialogFragment.KeyValueListener {
    private ListView sListView;
//    private ArrayList<String> sList = new ArrayList<>();

    private SettingAppDialogFragment dialog = null ;
    private String[] sList = {"消费","查询余额"} ;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        this.setTitle(R.string.main_tab_deal);
        setContentView(R.layout.activity_deal);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setDisplayShowHomeEnabled(false);
        init();
        EventCenter.getInstance().register(this);
    }

    public void init() {
        sListView = (ListView)findViewById(R.id.deal_listView) ;
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
        getMenuInflater().inflate(R.menu.menu_deal, menu);
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
            ArrayList<String> arrayStrList = new ArrayList<>() ;
            FragmentManager fm = getSupportFragmentManager() ;
            KeyValueDialogFragment dialog = KeyValueDialogFragment.newInstance(true , position , arrayStrList ,false) ;
            dialog.show(fm, "");
        } else if (position == 1) {
            CommInteface.getInstance().getBalance();

            FragmentManager fm = getSupportFragmentManager() ;
            dialog = SettingAppDialogFragment.newInstance(1 , 0 , "") ;
            dialog.show(fm, "");
        } else {
            return;
        }


    }

    @Override
    public void onEvent(Event event) {
        Event.EventType type = event.getType() ;
        if (type == Event.EventType.BLE_STATUS_CHANGED) {
            //String result = event.getStringParam() ;
            //FragmentManager fm = getSupportFragmentManager() ;
            //SettingAppDialogFragment dialog = SettingAppDialogFragment.newInstance(2 , 0 , result) ;
            //dialog.show(fm, "");
        } else if (type == Event.EventType.SALE) {
            if (dialog != null) {
                dialog.dismiss();
                dialog = null;
            }

            Toast.makeText(this, "交易成功", Toast.LENGTH_LONG).show();
        } else if (type == Event.EventType.SELECT) {
            if (dialog != null) {
                dialog.dismiss();
                dialog = null;
            }

            if (event.getIntParam() == 0) {
                String result = "余额： " + new String((byte[])event.getObjectParam());
                FragmentManager fm = getSupportFragmentManager() ;
                SettingAppDialogFragment notify = SettingAppDialogFragment.newInstance(2, 0, result) ;
                notify.show(fm, "");
            } else {
                Toast.makeText(this, "查询失败", Toast.LENGTH_SHORT).show();
            }


        }
    }

    @Override
    public void onKeyValueListener(int position, String value, boolean isKey) {
        Toast.makeText(this , "result:" + value , Toast.LENGTH_SHORT).show();
        if (position == 0) {
            CommInteface.getInstance().sale(value);

            FragmentManager fm = getSupportFragmentManager() ;
            dialog = SettingAppDialogFragment.newInstance(1 , 0 , "") ;
            dialog.show(fm, "");
        }
    }
}
