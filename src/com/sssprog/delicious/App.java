package com.sssprog.delicious;

import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;

import com.sssprog.activerecord.Database;
import com.sssprog.activerecord.DatabaseConfig;
import com.sssprog.delicious.api.ApiAsyncTask;
import com.sssprog.delicious.dbmodels.PostModel;
import com.sssprog.delicious.dbmodels.TagPostModel;
import com.sssprog.delicious.dbmodels.TagModel;
import com.sssprog.delicious.helpers.LogHelper;
import com.sssprog.delicious.helpers.Prefs;

import android.app.Application;
import android.content.Context;

public class App extends Application {
	public static final boolean DEBUG = true;
	private static final int DB_VERSION = 1;
	
	private static volatile Application sInstance;
	
	public static Context getContext() {
		return sInstance;
	}
	
	public App() {
		super();
		sInstance = this;
	}
	
	@Override
	public void onCreate() {
		super.onCreate();
		LogHelper.d("-TAG-", "onCreate App");
		
		loadClasses();
		
		Prefs.init(this);
		initDb();
		
	}
	
	/**
	 * Load classes where definied static Handlers.
	 * Workaround for this problem http://code.google.com/p/android/issues/detail?id=20915
	 */
	private void loadClasses() {
		String[] classes = new String[] { "com.sssprog.delicious.api.ApiAsyncTask" };
		for (String c : classes) {
			try {
				Class.forName(c);
			} catch (ClassNotFoundException e) {
				e.printStackTrace();
			}
		}
	}
	
	private void initDb() {
		new ApiAsyncTask<Void, Void, Void>() {

			@Override
			protected Void doInBackground(Void... params) {
				Database.debugMode = DEBUG;
				DatabaseConfig config = new DatabaseConfig();
				config.version = DB_VERSION;
				config.name = "delicious.db";
				
				config.addModelClass(PostModel.class);
				config.addModelClass(TagModel.class);
				config.addModelClass(TagPostModel.class);
				
				Database.initialize(getContext(), config);
				Database.open();
				return null;
			}
			
		}.execute();
	}

}
