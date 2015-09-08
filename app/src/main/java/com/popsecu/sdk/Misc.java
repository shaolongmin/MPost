package com.popsecu.sdk;

import android.util.Log;


public class Misc {
	private static final String PRODUCT_NAME = "PopMain";
	static public final boolean DEBUG = true;

	static public void logd(String msg) {
		if (DEBUG) {
			Log.d(PRODUCT_NAME, msg);
		}
	}

	static public void loge(String msg) {
		if (DEBUG) {
			Log.e(PRODUCT_NAME, msg);
		}
	}

	static public void logi(String msg) {
		if (DEBUG) {
			Log.i(PRODUCT_NAME, msg);
		}
	}

	static public void debugE(String log, String msg) {
		if (DEBUG) {
			Log.e(log, msg);
		}
	}

	static public void debugI(String log, String msg) {
		if (DEBUG) {
			Log.i(log, msg);
		}
	}

	static public void debugD(String log, String msg) {
		if (DEBUG) {
			Log.d(log, msg);
		}
	}
}
