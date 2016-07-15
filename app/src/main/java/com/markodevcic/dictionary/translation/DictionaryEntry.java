package com.markodevcic.dictionary.translation;

public final class DictionaryEntry {

	private final String deMainTerm;
	private final String[] deAltTerms;
	private final String frgnMainTerm;
	private final String[] frgnAltTerms;

	public DictionaryEntry(String deMainTerm,
							String[] deAltTerms,
							String frgnMainTerm,
							String[] frgnAltTerms) {
		this.deMainTerm = deMainTerm;
		this.deAltTerms = deAltTerms;
		this.frgnMainTerm = frgnMainTerm;
		this.frgnAltTerms = frgnAltTerms;
	}

	public String getDeMainTerm() {
		return deMainTerm;
	}

	public String[] getDeAltTerms() {
		return deAltTerms;
	}

	public String getFrgnMainTerm() {
		return frgnMainTerm;
	}

	public String[] getFrgnAltTerms() {
		return frgnAltTerms;
	}
}
