package net.zamasoft.pdfg2d.gc.text.hyphenation;

/**
 * Represents hyphenation rules for a language.
 * 
 * @author MIYABE Tatsuhiko
 * @since 1.0
 */
public interface TextBreakingRules {
	/**
	 * Returns true if the two characters should not be separated.
	 * 
	 * @param c1 the preceding character
	 * @param c2 the following character
	 * @return true if atomic
	 */
	public boolean atomic(char c1, char c2);

	/**
	 * Returns true if the space between the two characters can be expanded.
	 * 
	 * @param c1 the preceding character
	 * @param c2 the following character
	 * @return true if separable
	 */
	public boolean canSeparate(char c1, char c2);
}
