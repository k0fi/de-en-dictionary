package com.markodevcic.dictionary.utils;

import android.support.annotation.NonNull;

public final class StringUtils {
	private static final StringBuilder stringBuilder = new StringBuilder();

	private StringUtils() {
		throw new IllegalStateException("no instnaces");
	}

	public static String join(@NonNull String[] strings, @NonNull String join) {
		int index = 1;
		for (String string : strings) {
			stringBuilder.append(index);
			stringBuilder.append(". ");
			stringBuilder.append(string);
			stringBuilder.append(join);
			index++;
		}
		String builtString = stringBuilder.toString();
		stringBuilder.setLength(0);
		return builtString;
	}
}
