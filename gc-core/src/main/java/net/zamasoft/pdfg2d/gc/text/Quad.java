package net.zamasoft.pdfg2d.gc.text;

/**
 * テキスト中の文字以外の埋め物です。
 * 
 * @author MIYABE Tatsuhiko
 * @version $Id: Quad.java 1565 2018-07-04 11:51:25Z miyabe $
 */
public abstract class Quad implements Element {
	// U+200B ZERO WIDTH SPACE
	// U+00A0 NO-BREAK SPACE
	// U+2060 WORD JOINER
	/** 文字列を区切りません。 */
	public static final String JOIN = "";
	/** 文字列を区切ります。 */
	public static final String BREAK = "\u200B";
	/** 前の文字として扱います。Quad自体は後の文字列の前にくっつきます。 */
	public static final String CONTINUE_BEFORE = "\u200B\u2060";
	/** 前の文字として扱います。Quad自体は前の文字列の後にくっつきます。 */
	public static final String CONTINUE_AFTER = "\u2060\u200B";

	public short getElementType() {
		return Element.QUAD;
	}

	/**
	 * 相当する文字列です。
	 * 
	 * @return
	 */
	public abstract String getString();
}