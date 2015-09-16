package com.popsecu.sdk;

import android.content.Context;

import java.util.List;

/**
 * Created by GTW on 2015/9/8.
 */
public class Controller {

    private static Controller mController = null ;

    private Controller() {

    }

    public static Controller getInstance() {
        if (mController == null) {
            mController = new Controller() ;
        }
        return mController ;
    }

    private TreeInfoImp mTreeInfoImp ;
    private List<CfgInfo.TreeInfo> appTreeInfoList ;

    public void initThreeInfo(Context context) {
        mTreeInfoImp = new TreeInfoImp(context) ;
        mTreeInfoImp.initTreeInfoImp();
        CommInteface.getInstance().initCommInterface(context);
//        // test post data
//        CfgInfo.TreeInfo postTreeInfo = new CfgInfo.TreeInfo() ;
//        List<CfgInfo.TreeInfo>  postTreeInfoList = new ArrayList<>() ;
//        for (int i = 0 ; i < 6 ; i ++) {
//            CfgInfo.TreeInfo treeInfo = new CfgInfo.TreeInfo() ;
//            List<CfgInfo.TreeInfo> lChildTreeList =  new ArrayList<>() ;
//            for (int j = 0 ; j < 6 ; j ++) {
//                CfgInfo.TreeInfo childTreeInfo = new CfgInfo.TreeInfo() ;
//                childTreeInfo.name = "第" + i + "组" + j + "个" ;
//                List<CfgInfo.CfgKeyValue> keyValueList = new ArrayList<>();
//                for (int k = 0 ; k < 6 ; k ++) {
//                    CfgInfo.CfgKeyValue keyValue = new CfgInfo.CfgKeyValue() ;
//                    keyValue.keyName = "keyName" + k ;
//                    keyValue.disName = "disName" + k ;
//                    keyValue.defaultValue = "default" + k ;
//                    keyValueList.add(keyValue) ;
//                }
//                childTreeInfo.keyValueList = keyValueList ;
//                lChildTreeList.add(childTreeInfo) ;
//            }
//
//            treeInfo.childList = lChildTreeList ;
//            treeInfo.name = "第" + i + "组" ;
//            postTreeInfoList.add(treeInfo) ;
//        }
//        postTreeInfo.childList = postTreeInfoList ;
//        postTreeInfo.name = "POST" ;
//        mTreeInfoImp.setMHwTreeInfo(postTreeInfo);
//
//        //test app data
//        CfgInfo.TreeInfo appTreeInfo = new CfgInfo.TreeInfo() ;
//        List<CfgInfo.CfgKeyValue> appKeyValueList = new ArrayList<>();
//        for (int k = 0 ; k < 6 ; k ++) {
//            CfgInfo.CfgKeyValue keyValue = new CfgInfo.CfgKeyValue() ;
//            keyValue.keyName = "keyName" + k ;
//            keyValue.disName = "disName" + k ;
//            keyValue.defaultValue = "default" + k ;
//            appKeyValueList.add(keyValue) ;
//        }
//        appTreeInfo.keyValueList = appKeyValueList ;
//        appTreeInfo.name = "APP" ;
//        mTreeInfoImp.setAppTreeInfo(appTreeInfo);
    }

    public TreeInfoImp getTreeInfoImp() {
        return this.mTreeInfoImp ;
    }

}
