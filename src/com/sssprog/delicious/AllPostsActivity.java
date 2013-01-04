package com.sssprog.delicious;

import com.googlecode.androidannotations.annotations.EActivity;
import com.sssprog.delicious.helpers.NavigationHelper;

import android.os.Bundle;
import android.support.v4.app.Fragment;

@EActivity
public class AllPostsActivity extends BaseActivity {
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		NavigationHelper.setupListNavigation(getSupportActionBar(), this);
		setContentView(R.layout.posts_activity);
		
		if (savedInstanceState == null) {
			Fragment f = PostsFragment_.newInstance(0, false, true);
			getSupportFragmentManager().beginTransaction().add(R.id.container, f).commit();
		}
	}
	
	@Override
	protected void onResume() {
		super.onResume();
		NavigationHelper.updateNavListTitleOnResume(getSupportActionBar(), this);
	}

}
