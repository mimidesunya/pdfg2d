package net.zamasoft.pdfg2d.gc.text;

/**
 * Represents a non-character element (control) in text.
 * 
 * @author MIYABE Tatsuhiko
 * @since 1.0
 */
public non-sealed abstract class TextControl implements Element {
	// U+200B ZERO WIDTH SPACE
	// U+00A0 NO-BREAK SPACE
	// U+2060 WORD JOINER

	/** Does not break the string. */
	public static final String JOIN = "";

	/** Breaks the string. */
	public static final String BREAK = "\u200B";

	/**
	 * Treated as the previous character. The TextControl itself attaches before the
	 * following string.
	 */
	public static final String CONTINUE_BEFORE = "\u200B\u2060";

	/**
	 * Treated as the previous character. The TextControl itself attaches after the
	 * previous string.
	 */
	public static final String CONTINUE_AFTER = "\u2060\u200B";

	/**
	 * Returns the corresponding string.
	 * 
	 * @return the string
	 */
	public abstract String getString();
}