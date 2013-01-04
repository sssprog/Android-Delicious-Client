package com.sssprog.delicious.helpers;

import com.sssprog.delicious.App;

import android.util.Log;

public class LogHelper {
	
	public static void i(String tag, String message) {
		if (App.DEBUG)
			Log.i(tag, message);
	}

	public static void d(String tag, String message) {
		if (App.DEBUG)
			Log.d(tag, message);
	}

	public static void w(String tag, String message) {
		if (App.DEBUG)
			Log.w(tag, message);
	}

	public static void e(String tag, String message) {
		if (App.DEBUG)
			Log.e(tag, message);
	}

}
