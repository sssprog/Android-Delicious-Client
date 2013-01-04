package com.sssprog.delicious.api;

import com.sssprog.delicious.dbmodels.PostModel;

public interface Api {
	
	public static final String BROADCAST_ACTION_SYNC_STATUS_CHANGED = "BROADCAST_ACTION_SYNC_STATUS_CHANGED";
	public static final String BROADCAST_SYNC_STATUS_CHANGED_PARAM_SYNCING = "BROADCAST_SYNC_STATUS_CHANGED_PARAM_NEW_STATUS";
	
	public static final String BROADCAST_ACTION_DB_DATA_CHANGED = "BROADCAST_ACTION_DB_DATA_CHANGED";
	
	public void authenticate(String userName, String password, ApiCallback<Void> apiCallback);
	
	public boolean authIsRunning();
	
	public void sync();
	
	public boolean isAuthenticated();
	
	public boolean isSyncing();
	
	public void savePost(PostModel post, ApiCallback<Void> apiCallback);
	
	public void deletePost(PostModel post, ApiCallback<Void> apiCallback);
	
	public void logout();

}
