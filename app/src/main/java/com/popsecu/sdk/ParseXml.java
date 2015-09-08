package com.popsecu.sdk;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;

import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.xml.sax.SAXException;


import android.content.Context;

import com.popsecu.sdk.CfgInfo;
import com.popsecu.sdk.CfgInfo.CfgClassInfo;
import com.popsecu.sdk.CfgInfo.CfgKeyValue;
import com.popsecu.sdk.CfgInfo.AppKvInfo;

public class ParseXml {
	private Context mContext;

	private List<CfgClassInfo> mClassInfoList = new ArrayList<CfgClassInfo>();
	private List<AppKvInfo> mAppKvList = new ArrayList<AppKvInfo>();
	private List<String> mAppkeyList = new ArrayList<String>();

	
	public void initParseXml(Context context) {
		mContext = context;
		pareseHwCfg();
	}
	
	private void pareseHwCfg() {
		DocumentBuilderFactory dbf = DocumentBuilderFactory
				.newInstance();
		DocumentBuilder db;
		Document doc;
		
		try {
			db = dbf.newDocumentBuilder();
			doc = db.parse(mContext.getAssets().open("cfg/hwcfg.xml"));
		} catch (Exception e) {
			e.printStackTrace();
			return;
		}

		NodeList classList = doc.getElementsByTagName("class");
		for (int i = 0; i < classList.getLength(); i++) {
			CfgClassInfo classInfo = new CfgClassInfo();
			Element classElem = (Element)classList.item(i);
			classInfo.name = classElem.getAttribute("name");
			
			NodeList kvList = classElem.getElementsByTagName("key");
			for (int j = 0; j < kvList.getLength(); j++) {
				Element kvElem = (Element)kvList.item(j);
				CfgKeyValue kv = new CfgKeyValue();
				kv.keyName = kvElem.getAttribute("name");
				
				Element vElem = (Element)(kvElem.getElementsByTagName("value").item(0));
				kv.disName = vElem.getAttribute("name");
				kv.defaultValue = vElem.getAttribute("default");
				kv.type = vElem.getAttribute("type");
                if (kv.type.equals("select")) {
                	//String[] tmp = vElem.getAttribute("select").split("\\|");
                	kv.valueList = Arrays.asList(vElem.getAttribute("select").split("\\|"));
                	kv.isValueEditable = false;
                }

                classInfo.keyValueList.add(kv);
			}

			mClassInfoList.add(classInfo);
		}
	}

	private void pareseAppKV() {
		DocumentBuilderFactory dbf = DocumentBuilderFactory
				.newInstance();
		DocumentBuilder db;
		Document doc;

		try {
			db = dbf.newDocumentBuilder();
			doc = db.parse(mContext.getAssets().open("temp/app.xml"));
		} catch (Exception e) {
			e.printStackTrace();
			return;
		}

		NodeList kvNodeList = doc.getElementsByTagName("key");
		for (int i = 0; i < kvNodeList.getLength(); i++) {
			AppKvInfo kv = new AppKvInfo();
			Element kvElem = (Element)kvNodeList.item(i);
			kv.key = kvElem.getAttribute("name");
			kv.limit = kvElem.getAttribute("limit");
			if (kv.limit.equals("HARDWARE")) {
				kv.className = Arrays.asList(kvElem.getAttribute("class").split("\\|"));
			} else if (kv.limit.equals("select")) {
				kv.valueList = Arrays.asList(kvElem.getAttribute("select").split("\\|"));
				kv.defaultValue = kvElem.getAttribute("default");
			} else if (kv.limit.equals("'edit'")) {
				kv.defaultValue = kvElem.getAttribute("default");
			}

			mAppKvList.add(kv);
			mAppkeyList.add(kv.key);
		}
	}

	public CfgClassInfo getClassInfo(String name) {
		for (CfgClassInfo item : mClassInfoList) {
			if (item.name.equals(name)) {
				return item;
			}
		}

		return null;
	}

	public AppKvInfo getAppKVInfo(String name) {
		for (int i = 0; i < mAppKvList.size(); i++) {
			if (mAppKvList.get(i).key.equals(name)) {
				return mAppKvList.get(i);
			}
		}
		return null;
	}

	public CfgKeyValue getHwKeyValue(String className, String keyName) {
		for (CfgClassInfo item : mClassInfoList) {
			if (item.name.equals(className)) {
				for (CfgKeyValue kv : item.keyValueList) {
					if (kv.keyName.equals(keyName)) {
						return kv;
					}
				}
			}
		}

		return null;
	}
}
