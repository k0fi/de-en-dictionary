package com.markodevcic.dictionary.utils;

import java.util.concurrent.Executor;
import java.util.concurrent.Executors;

/*package*/  class DbExecutor {
	
	private static final Executor DB_EXECUTOR = Executors.newSingleThreadExecutor();
	
	private DbExecutor() {
		throw new IllegalStateException("no instances");
	}
	
	/*package*/ static Executor getDbExecutor() {
		return DB_EXECUTOR;
	}
}
