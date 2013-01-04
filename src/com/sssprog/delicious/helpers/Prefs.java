package com.sssprog.delicious.helpers;

import android.content.Context;
import android.content.SharedPreferences;

public class Prefs {
	
	public static final String AUTHENTICATED = "AUTHENTICATED";
	public static final String USER_NAME = "USER_NAME";
	public static final String PASSWORD = "PASSWORD";
	

	private static final String STORAGE_NAME = "ApplicationPrefs";
	private static SharedPreferences settings = null;
	private static SharedPreferences.Editor editor = null;

	public static void init(Context context) {
		settings = context.getSharedPreferences(STORAGE_NAME, Context.MODE_PRIVATE);
		editor = settings.edit();
	}

	/*	String values	*/
	public static void setString(String name, String value) {
		editor.putString(name, value);
		editor.commit();
	}

	public static String getString(String name) {
		return settings.getString(name, null);
	}
	
	/*	Int values	*/
	public static void setInt(String name, Integer value) {
		editor.putInt(name, value);
		editor.commit();
	}

	public static int getInt(String name) {
		return settings.getInt(name, 0);
	}
	

	/*	Boolean values	*/
	public static void setBoolean(String name, Boolean value) {
		editor.putBoolean(name, value);
		editor.commit();
	}

	public static boolean getBoolean(String name) {
		return settings.getBoolean(name, false);
	}
	
	/*	Float values	*/
	public static void setFloat(String name, Float value) {
		editor.putFloat(name, value);
		editor.commit();
	}

	public static float getFloat(String name) {
		return settings.getFloat(name, 0);
	}
	
	/*	Long values	*/
	public static void setLong(String name, Long value) {
		editor.putLong(name, value);
		editor.commit();
	}

	public static long getLong(String name) {
		return settings.getLong(name, 0);
	}
	
	public static boolean containsKey(String name) {
		return settings.contains(name);
	}
	
	public static void remove(String name) {
		editor.remove(name);
		editor.commit();
	}
	
}
