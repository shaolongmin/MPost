package com.popsecu.sdk;

import java.util.ArrayList;
import java.util.List;

public class CfgInfo {
	public static String TYPE_VIEW_EDIT = "edit";
	public static String TYPE_VIEW_SELECT = "select";
	public static String TYPE_VIEW_HW = "HARDWARE";
	public static String CFG_CLASS_KEY = "CLASS";
	
	public static class CfgKeyValue {
		public String type = TYPE_VIEW_EDIT; // type 判断
		public String keyName = "";
		public int hwnd = 0;
		public String disName = "";// 显示
		public boolean isDisNameReadOnly = true;  //判断key是否可以编辑
		public String defaultValue = ""; //显示
		public boolean isValueEditable = true;  //value 是否可编辑
		public List<String> valueList = new ArrayList<String>();
	}

	//app 添加
	public static class CfgClassInfo {
		public String name = "";
		public List<CfgKeyValue> keyValueList = new ArrayList<CfgKeyValue>();
	}
	
	public static class TempInfo {
		public String name = "";
		public List<CfgClassInfo> keyValueList = new ArrayList<CfgClassInfo>();
	}
	
	public static class AppKvInfo {
		public String limit = TYPE_VIEW_EDIT;
		public String key = "";
		public List<String> className = new ArrayList<String>();
		public String defaultValue = "";
		public List<String> valueList = new ArrayList<String>();
	}

	public static class TreeInfo {
		public String name = "";
		public List<TreeInfo> childList =  new ArrayList<TreeInfo>();
		public List<CfgKeyValue> keyValueList = new ArrayList<CfgKeyValue>();
	}
}
