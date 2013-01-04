package com.sssprog.delicious;

import java.text.DateFormat;
import java.util.Date;
import java.util.Locale;

import android.annotation.SuppressLint;
import android.content.ActivityNotFoundException;
import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.support.v4.app.LoaderManager.LoaderCallbacks;
import android.support.v4.content.Loader;
import android.util.Log;
import android.util.SparseBooleanArray;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.AbsListView;
import android.widget.AdapterView.OnItemLongClickListener;
import android.widget.Checkable;
import android.widget.ListView;
import android.widget.TextView;

import com.actionbarsherlock.view.ActionMode;
import com.actionbarsherlock.view.Menu;
import com.actionbarsherlock.view.MenuInflater;
import com.actionbarsherlock.view.MenuItem;
import com.github.rtyley.android.sherlock.roboguice.activity.RoboSherlockFragmentActivity;
import com.google.inject.Inject;
import com.googlecode.androidannotations.annotations.EFragment;
import com.googlecode.androidannotations.annotations.ViewById;
import com.sssprog.activerecord.helpers.ARUtils;
import com.sssprog.activerecord.helpers.ActiveRecordCursorAdapter;
import com.sssprog.activerecord.ActiveRecordCursor;
import com.sssprog.activerecord.CacheableActiveRecordCursor;
import com.sssprog.activerecord.helpers.ActiveRecordCursorLoader;
import com.sssprog.delicious.api.Api;
import com.sssprog.delicious.api.ApiResult;
import com.sssprog.delicious.api.CallbackFragment;
import com.sssprog.delicious.api.ApiResult.ApiRestRequestStatus;
import com.sssprog.delicious.dbmodels.PostModel;
import com.sssprog.delicious.dbmodels.TagPostModel;
import com.sssprog.delicious.dialogs.ProgressDialogFragment;
import com.sssprog.delicious.helpers.ApiHelper;
import com.sssprog.delicious.helpers.LogHelper;

@EFragment
public class PostsFragment extends BaseFragment implements LoaderCallbacks<ActiveRecordCursor<PostModel>> {
	
	public static final String PARAM_TAG_ID = "PARAM_TAG_ID";
	public static final String PARAM_RECENT = "PARAM_RECENT";
	public static final String PARAM_SHOW_ALL = "PARAM_SHOW_ALL";
	
	public static final String PROGRESS_DIALOG = "PROGRESS_DIALOG";
	
	public static PostsFragment_ newInstance(long tagId, boolean recent, boolean all) {
		PostsFragment_ f = new PostsFragment_();
		Bundle b = new Bundle();
		b.putLong(PARAM_TAG_ID, tagId);
		b.putBoolean(PARAM_RECENT, recent);
		b.putBoolean(PARAM_SHOW_ALL, all);
		f.setArguments(b);
		return f;
	}
	
