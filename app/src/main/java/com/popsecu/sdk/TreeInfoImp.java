package com.popsecu.sdk;

/**
 * Created by xumin on 2015/9/7.
 */

import android.content.Context;

import com.popsecu.sdk.CfgInfo.AppKvInfo;
import com.popsecu.sdk.CfgInfo.CfgKeyValue;
import com.popsecu.sdk.CfgInfo.TreeInfo;
import com.popsecu.sdk.CfgInfo.CfgClassInfo;

import java.io.BufferedInputStream;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.io.UnsupportedEncodingException;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.util.ArrayList;
import java.util.List;

public class TreeInfoImp {
    private static TreeInfoImp mInstance = null ;

    private final int SET_ITEM_SIZE = 64;
    //private List<TreeInfo> mRootTreeInfo = new ArrayList<TreeInfo>();
    private TreeInfo mHwTreeInfo = new TreeInfo();
    private TreeInfo mAppTreeInfo = new TreeInfo();

    private ParseXml mParseXml = new ParseXml();
    private Context mContext;
    private List<Integer> mHwHwndList = new ArrayList<Integer>();

    private String mCurrentClassName = "";


//    public static TreeInfoImp getInstance() {
//        if (mInstance == null) {
//            mInstance = new TreeInfoImp() ;
//        }
//        return mInstance ;
//    }

    //test treeInfo start
    public void setMHwTreeInfo (TreeInfo treeInfo) {
        //this.mHwTreeInfo = treeInfo ;
    }

    public TreeInfo getMHwTreeInfo() {
        return this.mHwTreeInfo ;
    }

    public void setAppTreeInfo (TreeInfo treeInfo) {
        //this.mAppTreeInfo = treeInfo ;
    }

    public TreeInfo getAppTreeInfo() {
        return this.mAppTreeInfo ;
    }

    //test treeInfo end


    public TreeInfoImp(Context context) {
        mContext = context;
    }

    public void initTreeInfoImp() {
        mAppTreeInfo.name = "App";
        mParseXml.initParseXml(mContext);

        //mRootTreeInfo.add(mHwTreeInfo);
        //mRootTreeInfo.add(mAppTreeInfo);

        byte[] buf = readCfgFromFile();
        if (buf == null) {
            return;
        }

        loadCfgFromDev(buf);
    }

    private byte[] readCfgFromFile() {
        int total = 0;
        File file = new File("/sdcard/tmp.cfg");
        int fileLen = (int)file.length();
        byte[] buf = new byte[fileLen];
        try {
            InputStream in = new BufferedInputStream(new FileInputStream(file));
            while (true) {
                int ret = in.read(buf, total, fileLen - total);
                if (ret <= 0) {
                    break;
                }
                total += ret;
            }
            in.close();
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }

        return  buf;
    }

    public boolean loadCfgFromDev(byte[] buf) {
        mHwTreeInfo = new TreeInfo();
        mAppTreeInfo = new TreeInfo();
        mAppTreeInfo.name = "App";
        String str;

        try {
            str = new String(buf, 0, SET_ITEM_SIZE, "UTF-8");
            mHwTreeInfo.name = str.substring(0, str.indexOf(0, '\0'));
        } catch (UnsupportedEncodingException e) {
            e.printStackTrace();
            return false;
        }

        for (int i = 1; i < (buf.length / SET_ITEM_SIZE); i++) {
            parseCfgItem(buf, i * SET_ITEM_SIZE, SET_ITEM_SIZE);
        }

        return true;
    }

    public List<CfgClassInfo> getClassInfoList() {
        return mParseXml.getClassInfoList();
    }

    public void addClass(String name) {
        if (getTreeItemInfo(name) != null) {
            return;
        }

        TreeInfo item = new TreeInfo();
        item.name = name;
        mHwTreeInfo.childList.add(item);
    }

    public void delClass(String name) {
        removeTreeNode(mHwTreeInfo.childList, name);
    }

    public void addClassInst(String name) {
        CfgClassInfo classInfo = mParseXml.getClassInfo(name);
        if (classInfo == null) {
            return;
        }

        TreeInfo item = getTreeItemInfo(name);
        if (item == null) {
            return;
        }

        int idx;
        TreeInfo sub = new TreeInfo();
        int size = item.childList.size();
        if (size == 0) {
            idx = 0;
        } else {
            idx = Integer.parseInt(item.childList.get(size - 1).name.split("_")[1]);
        }
        sub.name = item.name + '_' + idx;
        int hwnd = getHwndIdx(0);
        List<CfgKeyValue> srcKeyValueList = mParseXml.getClassInfo(item.name).keyValueList;
        copyKeyValueList(sub.keyValueList, srcKeyValueList, hwnd);
        item.childList.add(sub);
    }

