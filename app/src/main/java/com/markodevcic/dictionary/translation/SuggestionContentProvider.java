package com.markodevcic.dictionary.translation;

import android.app.SearchManager;
import android.content.ContentProvider;
import android.content.ContentValues;
import android.database.Cursor;
import android.database.MatrixCursor;
import android.net.Uri;
import android.provider.BaseColumns;
import android.support.annotation.Nullable;

import java.util.Arrays;
import java.util.List;

public final class SuggestionContentProvider extends ContentProvider{

	@Override
	public boolean onCreate() {
		return false;
	}

	@Nullable
	@Override
	public Cursor query(Uri uri, String[] projection, String selection, String[] selectionArgs, String sortOrder) {
		MatrixCursor cursor = new MatrixCursor(
				new String[] {
						BaseColumns._ID,
						SearchManager.SUGGEST_COLUMN_TEXT_1,
						SearchManager.SUGGEST_COLUMN_INTENT_DATA
				}
		);
		List<String> strings = Arrays.asList(new String[]{"brand", "fire", "house"});
		String query = selectionArgs == null ? "" : selectionArgs[0].toUpperCase();
		int limit = Integer.parseInt(uri.getQueryParameter(SearchManager.SUGGEST_PARAMETER_LIMIT));

		int lenght = strings.size();
		for (int i = 0; i < lenght; i++) {
			String city = strings.get(i);
			if (city.toUpperCase().contains(query)){
				cursor.addRow(new Object[]{ i, city, city});
			}
		}

		return cursor;
	}

	@Nullable
	@Override
	public String getType(Uri uri) {
		return null;
	}

	@Nullable
	@Override
	public Uri insert(Uri uri, ContentValues values) {
		return null;
	}

	@Override
	public int delete(Uri uri, String selection, String[] selectionArgs) {
		return 0;
	}

	@Override
	public int update(Uri uri, ContentValues values, String selection, String[] selectionArgs) {
		return 0;
	}
}
