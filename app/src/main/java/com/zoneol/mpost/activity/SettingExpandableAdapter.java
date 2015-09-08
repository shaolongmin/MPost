package com.zoneol.mpost.activity;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseExpandableListAdapter;
import android.widget.TextView;

import com.popsecu.sdk.CfgInfo;
import com.popsecu.sdk.Controller;
import com.zoneol.mpost.R;

/**
 * Created by GTW on 2015/9/8.
 */
public class SettingExpandableAdapter extends BaseExpandableListAdapter {

    private int groupPosition;
    private int childPosition;
    private Controller mController ;
    private CfgInfo.TreeInfo mTreeInfo ;

    private Context context = null ;
    public SettingExpandableAdapter(Context context) {
        this.context = context ;
        mController = Controller.getInstance() ;
        mTreeInfo = mController.getTreeInfoImp().getMHwTreeInfo() ;
    }

    public void setPosition(int groupPosition,int childPosition){
        this.groupPosition = groupPosition;
        this.childPosition = childPosition;
    }

    @Override
    public int getGroupCount() {
        return mTreeInfo.childList.size();
    }

    @Override
    public int getChildrenCount(int groupPosition) {
        return mTreeInfo.childList.get(groupPosition).childList.size();
    }

    @Override
    public Object getGroup(int groupPosition) {
        return mTreeInfo.childList.get(groupPosition);
    }

    @Override
    public Object getChild(int groupPosition, int childPosition) {
        return mTreeInfo.childList.get(groupPosition).childList.get(childPosition);
    }

    @Override
    public long getGroupId(int groupPosition) {
        return groupPosition;
    }

    @Override
    public long getChildId(int groupPosition, int childPosition) {
        return childPosition;
    }

    @Override
    public boolean hasStableIds() {
        return true;
    }

    @Override
    public View getGroupView(int groupPosition, boolean isExpanded, View convertView, ViewGroup parent) {
        convertView= LayoutInflater.from(context).inflate(R.layout.group_layout, null);
        TextView textView=(TextView) convertView.findViewById(R.id.mTV);
        textView.setText(mTreeInfo.childList.get(groupPosition).name);
        convertView.setTag(R.id.group_position, groupPosition);
        convertView.setTag(R.id.child_position, -1);
        return convertView;
    }

    @Override
    public View getChildView(int groupPosition, int childPosition, boolean isLastChild, View convertView, ViewGroup parent) {
        convertView =LayoutInflater.from(context).inflate(R.layout.children_layout, null);

        TextView textView = (TextView) convertView.findViewById(R.id.mchildrenTV);
        textView.setText(mTreeInfo.childList.get(groupPosition).childList.get(childPosition).name);

        convertView.setTag(R.id.group_position, groupPosition);
        convertView.setTag(R.id.child_position, childPosition);
        return convertView;
    }

    @Override
    public boolean isChildSelectable(int groupPosition, int childPosition) {
        return false;
    }
}
