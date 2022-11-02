package net.zamasoft.pdfg2d.gc.text.hyphenation;

import net.zamasoft.pdfg2d.gc.text.hyphenation.impl.JapaneseHyphenation;

public class HyphenationBundle {
	private static final Hyphenation DEFAULT_HYPHENATION = new JapaneseHyphenation();

	public static Hyphenation getHyphenation(String lang) {
		return DEFAULT_HYPHENATION;
	}
}
