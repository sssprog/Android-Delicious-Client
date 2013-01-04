package com.sssprog.delicious;


import com.google.inject.AbstractModule;
import com.sssprog.delicious.api.Api;
import com.sssprog.delicious.api.ApiImpl;

public class GuiceModule extends AbstractModule {

	@Override
	protected void configure() {
		bind(Api.class).to(ApiImpl.class);
	}

}
