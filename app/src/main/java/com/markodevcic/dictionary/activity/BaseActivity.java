package com.markodevcic.dictionary.activity;

import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;

import com.markodevcic.dictionary.injection.AppComponent;
import com.markodevcic.dictionary.injection.Injector;

public abstract class BaseActivity extends AppCompatActivity {

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		inject(Injector.INSTANCE.getAppComponent());
	}

	protected abstract void inject(AppComponent appComponent);
}
