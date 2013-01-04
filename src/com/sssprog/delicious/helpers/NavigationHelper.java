package com.sssprog.delicious.helpers;

import java.util.ArrayList;
import java.util.List;

import android.content.Context;
import android.content.Intent;
import android.os.Handler;
import android.widget.ArrayAdapter;

import com.actionbarsherlock.app.ActionBar;
import com.actionbarsherlock.app.ActionBar.OnNavigationListener;
import com.sssprog.delicious.AllPostsActivity_;
import com.sssprog.delicious.App;
import com.sssprog.delicious.BaseActivity;
import com.sssprog.delicious.MainActivity_;
import com.sssprog.delicious.R;
import com.sssprog.delicious.RecentPostsActivity_;

public class NavigationHelper {
	
	private static final List<Class<? extends BaseActivity>> listNavActivities;
	private static final List<String> listNavTitles;
	static {
		listNavActivities = new ArrayList<Class<? extends BaseActivity>>();
		listNavActivities.add(MainActivity_.class);
		listNavActivities.add(RecentPostsActivity_.class);
		listNavActivities.add(AllPostsActivity_.class);
		
		listNavTitles = new ArrayList<String>();
		listNavTitles.add(App.getContext().getString(R.string.title_activity_main));
		listNavTitles.add(App.getContext().getString(R.string.title_activity_recent_posts));
		listNavTitles.add(App.getContext().getString(R.string.title_activity_all_posts));
	}
	
	public static void setupListNavigation(final ActionBar actionBar, final BaseActivity activity) {
		Context context = actionBar.getThemedContext();
		ArrayAdapter<String> adapter = new ArrayAdapter<String>(context, R.layout.sherlock_spinner_item, listNavTitles);
		adapter.setDropDownViewResource(R.layout.sherlock_spinner_dropdown_item);

		actionBar.setDisplayShowTitleEnabled(false);
		actionBar.setNavigationMode(ActionBar.NAVIGATION_MODE_LIST);
		final int position = listNavActivities.indexOf(activity.getClass());
		actionBar.setListNavigationCallbacks(adapter, new OnNavigationListener() {
			
			@Override
			public boolean onNavigationItemSelected(int itemPosition, long itemId) {
				if (position == itemPosition)
					return false;
				Intent intent = new Intent(activity, listNavActivities.get(itemPosition));
				intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
				activity.startActivity(intent);
//				activity.finish();
//				actionBar.setSelectedNavigationItem(position);
				return true;
			}
		});
		actionBar.setSelectedNavigationItem(position);
	}
	
	public static void updateNavListTitleOnResume(ActionBar actionBar, BaseActivity activity) {
		final int position = listNavActivities.indexOf(activity.getClass());
		actionBar.setSelectedNavigationItem(position);
	}
	
}
