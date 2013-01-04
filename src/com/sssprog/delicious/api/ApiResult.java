package com.sssprog.delicious.api;

public class ApiResult<T> {
	
	public static enum ApiRestRequestStatus {
		RestStatusOk,
		RestStatusAuthFailed,
		RestStatusNetworkDown,
		RestStatusServerError,
		RestStatusServerOrNetworkError,
		RestStatusThrottled
	}
	
//	public int requestType;
	public final T result;
	public final ApiRestRequestStatus restRequestStatus;
	
	private ApiResult(ApiRestRequestStatus restRequestStatus, T result) {
		super();
		this.result = result;
		this.restRequestStatus = restRequestStatus;
	}
	
	public static <E> ApiResult<E> newInstance(ApiRestRequestStatus restRequestStatus, E result) {
		return new ApiResult<E>(restRequestStatus, result);
	}
	
	public static <E> ApiResult<E> newInstance(E result) {
		return newInstance(ApiRestRequestStatus.RestStatusOk, result);
	}
	
	@Override
	public String toString() {
		return restRequestStatus + "  " + result;
	}
	
}
