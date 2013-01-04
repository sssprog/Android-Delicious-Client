package com.sssprog.delicious.api;

public interface ApiResultReceiver {
	
	@SuppressWarnings("rawtypes")
	public void onApiResult(ApiResult result);

}
