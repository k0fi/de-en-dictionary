package com.markodevcic.dictionary.translation;

import android.content.SearchRecentSuggestionsProvider;

public final class SuggestionProvider extends SearchRecentSuggestionsProvider {
	public final static String AUTHORITY = "com.markodevcic.dictionary.translation.SuggestionProvider";
	public final static int MODE = DATABASE_MODE_QUERIES;

	public SuggestionProvider() {
		setupSuggestions(AUTHORITY, MODE);
	}
}
