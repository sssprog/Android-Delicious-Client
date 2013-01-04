package com.sssprog.delicious;

import android.app.Activity;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Bundle;
import android.support.v4.content.LocalBroadcastManager;

import com.github.rtyley.android.sherlock.roboguice.fragment.RoboSherlockFragment;
import com.sssprog.delicious.api.Api;
import com.sssprog.delicious.api.ApiResult;
import com.sssprog.delicious.api.ApiResultReceiver;

public class BaseFragment extends RoboSherlockFragment implements ApiResultReceiver {
	
	private MyBroadcastReceiver mBroadcastReceiver;
	
	@Override
	public void onAttach(Activity activity) {
		super.onAttach(activity);
		attachReceiver();
	}
	
	private void attachReceiver() {
		if (getActivity() != null)
			((BaseActivity) getActivity()).addApiResultReceiver(this);
	}
	
	@Override
	public void onActivityCreated(Bundle savedInstanceState) {
		super.onActivityCreated(savedInstanceState);
		attachReceiver();
		mBroadcastReceiver = new MyBroadcastReceiver();
		IntentFilter filter = new IntentFilter(Api.BROADCAST_ACTION_SYNC_STATUS_CHANGED);
		filter.addAction(Api.BROADCAST_ACTION_DB_DATA_CHANGED);
		LocalBroadcastManager.getInstance(App.getContext()).registerReceiver(mBroadcastReceiver, filter);
	}
	
	private class MyBroadcastReceiver extends BroadcastReceiver {

		@Override
		public void onReceive(Context context, Intent intent) {
			String action = intent.getAction();
			if (action.equals(Api.BROADCAST_ACTION_SYNC_STATUS_CHANGED)) {
				boolean syncing = intent.getBooleanExtra(Api.BROADCAST_SYNC_STATUS_CHANGED_PARAM_SYNCING, false);
				onSyncStatusChanged(syncing);
			} else if (action.equals(Api.BROADCAST_ACTION_DB_DATA_CHANGED)) {
				onDbDataChanged();
			}
		}
		
	}
	
	protected void onSyncStatusChanged(boolean syncing) {}
	
	protected void onDbDataChanged() {}

	@Override
	public void onApiResult(ApiResult result) {
	}
	
}
