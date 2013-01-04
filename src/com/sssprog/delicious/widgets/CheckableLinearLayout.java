package com.sssprog.delicious.widgets;

import android.content.Context;
import android.util.AttributeSet;
import android.widget.Checkable;
import android.widget.LinearLayout;

public class CheckableLinearLayout extends LinearLayout implements Checkable {
	
	private boolean mChecked;

	public CheckableLinearLayout(Context context) {
		super(context);
	}

	public CheckableLinearLayout(Context context, AttributeSet attrs) {
		super(context, attrs);
	}

	@Override
	public boolean isChecked() {
		return mChecked;
	}

	@Override
	public void setChecked(boolean value) {
		mChecked = value;
		refreshDrawableState();
	}

	@Override
	public void toggle() {
		setChecked(!mChecked);
	}
	
	private static final int[] CheckedStateSet = {
		android.R.attr.state_checked
	};
	
	@Override
	protected int[] onCreateDrawableState(int extraSpace) {
		final int[] drawableState = super.onCreateDrawableState(extraSpace + 1);
		if (isChecked()) {
		    mergeDrawableStates(drawableState, CheckedStateSet);
		}
		return drawableState;
	}
	
}
