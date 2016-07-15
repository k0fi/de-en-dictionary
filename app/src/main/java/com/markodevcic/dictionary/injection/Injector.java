package com.markodevcic.dictionary.injection;

import com.markodevcic.dictionary.DictionaryApplication;

public enum Injector {
	INSTANCE;

	private AppComponent appComponent;

	public AppComponent getAppComponent() {
		return appComponent;
	}

	public void initialize(DictionaryApplication application) {
		appComponent = DaggerAppComponent.builder()
				.appModule(new AppModule(application))
				.build();
	}
}
