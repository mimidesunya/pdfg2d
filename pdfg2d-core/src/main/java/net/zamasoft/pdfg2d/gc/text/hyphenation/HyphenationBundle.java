package net.zamasoft.pdfg2d.gc.text.hyphenation;

import net.zamasoft.pdfg2d.gc.text.hyphenation.impl.JapaneseHyphenation;

/**
 * A bundle of hyphenation rules.
 * 
 * @author MIYABE Tatsuhiko
 * @since 1.0
 */
public class HyphenationBundle {
	private static final Hyphenation DEFAULT_HYPHENATION = new JapaneseHyphenation();

	/**
	 * Returns the hyphenation rules for the specified language.
	 * 
	 * @param lang the language code
	 * @return the hyphenation rules
	 */
	public static Hyphenation getHyphenation(String lang) {
		return DEFAULT_HYPHENATION;
	}
}
