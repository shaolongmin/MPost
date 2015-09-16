package com.popsecu.sdk;

import android.content.ContentResolver;
import android.content.Context;
import android.database.Cursor;
import android.net.Uri;
import android.provider.MediaStore;
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

	/**
	  * Try to return the absolute file path from the given Uri
	  *
	  * @param context
	  * @param uri
	  * @return the file path or null
	  */
	public static String getRealFilePath( final Context context, final Uri uri ) {
		if ( null == uri ) return null;
		final String scheme = uri.getScheme();
		String data = null;
		if ( scheme == null )
			data = uri.getPath();
		else if ( ContentResolver.SCHEME_FILE.equals( scheme ) ) {
			data = uri.getPath();
			}
		else if ( ContentResolver.SCHEME_CONTENT.equals( scheme ) ) {
			Cursor cursor = context.getContentResolver().query( uri, new String[] { MediaStore.Images.ImageColumns.DATA }, null, null, null );
			if ( null != cursor ) {
				if ( cursor.moveToFirst() ) {
					int index = cursor.getColumnIndex( MediaStore.Images.ImageColumns.DATA );
					if ( index > -1 ) {
						data = cursor.getString( index );
						}
				}
				cursor.close();
			}
		}return data;
	}
}
