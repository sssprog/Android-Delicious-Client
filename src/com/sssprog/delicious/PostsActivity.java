package com.sssprog.delicious;

import com.actionbarsherlock.view.Menu;
import com.googlecode.androidannotations.annotations.EActivity;

import android.os.Bundle;
import android.support.v4.app.Fragment;

@EActivity
public class PostsActivity extends BaseActivity {
	
	public static final String PARAM_TAG_NAME = "PARAM_TAG_NAME";
	
	public static Bundle getIntentExtras(long tagId, String tagName) {
		Bundle b = new Bundle();
		b.putLong(PostsFragment.PARAM_TAG_ID, tagId);
		b.putString(PARAM_TAG_NAME, tagName);
		return b;
	}
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.posts_activity);
		setTitle(getIntent().getStringExtra(PARAM_TAG_NAME));
		
		if (savedInstanceState == null) {
			Fragment f = PostsFragment_.newInstance(getIntent().getLongExtra(PostsFragment.PARAM_TAG_ID, 0), false, false);
			getSupportFragmentManager().beginTransaction().add(R.id.container, f).commit();
		}
	}
	
	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		super.onCreateOptionsMenu(menu);
		menu.removeItem(R.id.menu_refresh);
		return true;
	}

}
