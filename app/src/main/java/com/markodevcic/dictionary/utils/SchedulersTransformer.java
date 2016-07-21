package com.markodevcic.dictionary.utils;

import rx.Observable;
import rx.android.schedulers.AndroidSchedulers;
import rx.schedulers.Schedulers;

public class SchedulersTransformer<T> implements Observable.Transformer<T, T> {
	@Override
	public Observable<T> call(Observable<T> tObservable) {
		return tObservable.subscribeOn(Schedulers.io())
				.observeOn(AndroidSchedulers.mainThread());
	}
}