    public void delClassInst(String name) {
        removeTreeNode(mHwTreeInfo.childList, name);
    }

    public void addAppKv() {
        CfgKeyValue kv = new CfgKeyValue();
        kv.type = CfgInfo.TYPE_VIEW_EDIT;
        kv.isDisNameReadOnly = false;
        kv.isValueEditable = true;
        mAppTreeInfo.keyValueList.add(kv);
    }

    public void delAppKv(int idx) {
        mAppTreeInfo.keyValueList.remove(idx);
    }

    public void updateAppKvItem(int idx, String key) {
        AppKvInfo kvInfo = mParseXml.getAppKVInfo(key);
        if (kvInfo == null) {
            return;
        }

        CfgKeyValue kv = mAppTreeInfo.keyValueList.get(idx);
        kv.keyName = key;
        kv.disName = key;
        kv.valueList.clear();
        kv.defaultValue = kvInfo.defaultValue;
        if (kvInfo.limit.equals(CfgInfo.TYPE_VIEW_HW)) {
            for (String cname : kvInfo.className) {
                int counts = getClassInstCounts(cname);
                for (int i = 0; i < counts; i++) {
                    kv.valueList.add(String.format("%s_%d", cname, i));
                }
            }
        } else if (kvInfo.limit.equals(CfgInfo.TYPE_VIEW_SELECT)) {
            kv.valueList = new ArrayList<String>(kv.valueList);
        } else {
        }
    }

    public List<String> getAppKeyList() {
        return mParseXml.getmAppkeyList();
    }

    private int getClassInstCounts(String name) {
        for (TreeInfo item : mHwTreeInfo.childList) {
            if (item.name.equals(name)) {
                return item.childList.size();
            }
        }

        return 0;
    }

    private void removeTreeNode(List<TreeInfo> list, String name) {
        for (TreeInfo item : list) {
            if (item.name.equals(name)) {
                list.remove(item);
                return;
            }

            if (item.childList.size() > 0) {
                removeTreeNode(item.childList, name);
            }
        }
    }

    private void copyKeyValueList(List<CfgKeyValue> des, List<CfgKeyValue> src, int hwnd) {
        for (CfgKeyValue kv : src) {
            CfgKeyValue cfg = new CfgKeyValue();
            cfg.type = kv.type;
            cfg.keyName = kv.keyName;
            cfg.disName = kv.disName;
            cfg.isDisNameReadOnly = kv.isDisNameReadOnly;
            cfg.defaultValue = kv.defaultValue;
            cfg.isValueEditable = kv.isValueEditable;
            cfg.valueList = kv.valueList;
            cfg.hwnd = hwnd;
            des.add(cfg);
        }
    }
    private int getHwndIdx(int flag) {
        if (flag == 0) {
            for (int i = 2000; i < 3000; i++) {
                if (!mHwHwndList.contains(i)) {
                    mHwHwndList.add(i);
                    return i;
                }
            }
        } else if (flag == 1) {
            return 1000;
        }

        return 0;
    }

    private String getClassInstNameByHwnd(int hwnd) {
        for(TreeInfo item : mHwTreeInfo.childList) {
            for (TreeInfo subItem : mHwTreeInfo.childList) {
                if (subItem.keyValueList.get(0).hwnd == hwnd) {
                    return subItem.name;
                }
            }
        }

        return null;
    }

    private TreeInfo getClassInstByHwnd(TreeInfo classItem, int hwnd) {
        for (TreeInfo item : classItem.childList) {
            if ((item.keyValueList.size() > 0) &&
                    (item.keyValueList.get(0).hwnd ==hwnd )) {
                return item;
            }
        }
        return null;
    }

    private TreeInfo getTreeItemInfo(String name) {
        for (TreeInfo item : mHwTreeInfo.childList) {
            if (item.name.equals(name)) {
                return  item;
            }
        }

        return null;
    }

