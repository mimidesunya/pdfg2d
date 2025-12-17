package net.zamasoft.pdfg2d.gc.text.breaking;

import net.zamasoft.pdfg2d.gc.text.breaking.impl.JapaneseBreakingRules;

/**
 * A bundle of hyphenation rules.
 * 
 * @author MIYABE Tatsuhiko
 * @since 1.0
 */
public class TextBreakingRulesBundle {
	private static final TextBreakingRules DEFAULT_RULES = new JapaneseBreakingRules();

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
