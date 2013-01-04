package com.sssprog.delicious.dialogs;

import com.github.rtyley.android.sherlock.roboguice.fragment.RoboSherlockDialogFragment;

public class BaseDialogFragment extends RoboSherlockDialogFragment {

	@Override
	public void onDestroyView() {
		if (getDialog() != null && getRetainInstance())
			getDialog().setOnDismissListener(null);
		super.onDestroyView();
	}
	
}
