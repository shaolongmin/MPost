package com.popsecu.sdk;

/**
 * Created by xumin on 2015/9/7.
 */

import android.content.Context;

import com.popsecu.sdk.CfgInfo.AppKvInfo;
import com.popsecu.sdk.CfgInfo.CfgKeyValue;
import com.popsecu.sdk.CfgInfo.TreeInfo;

import java.io.UnsupportedEncodingException;

public class TreeInfoImp {

    private final int SET_ITEM_SIZE = 64;
    private TreeInfo mHwTreeInfo = new TreeInfo();
    private TreeInfo mAppTreeInfo = new TreeInfo();
    private ParseXml mParseXml = new ParseXml();
    private Context mContext;

    //test treeInfo start
    public void setMHwTreeInfo (TreeInfo treeInfo) {
        this.mHwTreeInfo = treeInfo ;
    }

    public TreeInfo getMHwTreeInfo() {
        return this.mHwTreeInfo ;
    }

    public void setAppTreeInfo (TreeInfo treeInfo) {
        this.mAppTreeInfo = treeInfo ;
    }

    public TreeInfo getAppTreeInfo() {
        return this.mAppTreeInfo ;
    }

    //test treeInfo end


    public TreeInfoImp(Context context) {
        mAppTreeInfo.name = "App";
        mContext = context;
    }

    public void initTreeInfoImp() {
        mParseXml.initParseXml(mContext);
    }

    public boolean loadCfgFromDev(byte[] buf) {
        mHwTreeInfo = new TreeInfo();
        mAppTreeInfo = new TreeInfo();
        String str;

        try {
            mHwTreeInfo.name = new String(buf, 0, SET_ITEM_SIZE, "UTF-8");
        } catch (UnsupportedEncodingException e) {
            e.printStackTrace();
            return false;
        }

        for (int i = 1; i < (buf.length / SET_ITEM_SIZE); i++) {
            parseCfgItem(buf, i * SET_ITEM_SIZE, SET_ITEM_SIZE);
        }

        return true;
    }

    private void parseCfgItem(byte[] buf, int ofs, int len) {
        int hwnd = ((int)buf[ofs + 0] & 0xFF) |
                ((int)buf[ofs + 1] & 0xFF) << 8 |
                ((int)buf[ofs + 2] & 0xFF) << 16|
                ((int)buf[ofs + 3] & 0xFF) << 24;

        String str;
        try {
            str = new String(buf, ofs + 4, len - 4, "UTF-8");
        } catch (UnsupportedEncodingException e) {
            e.printStackTrace();
            return;
        }

        int idx = str.indexOf('\0');
        if (idx == -1) {
            return;
        }
        String key = str.substring(0, idx);
        String value = "";
        int end = str.indexOf('\0', idx + 1);
        if (end != -1) {
            value = str.substring(idx + 1, end);
        }

        if ((hwnd >= 1000) && (hwnd < 2000)) {
            CfgKeyValue kv = new CfgKeyValue();
            kv.keyName = key;
            kv.disName = key;

            AppKvInfo kvInfo = mParseXml.getAppKVInfo(key);
            if (kvInfo != null) {

            }
        }
    }
}
