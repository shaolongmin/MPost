package com.popsecu.sdk;

import java.util.ArrayList;
import java.util.List;

public class CfgInfo {
	public static String TYPE_VIEW_EDIT = "edit";
	public static String TYPE_VIEW_SELECT = "select";
	public static String TYPE_VIEW_HW = "HARDWARE";
	
	public static class CfgKeyValue {
		public String type = TYPE_VIEW_EDIT;
		public String keyName = "";
		public int hwnd = 0;
		public String disName = "";
		public boolean isDisNameReadOnly = true;
		public String defaultValue = "";
		public boolean isValueEditable = true;
		public List<String> valueList = new ArrayList<String>();
	}
	
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
