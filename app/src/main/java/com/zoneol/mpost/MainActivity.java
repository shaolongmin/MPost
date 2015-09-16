package com.zoneol.mpost;

import android.content.ActivityNotFoundException;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Toast;

import com.popsecu.sdk.CommInteface;
import com.popsecu.sdk.Controller;
import com.popsecu.sdk.Event;
import com.popsecu.sdk.EventCenter;
import com.popsecu.sdk.Misc;
import com.zoneol.mpost.activity.DealActivity;
import com.zoneol.mpost.activity.SettingActivity;

public class MainActivity extends AppCompatActivity implements View.OnClickListener , EventCenter.Receiver{

    private MenuItem main_menu_status ;
    private static final int REQUEST_CODE = 1 ;

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
        main_menu_status = menu.findItem(R.id.main_menu_status);

        if (CommInteface.getInstance().getBleStatus() == 1) {
            main_menu_status.setIcon(R.drawable.icon_device) ;
        } else {
            main_menu_status.setIcon(R.drawable.icon_device_disconnect) ;
        }


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

    private void showChooser() {
        // Use the GET_CONTENT intent from the utility class
        Intent intent = new Intent(Intent.ACTION_GET_CONTENT);
        // The MIME data type filter
        intent.setType("*/*");
        // Only return URIs that can be opened with ContentResolver
        intent.addCategory(Intent.CATEGORY_OPENABLE);
        try {
//            startActivityForResult(intent, REQUEST_CODE);
            startActivityForResult(Intent.createChooser(intent, "请选择一个文件进行更新"),
                    REQUEST_CODE);
        } catch (ActivityNotFoundException e) {
            // The reason for the existence of aFileChooser
            Toast.makeText(this, "请安装文件管理器", Toast.LENGTH_SHORT)
                    .show();
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        switch (requestCode) {
            case REQUEST_CODE:
                // If the file selection was successful
                if (resultCode == RESULT_OK) {
                    if (data != null) {
                        // Get the URI of the selected file
                        final Uri uri = data.getData();
                        Misc.logd("get uri:" +uri);
                        String realPath = Misc.getRealFilePath(this , uri) ;
                        Toast.makeText(this , "路径：" + realPath , Toast.LENGTH_SHORT).show();
                        Misc.logd("realPath:" + realPath);
                    }
                }
                break;
        }
        super.onActivityResult(requestCode, resultCode, data);
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
            showChooser() ;
        }
    }

    @Override
    public void onEvent(Event event) {
        Event.EventType type = event.getType() ;
        if (type == Event.EventType.BLE_STATUS_CHANGED) {
            int parm = event.getIntParam() ;
            if (main_menu_status == null) {
                Misc.logd("menu is not ok" );
                return ;
            }
            if (parm == 1) {
                main_menu_status.setIcon(R.drawable.icon_device) ;
            } else {
                main_menu_status.setIcon(R.drawable.icon_device_disconnect);
            }
        }
    }
}
