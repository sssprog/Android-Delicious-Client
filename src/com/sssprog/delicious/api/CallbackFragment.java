package com.sssprog.delicious.api;

import android.app.Activity;
import android.os.Bundle;
import android.os.Handler;

import com.github.rtyley.android.sherlock.roboguice.fragment.RoboSherlockFragment;

public class CallbackFragment<T> extends RoboSherlockFragment {
	
//	private static final Handler sHandler = new Handler();
	
	private ApiResult<T> mResult;

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setRetainInstance(true);
	}
	
	@Override
	public void onAttach(Activity activity) {
		super.onAttach(activity);
		if (mResult != null)
			sendResult();
	}
	
	public ApiCallback<T> getApiCallback() {
		return new ApiCallback<T>() {

			@Override
			public void onApiResult(ApiResult<T> result) {
				mResult = result;
				sendResult();
			}
		};
	}
	
	private void sendResult() {
		if (getActivity() == null)
			return;
//		sHandler.post(new Runnable() {
//			
//			@Override
//			public void run() {
//				((ApiResultReceiver) getActivity()).onApiResult(mResult);				
//			}
//		});
		((ApiResultReceiver) getActivity()).onApiResult(mResult);
		try {
			getActivity().getSupportFragmentManager().beginTransaction().remove(this).commitAllowingStateLoss();
		} catch (IllegalStateException e) {}
	}
	
}
