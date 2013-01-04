package com.sssprog.delicious;


import android.content.Intent;
import android.content.pm.ActivityInfo;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.Log;
import android.view.View;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.TextView;

import com.actionbarsherlock.view.Menu;
import com.actionbarsherlock.view.MenuItem;
import com.googlecode.androidannotations.annotations.EActivity;
import com.googlecode.androidannotations.annotations.ViewById;
import com.sssprog.delicious.api.ApiResult;
import com.sssprog.delicious.api.CallbackFragment;
import com.sssprog.delicious.api.ApiResult.ApiRestRequestStatus;
import com.sssprog.delicious.dbmodels.PostModel;
import com.sssprog.delicious.dialogs.ProgressDialogFragment;
import com.sssprog.delicious.helpers.ApiHelper;
import com.sssprog.delicious.helpers.LogHelper;

@EActivity
public class EditPostActivity extends BaseActivity {
	public static final String PARAM_POST = "PARAM_POST";
	public static final String PROGRESS_DIALOG = "PROGRESS_DIALOG";
	
	public static Bundle getIntentExtras(PostModel post) {
		Bundle b = new Bundle();
		if (post == null)
			post = new PostModel();
		b.putSerializable(PARAM_POST, post);
		return b;
	}
	
	@ViewById
	EditText etUrl;
	@ViewById
	TextView tvUrl;
	@ViewById
	EditText etDescription;
	@ViewById
	EditText etExtended;
	@ViewById
	EditText etTags;
	@ViewById
	CheckBox cbPrivate;
	
	private PostModel mPost;
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.edit_post_activity);
		LogHelper.i("-TAG-", "onCreate EditPostActivity " + savedInstanceState);
		mPost = (PostModel) getIntent().getSerializableExtra(PARAM_POST);
		if (mPost.getID() > 0) {
			setTitle(R.string.title_activity_edit_post);
			etUrl.setVisibility(View.GONE);
		} else {
			setTitle(R.string.title_activity_add_post);
			tvUrl.setVisibility(View.GONE);
		}
		if (savedInstanceState == null) {
			tvUrl.setText(mPost.href);
			etDescription.setText(mPost.description);
			etExtended.setText(mPost.extended);
			etTags.setText(mPost.tags);
			cbPrivate.setChecked(mPost.privatePost);
		}
	}
	
	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		getSupportMenuInflater().inflate(R.menu.edit_link_activity, menu);
		return true;
	}
	
	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		switch (item.getItemId()) {
		case R.id.menu_cancel:
			finish();
			return true;
			
		case R.id.menu_save:
			boolean cancel = false;
			etUrl.setError(null);
			etDescription.setError(null);
			View focusView = null;
			if (TextUtils.isEmpty(etDescription.getText().toString())) {
				cancel = true;
				etDescription.setError(getString(R.string.error_field_required));
				focusView = etDescription;
			}
			if (mPost.getID() <= 0 && TextUtils.isEmpty(etUrl.getText().toString())) {
				cancel = true;
				etUrl.setError(getString(R.string.error_field_required));
				focusView = etUrl;
			}
			
			if (cancel) {
				focusView.requestFocus();
			} else {
				ProgressDialogFragment d = ProgressDialogFragment.newInstance(R.string.saving);
				// setRetainInstance(true) so if process terminated when dialog
				// is showing, after app restart dialog won't block UI forever
				d.setRetainInstance(true);
				d.show(getSupportFragmentManager(), PROGRESS_DIALOG);
				
				if (mPost.getID() <= 0) {
					mPost.href = etUrl.getText().toString();
					mPost.creationDate = System.currentTimeMillis();
				}
				mPost.description = etDescription.getText().toString();
				mPost.extended = etExtended.getText().toString();
				mPost.privatePost = cbPrivate.isChecked();
				mPost.tags = ApiHelper.tagsToList(etTags.getText().toString()).value2;
				
				CallbackFragment<Void> f = new CallbackFragment<Void>();
				getSupportFragmentManager().beginTransaction().add(f, null).commit();
				api.savePost(mPost, f.getApiCallback());
			}
			return true;
		}
		return super.onOptionsItemSelected(item);
	}
	
	@Override
	public void onApiResult(@SuppressWarnings("rawtypes") ApiResult result) {
		super.onApiResult(result);
		if (result.restRequestStatus == ApiRestRequestStatus.RestStatusOk) {
			finish();
		} else {
			ProgressDialogFragment d = (ProgressDialogFragment) getSupportFragmentManager().findFragmentByTag(PROGRESS_DIALOG);
			d.dismiss();
			Log.i("-TAG-", "result = " + result);
			ApiHelper.showError(result.restRequestStatus);
		}
	}

}
