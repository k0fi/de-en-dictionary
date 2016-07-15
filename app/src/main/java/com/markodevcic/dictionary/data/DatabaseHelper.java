package com.markodevcic.dictionary.data;

import android.content.Context;
import android.database.Cursor;
import android.database.DatabaseUtils;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.database.sqlite.SQLiteStatement;
import android.util.Log;
import android.util.Pair;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.List;

import rx.Observable;
import rx.Subscriber;

public final class DatabaseHelper extends SQLiteOpenHelper {

	public static final String TABLE_FTS = "fts_table";
	public static final String COL_EN_TEXT = "text_en";
	public static final String COL_DE_TEXT = "text_de";

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

	public List<Pair<String, String>> search(String query) {
		SQLiteDatabase database = getReadableDatabase();
		String sql = "SELECT * FROM " + TABLE_FTS + " WHERE " + TABLE_FTS + " MATCH ? ORDER BY OFFSETS(" + TABLE_FTS + ") ASC LIMIT 20 OFFSET 0";
		String offsetSQL = "SELECT offsets(" + TABLE_FTS + ") FROM " + TABLE_FTS + " WHERE " + TABLE_FTS + " MATCH ? ORDER BY OFFSETS(" + TABLE_FTS + ") ASC LIMIT 20 OFFSET 0";
		Cursor cursor = database.rawQuery(sql, new String[]{query});
		Cursor offsetCursor = database.rawQuery(offsetSQL, new String[]{query});
		if (offsetCursor.moveToFirst()) {
			do {
				Log.d("DB", offsetCursor.getString(0));
			} while (offsetCursor.moveToNext());
		}
		offsetCursor.close();
		List<Pair<String, String>> result = new ArrayList<>();
		if (cursor.moveToFirst()) {
			do {
				String deText = cursor.getString(cursor.getColumnIndex(COL_DE_TEXT));
				String enText = cursor.getString(cursor.getColumnIndex(COL_EN_TEXT));
				result.add(new Pair<>(deText, enText));
			} while (cursor.moveToNext());
		}
		cursor.close();
		database.close();
		return result;
	}

	public Observable<Pair<String, String>> startSearch(final String query) {
		return Observable.create(new Observable.OnSubscribe<Pair<String, String>>() {
			@Override
			public void call(Subscriber<? super Pair<String, String>> subscriber) {
				SQLiteDatabase database = getReadableDatabase();
				String sql = "SELECT * FROM " + TABLE_FTS + " WHERE " + TABLE_FTS + " MATCH ? ORDER BY MATCHINFO(" + TABLE_FTS +")  DESC LIMIT 40 OFFSET 0";
				Cursor cursor = database.rawQuery(sql, new String[]{query});
				if (cursor.moveToFirst()) {
					do {
						String deText = cursor.getString(cursor.getColumnIndex(COL_DE_TEXT));
						String enText = cursor.getString(cursor.getColumnIndex(COL_EN_TEXT));
						subscriber.onNext(new Pair<>(deText, enText));
					} while (cursor.moveToNext() && !subscriber.isUnsubscribed());
				}
				cursor.close();
				database.close();
			}
		});
	}

	public void loadDictionary() {
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
			database.beginTransaction();
			while ((line = reader.readLine()) != null) {
				String[] splitted = line.split("::");
				insertStatement.clearBindings();
				insertStatement.bindString(1, splitted[0].trim());
				insertStatement.bindString(2, splitted[1].trim());
				insertStatement.executeInsert();
			}
			database.setTransactionSuccessful();
		} finally {
			database.endTransaction();
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