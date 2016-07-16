package com.markodevcic.dictionary.activity;

import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.widget.Toast;

import com.markodevcic.dictionary.R;
import com.markodevcic.dictionary.data.DatabaseHelper;

import rx.Observer;
import rx.Subscription;
import rx.android.schedulers.AndroidSchedulers;
import rx.schedulers.Schedulers;
import rx.subscriptions.Subscriptions;

public class StartupActivity extends AppCompatActivity {

	private DatabaseHelper databaseHelper;

	private Subscription loadSubscription = Subscriptions.unsubscribed();

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		databaseHelper = new DatabaseHelper(this);
		if (databaseHelper.getRowCount() > 0) {
			startMainActivity();
		} else {
			setContentView(R.layout.activity_startup);
			databaseHelper.startLoadDictionary()
					.subscribeOn(Schedulers.io())
					.observeOn(AndroidSchedulers.mainThread())
					.subscribe(new Observer<Void>() {
						@Override
						public void onCompleted() {
							startMainActivity();
						}

						@Override
						public void onError(Throwable e) {
							Toast.makeText(StartupActivity.this, "Error while loading data", Toast.LENGTH_LONG).show();
						}

						@Override
						public void onNext(Void voids) {

						}
					});
		}
	}

	private void startMainActivity() {
		Intent intent = new Intent(this, MainActivity.class);
		startActivity(intent);
	}

	@Override
	protected void onDestroy() {
		super.onDestroy();
		loadSubscription.unsubscribe();
	}
}