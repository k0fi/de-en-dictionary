package com.markodevcic.dictionary.activity;

import android.os.Bundle;

import com.markodevcic.dictionary.data.DatabaseHelper;
import com.markodevcic.dictionary.injection.AppComponent;

import javax.inject.Inject;

public class StartupActivity extends BaseActivity {

	@Inject
	DatabaseHelper databaseHelper;

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
	}

	@Override
	protected void inject(AppComponent appComponent) {
		appComponent.inject(this);
	}
}

