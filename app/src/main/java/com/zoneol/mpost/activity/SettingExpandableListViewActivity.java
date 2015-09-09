package com.zoneol.mpost.activity;

import android.support.v4.app.FragmentManager;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ExpandableListView;

import com.popsecu.sdk.CfgInfo;
import com.popsecu.sdk.Controller;
import com.popsecu.sdk.Misc;
import com.zoneol.mpost.R;
import com.zoneol.mpost.fragment.SettingSelectDialogFragment;

import java.util.ArrayList;
import java.util.List;

public class SettingExpandableListViewActivity extends AppCompatActivity implements AdapterView.OnItemLongClickListener , SettingSelectDialogFragment.SelectListener{

    private ExpandableListView mSettingExpandableListView ;
    private SettingExpandableAdapter mSettingExpandableAdapter ;

    private int mSettingGroupId ;

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

        if (id == R.id.setting_add) {
            addItem();
            return true;
        }

        return super.onOptionsItemSelected(item);
    }

    public void addItem() {
        CfgInfo.TreeInfo treeInfo = new CfgInfo.TreeInfo() ;
        List<CfgInfo.TreeInfo> lChildTreeList =  new ArrayList<>() ;
        for (int j = 0 ; j < 6 ; j ++) {
            CfgInfo.TreeInfo childTreeInfo = new CfgInfo.TreeInfo() ;
            childTreeInfo.name = "第"  + j + "个" ;
            List<CfgInfo.CfgKeyValue> keyValueList = new ArrayList<>();
            for (int k = 0 ; k < 6 ; k ++) {
                CfgInfo.CfgKeyValue keyValue = new CfgInfo.CfgKeyValue() ;
                keyValue.keyName = "keyName" + k ;
                keyValue.disName = "disName" + k ;
                keyValue.defaultValue = "default" + k ;
                keyValueList.add(keyValue) ;
            }
            childTreeInfo.keyValueList = keyValueList ;
            lChildTreeList.add(childTreeInfo) ;
        }

        treeInfo.childList = lChildTreeList ;
        treeInfo.name = "第" + "addItem" + "组" ;
        Controller.getInstance().getTreeInfoImp().getMHwTreeInfo().childList.add(treeInfo) ;
        mSettingExpandableAdapter.notifyDataSetChanged();
    }

    @Override
    public boolean onItemLongClick(AdapterView<?> parent, View view, int position, long id) {
        int groupid = (Integer) view.getTag(R.id.group_position);
        mSettingGroupId = groupid;
        int childid = (Integer) view.getTag(R.id.child_position);

        if(childid ==-1){
            FragmentManager fm = getSupportFragmentManager() ;
            SettingSelectDialogFragment dialog = SettingSelectDialogFragment.newInstance(SettingSelectDialogFragment.TYPE_PARENT_LONG_ONCLICK , "longOnclick") ;
            dialog.show(fm, "longOnclick");
        }else{
            mSettingExpandableAdapter.setPosition(groupid, childid);
        }
        return true;
    }

    @Override
    public void onSelectListener(int type, int position) {
            if (type == SettingSelectDialogFragment.TYPE_PARENT_LONG_ONCLICK) {
                if (position == 0) {
                    Misc.logd("删除该组");
                } else {
                    Misc.logd("添加子项");
                }
            } else if (type == SettingSelectDialogFragment.TYPE_PARENT_ADD) {

            }
    }
}
