package com.markodevcic.dictionary;

import android.app.Application;

import com.markodevcic.dictionary.injection.Injector;

public final class DictionaryApplication extends Application{
	@Override
	public void onCreate() {
		super.onCreate();
		Injector.INSTANCE.initialize(this);
	}
}
