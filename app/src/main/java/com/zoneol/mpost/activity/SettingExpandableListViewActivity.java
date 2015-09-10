package com.zoneol.mpost.activity;

import android.content.Intent;
import android.os.Bundle;
import android.support.v4.app.FragmentManager;
import android.support.v7.app.AppCompatActivity;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ExpandableListView;

import com.popsecu.sdk.Controller;
import com.popsecu.sdk.Misc;
import com.zoneol.mpost.R;
import com.zoneol.mpost.fragment.SettingSelectDialogFragment;

public class SettingExpandableListViewActivity extends AppCompatActivity implements AdapterView.OnItemLongClickListener , SettingSelectDialogFragment.SelectListener , AdapterView.OnItemClickListener , ExpandableListView.OnChildClickListener{

    private ExpandableListView mSettingExpandableListView ;
    private SettingExpandableAdapter mSettingExpandableAdapter ;

    private int mSettingGroupId ;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        this.setTitle(Controller.getInstance().getTreeInfoImp().getMHwTreeInfo().name);
        setContentView(R.layout.activity_setting_expandable_list_view);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setDisplayShowHomeEnabled(false);
        init();
    }

    public void init() {
        mSettingExpandableListView = (ExpandableListView)findViewById(R.id.setting_expandablelistView) ;
        mSettingExpandableAdapter = new SettingExpandableAdapter(this) ;
        mSettingExpandableListView.setAdapter(mSettingExpandableAdapter);
        mSettingExpandableListView.setOnItemLongClickListener(this);
        mSettingExpandableListView.setOnItemClickListener(this);
        mSettingExpandableListView.setOnChildClickListener(this);
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
        } else if (id == android.R.id.home) {
            finish();
            return true ;
        }

        return super.onOptionsItemSelected(item);
    }

    public void addItem() {
        FragmentManager fm = getSupportFragmentManager() ;
        SettingSelectDialogFragment dialog = SettingSelectDialogFragment.newInstance(SettingSelectDialogFragment.TYPE_PARENT_ADD , "aditem") ;
        dialog.show(fm, "additem");
    }

    @Override
    public boolean onItemLongClick(AdapterView<?> parent, View view, int position, long id) {
        int groupid = (Integer) view.getTag(R.id.group_position);
        mSettingGroupId = groupid;
        int childid = (Integer) view.getTag(R.id.child_position);

        if(childid ==-1){
            String name = Controller.getInstance().getTreeInfoImp().getMHwTreeInfo().childList.get(position).name ;
            FragmentManager fm = getSupportFragmentManager() ;
            SettingSelectDialogFragment dialog = SettingSelectDialogFragment.newInstance(SettingSelectDialogFragment.TYPE_PARENT_LONG_ONCLICK , name) ;
            dialog.show(fm, name);
        }else{
            mSettingExpandableAdapter.setPosition(groupid, childid);
        }
        return true;
    }

    @Override
    public void onSelectListener(int type, int position , Object obj) {
            if (type == SettingSelectDialogFragment.TYPE_PARENT_LONG_ONCLICK) {
                if (position == 0) {
                    Misc.logd("删除该组");
                    Controller.getInstance().getTreeInfoImp().getMHwTreeInfo().childList.remove(mSettingGroupId) ;
                    mSettingExpandableAdapter.notifyDataSetChanged();
                } else {
                    Misc.logd("添加子项");
                    String name = (String)obj ;
                    Controller.getInstance().getTreeInfoImp().addClassInst(name) ;
                    mSettingExpandableAdapter.notifyDataSetChanged();
                }
            } else if (type == SettingSelectDialogFragment.TYPE_PARENT_ADD) {
                mSettingExpandableAdapter.notifyDataSetChanged();
            }
    }

    @Override
    public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
        int groupid = (Integer) view.getTag(R.id.group_position);
        int childid = (Integer) view.getTag(R.id.child_position);
        Misc.logd("expandlaListView onItemClick");
        if(childid ==-1){

        }else{
            Intent intent = new Intent(this , SettingKeyValueActivity.class) ;
            intent.putExtra(SettingKeyValueActivity.GROUP_ID ,groupid ) ;
            intent.putExtra(SettingKeyValueActivity.CHILD_ID , childid) ;
            startActivity(intent);
        }
    }

    @Override
    public boolean onChildClick(ExpandableListView parent, View v, int groupPosition, int childPosition, long id) {
        int groupid = (Integer) v.getTag(R.id.group_position);
        int childid = (Integer) v.getTag(R.id.child_position);
        Misc.logd("expandlaListView childe");
        if(childid ==-1){

        }else{
            String title = Controller.getInstance().getTreeInfoImp().getMHwTreeInfo().childList.get(groupid).childList.get(childid).name ;
            Intent intent = new Intent(this , SettingKeyValueActivity.class) ;
            intent.putExtra(SettingKeyValueActivity.GROUP_ID ,groupid ) ;
            intent.putExtra(SettingKeyValueActivity.CHILD_ID , childid) ;
            intent.putExtra(SettingKeyValueActivity.KEYVALUE_NAME , title) ;

            Misc.logd("groupid:" + groupid + ",childId:" + childid + ",name:" + title);
            startActivity(intent);
        }
        return true;
    }
}
