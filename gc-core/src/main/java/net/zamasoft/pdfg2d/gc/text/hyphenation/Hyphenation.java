package net.zamasoft.pdfg2d.gc.text.hyphenation;

/**
 * 言語ごとのハイフネーションです。
 * 
 * @author MIYABE Tatsuhiko
 * @version $Id: Hyphenation.java 1572 2018-07-11 07:22:55Z miyabe $
 */
public interface Hyphenation {
	/**
	 * 前後の文字が分割不可能であればtrueを返します。
	 * @param c1
	 * @param c2
	 * @return
	 */
	public boolean atomic(char c1, char c2);

	/**
	 * 前後の文字の間を広げることが可能であればtrueを返します。
	 * 
	 * @param c1
	 * @param c2
	 * @return
	 */
	public boolean canSeparate(char c1, char c2);
}
