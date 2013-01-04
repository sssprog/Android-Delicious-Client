package com.sssprog.delicious;

import java.lang.ref.WeakReference;
import java.util.ArrayList;
import java.util.List;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Bundle;
import android.os.Handler;
import android.support.v4.content.LocalBroadcastManager;

import com.actionbarsherlock.view.Menu;
import com.actionbarsherlock.view.MenuItem;
import com.github.rtyley.android.sherlock.roboguice.activity.RoboSherlockFragmentActivity;
import com.google.inject.Inject;
import com.sssprog.delicious.api.Api;
import com.sssprog.delicious.api.ApiResult;
import com.sssprog.delicious.api.ApiResultReceiver;

public class BaseActivity extends RoboSherlockFragmentActivity implements ApiResultReceiver {
	
	private static final Handler sHandler = new Handler();
	private final List<WeakReference<ApiResultReceiver>> mFragments = new ArrayList<WeakReference<ApiResultReceiver>>();
	
	@Inject
	protected Api api;
	protected Menu mOptionsMenu;
	private MyBroadcastReceiver mBroadcastReceiver;
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		mBroadcastReceiver = new MyBroadcastReceiver();
		IntentFilter filter = new IntentFilter(Api.BROADCAST_ACTION_SYNC_STATUS_CHANGED);
		LocalBroadcastManager.getInstance(App.getContext()).registerReceiver(mBroadcastReceiver, filter);
		
		if (getClass() != MainActivity_.class && getClass() != LoginActivity_.class)
			getSupportActionBar().setDisplayHomeAsUpEnabled(true);
		
		getSupportFragmentManager();
	}
	
	public void addApiResultReceiver(ApiResultReceiver receiver) {
		for (WeakReference<ApiResultReceiver> w: mFragments) {
			ApiResultReceiver r = w.get();
			if (r == receiver)
				return;
		}
		mFragments.add(new WeakReference<ApiResultReceiver>(receiver));
	}

	@SuppressWarnings("rawtypes")
	@Override
	public void onApiResult(final ApiResult result) {
		sHandler.post(new Runnable() {
			
			@Override
			public void run() {
				for (WeakReference<ApiResultReceiver> w: mFragments) {
					ApiResultReceiver r = w.get();
					if (r != null)
						r.onApiResult(result);
				}
			}
		});
	}
	
	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		getSupportMenuInflater().inflate(R.menu.main_activity, menu);
		mOptionsMenu = menu;
		if (api.isSyncing())
			setRefreshActionButtonState(true);
		return true;
	}
	
	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		switch (item.getItemId()) {
		case android.R.id.home:
			finish();
			return true;
			
		case R.id.menu_refresh:
			api.sync();
			return true;
			
		case R.id.menu_add:
			Intent intent = new Intent(this, EditPostActivity_.class);
			intent.putExtras(EditPostActivity.getIntentExtras(null));
			startActivity(intent);
			return true;
			
		case R.id.menu_logout:
			api.logout();
			if (this.getClass() == MainActivity_.class) {
				startActivity(new Intent(this, LoginActivity_.class));
				finish();
			} else {
				intent = new Intent(this, MainActivity_.class);
				intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
				startActivity(intent);
			}
			return true;
		}
		return super.onOptionsItemSelected(item);
	}
	
	protected void setRefreshActionButtonState(boolean refreshing) {
        if (mOptionsMenu == null) {
            return;
        }

        final MenuItem refreshItem = mOptionsMenu.findItem(R.id.menu_refresh);
        if (refreshItem != null) {
            if (refreshing) {
                refreshItem.setActionView(R.layout.actionbar_indeterminate_progress);
            } else {
                refreshItem.setActionView(null);
            }
        }
    }
	
	private class MyBroadcastReceiver extends BroadcastReceiver {

		@Override
		public void onReceive(Context context, Intent intent) {
			String action = intent.getAction();
			if (action.equals(Api.BROADCAST_ACTION_SYNC_STATUS_CHANGED)) {
				boolean syncing = intent.getBooleanExtra(Api.BROADCAST_SYNC_STATUS_CHANGED_PARAM_SYNCING, false);
				setRefreshActionButtonState(syncing);
			}
		}
		
	}
	
}
