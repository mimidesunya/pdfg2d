package net.zamasoft.pdfg2d.gc.text;

/**
 * Represents a non-character element (quad) in text.
 * 
 * @author MIYABE Tatsuhiko
 * @since 1.0
 */
public abstract class Quad implements Element {
	// U+200B ZERO WIDTH SPACE
	// U+00A0 NO-BREAK SPACE
	// U+2060 WORD JOINER

	/** Does not break the string. */
	public static final String JOIN = "";

	/** Breaks the string. */
	public static final String BREAK = "\u200B";

	/**
	 * Treated as the previous character. The Quad itself attaches before the
	 * following string.
	 */
	public static final String CONTINUE_BEFORE = "\u200B\u2060";

	/**
	 * Treated as the previous character. The Quad itself attaches after the
	 * previous string.
	 */
	public static final String CONTINUE_AFTER = "\u2060\u200B";

	@Override
	public Type getElementType() {
		return Type.QUAD;
	}

	/**
	 * Returns the corresponding string.
	 * 
	 * @return the string
	 */
	public abstract String getString();
}