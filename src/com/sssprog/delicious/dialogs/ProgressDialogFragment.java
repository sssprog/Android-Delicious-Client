package com.sssprog.delicious.dialogs;

import com.sssprog.delicious.App;
import com.sssprog.delicious.R;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

public class ProgressDialogFragment extends BaseDialogFragment {
	private static final String PARAM_TEXT = "PARAM_TEXT";
	
	public static ProgressDialogFragment newInstance(String text) {
		ProgressDialogFragment f = new ProgressDialogFragment();
		Bundle args = new Bundle();
		args.putString(PARAM_TEXT, text);
		f.setArguments(args);
		return f;
	}
	
	public static ProgressDialogFragment newInstance(int resId) {
		return newInstance(App.getContext().getString(resId));
	}
	
	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setCancelable(false);
		setStyle(STYLE_NO_TITLE, 0);
	}
	
	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
		View v = inflater.inflate(R.layout.progress_dialog, container, false);
		String text = getArguments().getString(PARAM_TEXT);
		TextView tv = (TextView) v.findViewById(R.id.tvText);
		tv.setText(text);
		return v;
	}

}
