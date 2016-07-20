package com.markodevcic.dictionary.translation;

import android.util.Pair;

import com.markodevcic.dictionary.data.DatabaseHelper;

import java.util.Arrays;

import rx.Observable;
import rx.functions.Func1;

public final class TranslationService {
	private final DatabaseHelper databaseHelper;

	public TranslationService(DatabaseHelper databaseHelper) {
		this.databaseHelper = databaseHelper;
	}

	public Observable<DictionaryEntry> startQuery(final String term) {
		return databaseHelper.startSearch(term)
				.map(new Func1<String, DictionaryEntry>() {
					@Override
					public DictionaryEntry call(String result) {
						return createEntry(result);
					}
				});
	}

	private DictionaryEntry createEntry(String result) {
		String[] splitted = result.split("::");
		Pair<String, String[]> deEntries = parseString(splitted[0]);
		Pair<String, String[]> enEntries = parseString(splitted[1]);
		return new DictionaryEntry(deEntries.first, deEntries.second, enEntries.first, enEntries.second);
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
