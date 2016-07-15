package com.markodevcic.dictionary.translation;

import android.util.Pair;

import com.markodevcic.dictionary.data.DatabaseHelper;

import java.util.Arrays;

import javax.inject.Inject;

import rx.Observable;
import rx.functions.Func1;

public final class TranslationService {
	private final DatabaseHelper databaseHelper;

	@Inject
	public TranslationService(DatabaseHelper databaseHelper) {
		this.databaseHelper = databaseHelper;
	}

	public Observable<DictionaryEntry> startQuery(final String term) {
		return databaseHelper.startSearch(term)
				.map(new Func1<Pair<String, String>, DictionaryEntry>() {
					@Override
					public DictionaryEntry call(Pair<String, String> result) {
						return createEntry(result);
					}
				});
	}

	private DictionaryEntry createEntry(Pair<String, String> result) {
		Pair<String, String[]> deEntries = parseString(result.first);
		Pair<String, String[]> frngEntries = parseString(result.second);
		return new DictionaryEntry(deEntries.first, deEntries.second, frngEntries.first, frngEntries.second);
	}

	private Pair<String, String[]> parseString(String text) {
		String[] terms = text.split("\\|");
		String mainTerm = terms[0];
		String[] altTerms = null;
		if (terms.length > 1) {
			altTerms = Arrays.copyOfRange(terms, 1, terms.length);
		}
		return new Pair<>(mainTerm, altTerms);
	}
}
