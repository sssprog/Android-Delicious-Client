package com.sssprog.delicious.api;

public interface ApiCallback<T> {
	
	public void onApiResult(ApiResult<T> result);

}
