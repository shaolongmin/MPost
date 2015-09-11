package com.zoneol.mpost.activity;

import android.content.Context;
import android.support.v4.app.FragmentManager;
import android.support.v7.app.AppCompatActivity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.Button;
import android.widget.LinearLayout;

import com.popsecu.sdk.CfgInfo;
import com.popsecu.sdk.Controller;
import com.popsecu.sdk.Misc;
import com.zoneol.mpost.R;
import com.zoneol.mpost.fragment.KeyValueDialogFragment;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by GTW on 2015/9/9.
 */
public class SettingKeyValueAdapter extends BaseAdapter implements View.OnClickListener{

    private Context mContext ;
    private List<CfgInfo.CfgKeyValue> mList = new ArrayList<>();
    private boolean mIsApp = false ;
    private AppCompatActivity activity ;

    public SettingKeyValueAdapter (Context context , List<CfgInfo.CfgKeyValue> list , boolean isApp) {
        this.mContext = context ;
        this.mList = list ;
        this.mIsApp = isApp ;
        if (context instanceof  SettingKeyValueActivity) {
            activity = (SettingKeyValueActivity)context ;
        } else if (context instanceof  SettingAppActivity) {
            activity = (SettingAppActivity)context ;
        }
    }

    @Override
    public int getCount() {
        return mList.size();
    }

    @Override
    public Object getItem(int position) {
        return mList.get(position);
    }

    @Override
    public long getItemId(int position) {
        return position;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        ViewHolder viewHolder = null;
        if (convertView == null) {
            viewHolder = new ViewHolder();
            convertView = LayoutInflater.from(mContext).inflate(R.layout.keyvalue_item_layout, null);
            viewHolder.keyvalue_item_key = (Button)convertView.findViewById(R.id.setting_keyvalue_key) ;
            viewHolder.keyvalue_item_value = (Button)convertView.findViewById(R.id.setting_keyvalue_value) ;
            viewHolder.keyvalue_item_linearn = (LinearLayout)convertView.findViewById(R.id.setting_keyvalue_linearn) ;
            convertView.setTag(viewHolder);
        } else {
            viewHolder = (ViewHolder) convertView.getTag();
        }

        CfgInfo.CfgKeyValue keyValue = mList.get(position) ;
        viewHolder.keyvalue_item_key.setText(keyValue.disName);
        viewHolder.keyvalue_item_value.setText(keyValue.defaultValue);

        viewHolder.keyvalue_item_key.setTag(position);
        viewHolder.keyvalue_item_key.setOnClickListener(this);
        viewHolder.keyvalue_item_value.setTag(position);
        viewHolder.keyvalue_item_value.setOnClickListener(this);

        if (mIsApp) {
            viewHolder.keyvalue_item_key.setEnabled(true);
        } else {
            viewHolder.keyvalue_item_key.setEnabled(false);
        }

        viewHolder.keyvalue_item_linearn.setOnLongClickListener(new View.OnLongClickListener() {
            @Override
            public boolean onLongClick(View v) {
                Misc.logd("linear long onclick");
                return false;
            }
        });

        return convertView;
    }

    @Override
    public void onClick(View v) {
        int id = v.getId() ;
        int position = (int) v.getTag() ;
        CfgInfo.CfgKeyValue keyValue = mList.get(position) ;
        if (id == R.id.setting_keyvalue_key) {
           List<String> list =  Controller.getInstance().getTreeInfoImp().getAppKeyList();
            ArrayList<String> arrayStrList = new ArrayList<>() ;
            for (String str : list)  {
                arrayStrList.add(str) ;
            }

            FragmentManager fm = activity.getSupportFragmentManager() ;
            KeyValueDialogFragment dialog = KeyValueDialogFragment.newInstance(false , position , arrayStrList ,true) ;
            dialog.show(fm, "");
        } else {
            ArrayList<String> arrayStrList = new ArrayList<>() ;
            for (String str : keyValue.valueList)  {
                arrayStrList.add(str) ;
            }
            if (keyValue.isValueEditable) {
                FragmentManager fm = activity.getSupportFragmentManager() ;
                KeyValueDialogFragment dialog = KeyValueDialogFragment.newInstance(true , position , arrayStrList ,false) ;
                dialog.show(fm, "");
            } else {
                FragmentManager fm = activity.getSupportFragmentManager() ;
                KeyValueDialogFragment dialog = KeyValueDialogFragment.newInstance(false , position , arrayStrList , false) ;
                dialog.show(fm, "");
            }
        }
    }

    private class ViewHolder{
        private Button keyvalue_item_key ;
        private Button keyvalue_item_value ;
        private LinearLayout keyvalue_item_linearn ;
    }
}
