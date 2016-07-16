package com.markodevcic.dictionary.data;

public final class SearchResult {

	private final String deText;
	private final String enText;

	public SearchResult(String deText, String enText) {
		this.deText = deText;
		this.enText = enText;
	}

	public String getDeText() {
		return deText;
	}

	public String getEnText() {
		return enText;
	}
}
