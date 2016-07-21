package com.markodevcic.dictionary.translation;

public final class DictionaryEntry {

	private final String deMainTerm;
	private final String[] deAltTerms;
	private final String enMainTerm;
	private final String[] enAltTerms;

	public DictionaryEntry(String deMainTerm,
							String[] deAltTerms,
							String enMainTerm,
							String[] enAltTerms) {
		this.deMainTerm = deMainTerm;
		this.deAltTerms = deAltTerms;
		this.enMainTerm = enMainTerm;
		this.enAltTerms = enAltTerms;
	}

	public String getDeMainTerm() {
		return deMainTerm;
	}

	public String[] getDeAltTerms() {
		return deAltTerms;
	}

	public String getEnMainTerm() {
		return enMainTerm;
	}

	public String[] getEnAltTerms() {
		return enAltTerms;
	}
}
