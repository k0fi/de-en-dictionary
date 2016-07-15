package com.markodevcic.dictionary.injection;

import android.content.Context;

import com.markodevcic.dictionary.data.DatabaseHelper;

import dagger.Module;
import dagger.Provides;

@Module
public class AppModule {
	private final Context appContext;

	public AppModule(Context appContext) {
		this.appContext = appContext;
	}

	@Provides
	Context providesContext() {
		return appContext;
	}

	@Provides
	DatabaseHelper providesDatabaseHelper() {
		return new DatabaseHelper(appContext);
	}
}
