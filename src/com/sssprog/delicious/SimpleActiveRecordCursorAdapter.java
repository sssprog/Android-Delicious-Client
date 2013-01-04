package com.sssprog.delicious;

import android.content.Context;
import android.support.v4.widget.SimpleCursorAdapter;

import com.sssprog.activerecord.ActiveRecord;
import com.sssprog.activerecord.ActiveRecordCursor;

public class SimpleActiveRecordCursorAdapter<T extends ActiveRecord> extends SimpleCursorAdapter {
	
	private ActiveRecordCursor<T> mCursor;

	public SimpleActiveRecordCursorAdapter(Context context, int layout,
			ActiveRecordCursor<T> c, String[] from, int[] to, int flags) {
		super(context, layout, null, from, to, flags);
		if (c != null)
			swapCursor(c.getCursor());
		mCursor = c;
	}
	
	@Override
	public Object getItem(int position) {
//		return super.getItem(position);
		mCursor.moveToPosition(position);
		return mCursor.getCurrentRowAsModel();
	}
	
	public void swapActiveRecordCursor(ActiveRecordCursor<T> newCursor) {
		mCursor = newCursor;
		if (newCursor != null)
			swapCursor(newCursor.getCursor());
		else
			swapCursor(null);
	}


}
