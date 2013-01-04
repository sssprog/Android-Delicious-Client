package com.sssprog.delicious;

import android.content.Intent;
import android.os.Bundle;
import android.support.v4.app.LoaderManager.LoaderCallbacks;
import android.support.v4.content.Loader;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.ListView;

import com.googlecode.androidannotations.annotations.EFragment;
import com.googlecode.androidannotations.annotations.ViewById;
import com.sssprog.activerecord.helpers.ARUtils;
import com.sssprog.activerecord.helpers.ActiveRecordCursorLoader;
import com.sssprog.delicious.dbmodels.TagModel;
import com.sssprog.delicious.helpers.LogHelper;
import com.sssprog.activerecord.ActiveRecordCursor;

@EFragment
public class TagsFragment extends BaseFragment implements LoaderCallbacks<ActiveRecordCursor<TagModel>> {
	
	@ViewById
	ListView listView;
	SimpleActiveRecordCursorAdapter<TagModel> mAdapter;
	
	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container,
			Bundle savedInstanceState) {
		return inflater.inflate(R.layout.tags_fragment, container, false);
	}
	
	@Override
	public void onActivityCreated(Bundle savedInstanceState) {
		super.onActivityCreated(savedInstanceState);
		mAdapter = new SimpleActiveRecordCursorAdapter<TagModel>(getActivity(),
                R.layout.item_tag, null,
                new String[] { ARUtils.toSqlName("name"), ARUtils.toSqlName("count") },
                new int[] { R.id.tvName, R.id.tvCount }, 0);
        listView.setAdapter(mAdapter);
        
        listView.setOnItemClickListener(new OnItemClickListener() {

			@Override
			public void onItemClick(AdapterView<?> arg0, View arg1, int position, long arg3) {
				TagModel tag = (TagModel) listView.getItemAtPosition(position);
				Intent intent = new Intent(getActivity(), PostsActivity_.class);
				intent.putExtras(PostsActivity.getIntentExtras(tag.getID(), tag.name));
				startActivity(intent);
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
	protected void onDbDataChanged() {
		if (getActivity() != null)
			getLoaderManager().getLoader(0).forceLoad();
	}

	@Override
	public Loader<ActiveRecordCursor<TagModel>> onCreateLoader(int id, Bundle args) {
		return new ActiveRecordCursorLoader<TagModel>(getActivity(), false, TagModel.class, 
				null, null, null, null, null, ARUtils.toSqlName("count") + " DESC", null);
	}

	@Override
	public void onLoadFinished(Loader<ActiveRecordCursor<TagModel>> loader,
			ActiveRecordCursor<TagModel> data) {
		mAdapter.swapActiveRecordCursor(data);
	}

	@Override
	public void onLoaderReset(Loader<ActiveRecordCursor<TagModel>> loader) {
		mAdapter.swapActiveRecordCursor(null);
	}

}