	@Inject
	private Api api;
	@ViewById
	ListView listView;
	PostsAdapter mAdapter;
	private long tagId;
	private boolean mRecent;
	private boolean mAll;
	private ActionMode mActionMode;
	
	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container,
			Bundle savedInstanceState) {
		return inflater.inflate(R.layout.posts_fragment, container, false);
	}
	
	@Override
	public void onActivityCreated(Bundle savedInstanceState) {
		super.onActivityCreated(savedInstanceState);
		tagId = getArguments().getLong(PARAM_TAG_ID);
		mRecent = getArguments().getBoolean(PARAM_RECENT);
		mAll = getArguments().getBoolean(PARAM_SHOW_ALL);
		mAdapter = new PostsAdapter(getActivity(), null);
        listView.setAdapter(mAdapter);
        listView.setItemsCanFocus(false);
        
        listView.setOnItemClickListener(new OnItemClickListener() {

        	@Override
        	public void onItemClick(AdapterView<?> arg0, View arg1, int position, long arg3) {
        		if (mActionMode != null) {
        			if (listSelectionIsEmpty())
        				mActionMode.finish();
        			else
        				mActionMode.invalidate();
        		} else {
	        		PostModel item = (PostModel) listView.getItemAtPosition(position);
	        		if (item.href != null) {
	        			Uri uri = Uri.parse(item.href);
	        			Intent intent = new Intent(Intent.ACTION_VIEW, uri);
	        			try {
	        				startActivity(intent);
	        			} catch (ActivityNotFoundException e) {}
	        		}
        		}
        	}
        });
        
        listView.setOnItemLongClickListener(new OnItemLongClickListener() {

			@Override
			public boolean onItemLongClick(AdapterView<?> arg0, View arg1, int position, long arg3) {
				if (mActionMode == null) {
					mActionMode = getSherlockActivity().startActionMode(new MyActionMode());
//					listView.setChoiceMode(AbsListView.CHOICE_MODE_MULTIPLE);
					listView.setChoiceMode(AbsListView.CHOICE_MODE_SINGLE);
					listView.setItemChecked(position, true);
					return true;
				}
				return false;
			}
		});

        getLoaderManager().initLoader(0, null, this);
	}
	
	@Override
	protected void onSyncStatusChanged(boolean syncing) {
		if (!syncing && getActivity() != null)
			getLoaderManager().getLoader(0).forceLoad();
	}
	
	@Override
	public Loader<ActiveRecordCursor<PostModel>> onCreateLoader(int id, Bundle args) {
		ActiveRecordCursorLoader<PostModel> res;
		if (mAll) {
			res = new ActiveRecordCursorLoader<PostModel>(getActivity(), false, PostModel.class, null, null, null, null, null, "description", null);
		} else if (mRecent) {
			res = new ActiveRecordCursorLoader<PostModel>(getActivity(), false, PostModel.class, null, null, null, null, null, "creationDate DESC", "0, 10");
		} else {
			String sql = String.format(Locale.US,  "SELECT a.* FROM %s AS a, %s AS b ON b.postId=a._id WHERE b.tagId=%d ORDER BY description",
					ARUtils.getTableName(PostModel.class), ARUtils.getTableName(TagPostModel.class), tagId);
			res = new ActiveRecordCursorLoader<PostModel>(getActivity(), PostModel.class, sql, null);
		}
		res.setUseCacheableCursor(true);
		res.setMaxItemsNumberInCache(0);
		return res;
	}

	@Override
	public void onLoadFinished(Loader<ActiveRecordCursor<PostModel>> loader,
			ActiveRecordCursor<PostModel> data) {
		mAdapter.swapCursor(data);
	}

	@Override
	public void onLoaderReset(Loader<ActiveRecordCursor<PostModel>> loader) {
		mAdapter.swapCursor(null);
	}
	
	private void uncheckListViewItems() {
		SparseBooleanArray ids = listView.getCheckedItemPositions();
		if (ids != null) {
			for (int i = 0, n = ids.size(); i < n; i++) {
				if (ids.valueAt(i))
					listView.setItemChecked(ids.keyAt(i), false);
			}
		}
	}
	
	private boolean listSelectionIsEmpty() {
		SparseBooleanArray ids = listView.getCheckedItemPositions();
		if (ids != null) {
			for (int i = 0, n = ids.size(); i < n; i++) {
				if (ids.valueAt(i))
					return false;
			}
		}
		return true;
	}
	
	private int numberOfSelectedItems() {
		SparseBooleanArray ids = listView.getCheckedItemPositions();
		int res = 0;
		if (ids != null) {
			for (int i = 0, n = ids.size(); i < n; i++) {
				if (ids.valueAt(i))
					res++;
			}
		}
		return res;
	}
	
	@Override
	protected void onDbDataChanged() {
		if (getActivity() != null)
			getLoaderManager().getLoader(0).forceLoad();
	}
	
	private PostModel getFirstSelectedItem() {
		SparseBooleanArray ids = listView.getCheckedItemPositions();
		for (int i = 0, n = ids.size(); i < n; i++) {
			if (!ids.valueAt(i))
				continue;
			PostModel post = (PostModel) listView.getItemAtPosition(ids.keyAt(i));
			return post;
		}
		return null;
	}
	
	@Override
	public void onApiResult(ApiResult result) {
		super.onApiResult(result);
		ProgressDialogFragment d = (ProgressDialogFragment) getActivity().getSupportFragmentManager().findFragmentByTag(PROGRESS_DIALOG);
		d.dismiss();
		if (result.restRequestStatus == ApiRestRequestStatus.RestStatusOk) {
			mActionMode.finish();
		} else {
			ApiHelper.showError(result.restRequestStatus);
		}
	}
	
	/**
	 * ActionMode
	 */
	private class MyActionMode implements ActionMode.Callback {

		@Override
		public boolean onCreateActionMode(ActionMode mode, Menu menu) {
			MenuInflater inflater = getSherlockActivity().getSupportMenuInflater();
			inflater.inflate(R.menu.posts_context, menu);
			return true;
		}

		@Override
		public boolean onPrepareActionMode(ActionMode mode, Menu menu) {
			final MenuItem editItem = menu.findItem(R.id.menu_edit);
			if (numberOfSelectedItems() > 1)
				editItem.setVisible(false);
			else
				editItem.setVisible(true);
			return false;
		}

		@Override
		public boolean onActionItemClicked(ActionMode mode, MenuItem item) {
			switch (item.getItemId()) {
			case R.id.menu_edit: {
				PostModel post = getFirstSelectedItem();
				if (post != null) {
					Intent intent = new Intent(getActivity(), EditPostActivity_.class);
					intent.putExtras(EditPostActivity_.getIntentExtras(post));
					startActivity(intent);
					mActionMode.finish();
				}
				return true;
			}
			
			case R.id.menu_delete: {
				PostModel post = getFirstSelectedItem();
				if (post != null) {
					ProgressDialogFragment d = ProgressDialogFragment.newInstance(R.string.saving);
					// setRetainInstance(true) so if process terminated when dialog
					// is showing, after app restart dialog won't block UI forever
					d.setRetainInstance(true);
					d.show(getActivity().getSupportFragmentManager(), PROGRESS_DIALOG);
					
					CallbackFragment<Void> f = new CallbackFragment<Void>();
					getActivity().getSupportFragmentManager().beginTransaction().add(f, null).commit();
					api.deletePost(post, f.getApiCallback());
				}
				return true;
			}
			
			}
			return false;
		}

		@Override
		public void onDestroyActionMode(ActionMode mode) {
			mActionMode = null;
			uncheckListViewItems();
			listView.setChoiceMode(AbsListView.CHOICE_MODE_NONE);
		}
		
	}
	
	/**
	 * List adapter
	 */
	private static class PostsAdapter extends ActiveRecordCursorAdapter<PostModel> {
		
		LayoutInflater mLayoutInflater;
		
		public PostsAdapter(Context context, ActiveRecordCursor<PostModel> cursor) {
			super(cursor);
			mLayoutInflater = LayoutInflater.from(context);
		}

		@SuppressLint("NewApi")
		@Override
		public View getView(int position, View convertView, ViewGroup parent) {
			final ViewHolder holder;
			if (convertView == null) {
				convertView = mLayoutInflater.inflate(R.layout.item_post, parent, false);
				holder = new ViewHolder();
				holder.tvName = (TextView) convertView.findViewById(R.id.tvName);
				holder.tvLink = (TextView) convertView.findViewById(R.id.tvLink);
				holder.tvTags = (TextView) convertView.findViewById(R.id.tvTags);
				holder.tvDate = (TextView) convertView.findViewById(R.id.tvDate);
				convertView.setTag(holder);
			} else {
				holder = (ViewHolder) convertView.getTag();
			}
			
			if ((convertView instanceof Checkable) && ((AbsListView) parent).getChoiceMode() == AbsListView.CHOICE_MODE_NONE) {
				((Checkable) convertView).setChecked(false);
			}
			
			PostModel item = getItem(position);
			holder.tvName.setText(item.description);
			holder.tvLink.setText(item.href);
			holder.tvTags.setText(item.tags);
			DateFormat df = DateFormat.getDateTimeInstance(DateFormat.MEDIUM, DateFormat.SHORT);
			String d = df.format(new Date(item.creationDate));
			holder.tvDate.setText(d);
			return convertView;
		}
		
		static class ViewHolder {
			TextView tvName;
			TextView tvLink;
			TextView tvTags;
			TextView tvDate;
		}
		
	}

}
