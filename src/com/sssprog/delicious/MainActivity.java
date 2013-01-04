package com.sssprog.delicious;

import com.googlecode.androidannotations.annotations.EActivity;
import com.sssprog.delicious.helpers.NavigationHelper;

import android.content.Intent;
import android.os.Bundle;

@EActivity
public class MainActivity extends BaseActivity {
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		if (!api.isAuthenticated()) {
			startActivity(new Intent(this, LoginActivity_.class));
			finish();
			return;
		}
		
		NavigationHelper.setupListNavigation(getSupportActionBar(), this);
		
		setContentView(R.layout.main_activity);
	}
	
	@Override
	protected void onResume() {
		super.onResume();
		if (!api.isAuthenticated()) {
			startActivity(new Intent(this, LoginActivity_.class));
			finish();
			return;
		}
		NavigationHelper.updateNavListTitleOnResume(getSupportActionBar(), this);
	}

}
