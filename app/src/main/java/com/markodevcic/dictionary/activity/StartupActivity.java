package com.markodevcic.dictionary.activity;

import android.content.Intent;
import android.os.Bundle;

import com.markodevcic.dictionary.R;
import com.markodevcic.dictionary.data.DatabaseHelper;
import com.markodevcic.dictionary.injection.AppComponent;

import javax.inject.Inject;

import rx.Observer;
import rx.android.schedulers.AndroidSchedulers;
import rx.schedulers.Schedulers;

public class StartupActivity extends BaseActivity {

	@Inject
	DatabaseHelper databaseHelper;

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		if (databaseHelper.getRowCount() > 0) {
			Intent intent = new Intent(this, MainActivity.class);
			startActivity(intent);
		} else {
			setContentView(R.layout.activity_startup);
			databaseHelper.startLoadDictionary()
					.subscribeOn(Schedulers.io())
					.observeOn(AndroidSchedulers.mainThread())
					.subscribe(new Observer<Void>() {
						@Override
						public void onCompleted() {

						}

						@Override
						public void onError(Throwable e) {

						}

						@Override
						public void onNext(Void aVoid) {

						}
					});
		}
	}

	@Override
	protected void inject(AppComponent appComponent) {
		appComponent.inject(this);
	}
}

