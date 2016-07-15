package com.markodevcic.dictionary.injection;

import com.markodevcic.dictionary.activity.MainActivity;
import com.markodevcic.dictionary.activity.StartupActivity;

import javax.inject.Singleton;

import dagger.Component;

@Component(modules = {AppModule.class})
@Singleton
public interface AppComponent {
	void inject(StartupActivity startupActivity);

	void inject(MainActivity mainActivity);
}
