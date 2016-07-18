package com.markodevcic.dictionary.data;

import android.content.Context;
import android.database.Cursor;
import android.database.DatabaseUtils;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.database.sqlite.SQLiteStatement;
import android.util.Pair;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;

import rx.Observable;
import rx.Subscriber;

public final class DatabaseHelper extends SQLiteOpenHelper {

	public static final String TABLE_FTS = "fts_table";
	public static final String COL_EN_TEXT = "text_en";
	public static final String COL_DE_TEXT = "text_de";

	private static final String SEARCH_SQL = "SELECT * FROM " + TABLE_FTS + " WHERE " + TABLE_FTS + " MATCH ? ORDER BY MATCHINFO(" + TABLE_FTS + ", 'x')  DESC LIMIT 50 OFFSET 0";
	private static final String DB_NAME = "dict_db";
	private static final int DB_VERSION = 1;

	private final Context context;

	public DatabaseHelper(Context context) {
		super(context, DB_NAME, null, DB_VERSION);
		this.context = context;
	}

	@Override
	public void onCreate(SQLiteDatabase db) {
		db.execSQL("CREATE VIRTUAL TABLE " + TABLE_FTS + " USING FTS4 (" + COL_DE_TEXT + ", " + COL_EN_TEXT + ")");
	}

	@Override
	public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {

	}

	public Observable<Pair<String, String>> startSearch(final String query) {
		return Observable.create(new Observable.OnSubscribe<Pair<String, String>>() {
			@Override
			public void call(Subscriber<? super Pair<String, String>> subscriber) {
				SQLiteDatabase database = getReadableDatabase();
				Cursor cursor = database.rawQuery(SEARCH_SQL, new String[]{query + "*"});
				try {
					if (cursor.moveToFirst() && !subscriber.isUnsubscribed()) {
						do {
							String deText = cursor.getString(cursor.getColumnIndex(COL_DE_TEXT));
							String enText = cursor.getString(cursor.getColumnIndex(COL_EN_TEXT));
							subscriber.onNext(new Pair<>(deText, enText));
						} while (cursor.moveToNext() && !subscriber.isUnsubscribed());
					}
					if (!subscriber.isUnsubscribed()) {
						subscriber.onCompleted();
					}
				} catch (Exception e) {
					if (!subscriber.isUnsubscribed()) {
						subscriber.onError(e);
					}
				}finally {
					database.close();
					cursor.close();
				}
			}
		});
	}

	public Observable<Void> startLoadDictionary() {
		return Observable.create(new Observable.OnSubscribe<Void>() {
			@Override
			public void call(Subscriber<? super Void> subscriber) {
				try {
					loadWords();
					subscriber.onCompleted();
				} catch (IOException e) {
					subscriber.onError(e);
				}
			}
		});
	}

	public void loadDictionaryAsync() {
		new Thread(new Runnable() {
			public void run() {
				try {
					loadWords();
				} catch (IOException e) {
					throw new RuntimeException(e);
				}
			}
		}).start();
	}

	private void loadWords() throws IOException {
		InputStream inputStream = context.getAssets().open("de-en.txt");
		SQLiteDatabase database = getWritableDatabase();
		try (BufferedReader reader = new BufferedReader(new InputStreamReader(inputStream))) {
			String insertSql = "INSERT INTO " + TABLE_FTS + " (" + COL_DE_TEXT + ", " + COL_EN_TEXT + ")" + " VALUES (?,?)";
			String line;
			SQLiteStatement insertStatement = database.compileStatement(insertSql);
			database.execSQL("PRAGMA synchronous = OFF");
			database.beginTransaction();
			while ((line = reader.readLine()) != null) {
				String[] splitted = line.split("::");
				insertStatement.bindString(1, splitted[0].trim());
				insertStatement.bindString(2, splitted[1].trim());
				insertStatement.executeInsert();
			}
			database.setTransactionSuccessful();
		} finally {
			database.endTransaction();
			database.execSQL("PRAGMA synchronous = ON");
			database.close();
		}
	}

	public long getRowCount() {
		SQLiteDatabase database = getReadableDatabase();
		long count = DatabaseUtils.queryNumEntries(getReadableDatabase(), TABLE_FTS);
		database.close();
		return count;
	}
}