    private int getHwndByClassInst(String name) {
        for (TreeInfo item : mHwTreeInfo.childList) {
            for (TreeInfo subItem : item.childList) {
                if (subItem.name.equals(name)) {
                    return subItem.keyValueList.get(0).hwnd;
                }
            }
        }
        return 0;
    }


    private void copyKeyValue(CfgKeyValue dis, CfgKeyValue src) {
        dis.type = src.type;
        dis.keyName = src.keyName;
        dis.disName = src.disName;
        dis.isDisNameReadOnly = src.isDisNameReadOnly;
        dis.defaultValue = src.defaultValue;
        dis.isValueEditable = src.isValueEditable;
        dis.valueList = src.valueList;
        dis.hwnd = src.hwnd;
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
                if (kvInfo.limit == CfgInfo.TYPE_VIEW_HW) {
                    value = getClassInstNameByHwnd(Integer.parseInt(value));
                }
            }

            kv.defaultValue = value;
            mAppTreeInfo.keyValueList.add(kv);
        } else if ((hwnd >= 2000) && (hwnd < 3000)) {
            if (key.equals(CfgInfo.CFG_CLASS_KEY)) {
                mCurrentClassName = value;
                return;
            }
            TreeInfo classItem = getTreeItemInfo(mCurrentClassName);
            if (classItem == null) {
                classItem = new TreeInfo();
                classItem.name = mCurrentClassName;
                mHwTreeInfo.childList.add(classItem);
            }

            TreeInfo instItem = getClassInstByHwnd(classItem, hwnd);
            if (instItem == null) {
                instItem = new TreeInfo();
                instItem.name = mCurrentClassName + '_' + classItem.childList.size();
                classItem.childList.add(instItem);
            }

            CfgKeyValue desKv = new CfgKeyValue();
            CfgKeyValue srcKv = mParseXml.getHwKeyValue(mCurrentClassName, key);
            copyKeyValue(desKv, srcKv);
            desKv.defaultValue = value;
            desKv.hwnd = hwnd;
            instItem.keyValueList.add(desKv);

            mHwHwndList.add(hwnd);
        }
    }

    private  byte[] packageKv(ByteBuffer buf, int hwnd, String key, String value) {
        buf.position(0);
        buf.putInt(hwnd);
        buf.put(key.getBytes(), 0, key.length());
        buf.put((byte) 0);
        buf.put(value.getBytes(), 0, value.length());
        byte[] tmp = buf.array();

        return tmp;
    }

    private byte[] serializationAllCfg() {
        ByteArrayOutputStream out =  new ByteArrayOutputStream();
        ByteBuffer buf = ByteBuffer.allocate(64).order(ByteOrder.LITTLE_ENDIAN);
        byte[] packageBuf;

        for (TreeInfo node : mHwTreeInfo.childList) {
            for (TreeInfo subNode : node.childList) {
                if (subNode.keyValueList.size() > 0) {
                    packageBuf = packageKv(buf, subNode.keyValueList.get(0).hwnd, CfgInfo.CFG_CLASS_KEY, node.name);
                    try {
                        out.write(packageBuf);
                    } catch (IOException e) {
                        e.printStackTrace();
                        return null;
                    }
                }

                for (CfgKeyValue item : subNode.keyValueList) {
                    packageBuf = packageKv(buf, item.hwnd, item.keyName, item.defaultValue);
                    try {
                        out.write(packageBuf);
                    } catch (IOException e) {
                        e.printStackTrace();
                        return null;
                    }
                }
            }
        }

        for (CfgKeyValue item : mAppTreeInfo.keyValueList) {
            if (item.disName.length() == 0) {
                continue;
            }

            item.keyName = item.disName;
            String value = item.defaultValue;
            AppKvInfo kvInfo = mParseXml.getAppKVInfo(item.keyName);
            if (kvInfo != null) {
                if (kvInfo.limit.equals(CfgInfo.TYPE_VIEW_HW)) {
                    value = Integer.toString(getHwndByClassInst(value));
                }
            } else {
                continue;
            }

            packageBuf = packageKv(buf, item.hwnd, item.keyName, value);
            try {
                out.write(packageBuf);
            } catch (IOException e) {
                e.printStackTrace();
                return null;
            }
        }

        byte[] totalBuf = out.toByteArray();
        try {
            out.close();
        } catch (IOException e) {
            e.printStackTrace();
        }

        return totalBuf;
    }
}
