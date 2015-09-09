package com.zoneol.mpost.activity;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.TextView;

import com.popsecu.sdk.CfgInfo;
import com.zoneol.mpost.R;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by GTW on 2015/9/9.
 */
public class SettingKeyValueAdapter extends BaseAdapter implements View.OnClickListener{

    private Context mContext ;
    private List<CfgInfo.CfgKeyValue> mList = new ArrayList<>();

    public SettingKeyValueAdapter (Context context , List<CfgInfo.CfgKeyValue> list) {
        this.mContext = context ;
        this.mList = list ;
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
            viewHolder.keyvalue_item_key = (TextView)convertView.findViewById(R.id.setting_keyvalue_key) ;
            viewHolder.keyvalue_item_value = (TextView)convertView.findViewById(R.id.setting_keyvalue_value) ;
            convertView.setTag(viewHolder);
        } else {
            viewHolder = (ViewHolder) convertView.getTag();
        }

        CfgInfo.CfgKeyValue keyValue = mList.get(position) ;
        viewHolder.keyvalue_item_key.setText(keyValue.keyName);
        viewHolder.keyvalue_item_value.setText(keyValue.defaultValue);

        viewHolder.keyvalue_item_key.setTag(position);
        viewHolder.keyvalue_item_key.setOnClickListener(this);
        viewHolder.keyvalue_item_value.setTag(position);
        viewHolder.keyvalue_item_value.setOnClickListener(this);

        return convertView;
    }

    @Override
    public void onClick(View v) {
        int id = v.getId() ;
        if (id == R.id.setting_keyvalue_key) {

        } else {

        }
    }

    private class ViewHolder{
        private TextView keyvalue_item_key ;
        private TextView keyvalue_item_value ;
    }
}
