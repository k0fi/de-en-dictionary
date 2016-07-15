package com.markodevcic.dictionary.utils;

import android.support.annotation.NonNull;

public final class StringUtils {
	private static final StringBuilder stringBuilder = new StringBuilder();

	public static String join(@NonNull String[] strings, @NonNull String join) {
		for (String string : strings) {
			stringBuilder.append(string);
			stringBuilder.append(join);
		}
		String builtString = stringBuilder.toString();
		stringBuilder.setLength(0);
		return builtString;
	}
}
