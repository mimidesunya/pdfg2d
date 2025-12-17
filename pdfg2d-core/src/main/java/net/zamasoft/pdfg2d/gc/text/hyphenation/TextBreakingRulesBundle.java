package net.zamasoft.pdfg2d.gc.text.hyphenation;

import net.zamasoft.pdfg2d.gc.text.hyphenation.impl.JapaneseHyphenation;

/**
 * A bundle of hyphenation rules.
 * 
 * @author MIYABE Tatsuhiko
 * @since 1.0
 */
public class TextBreakingRulesBundle {
	private static final TextBreakingRules DEFAULT_RULES = new JapaneseHyphenation();

	/**
	 * Returns the hyphenation rules for the specified language.
	 * 
	 * @param lang the language code
	 * @return the hyphenation rules
	 */
	public static TextBreakingRules getRules(String lang) {
		return DEFAULT_RULES;
	}
}
