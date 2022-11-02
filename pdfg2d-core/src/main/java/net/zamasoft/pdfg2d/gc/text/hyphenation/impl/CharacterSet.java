package net.zamasoft.pdfg2d.gc.text.hyphenation.impl;

public interface CharacterSet {
	/**
	 * 全ての文字を表す文字集合です。
	 */
	public static final CharacterSet ALL = new CharacterSet() {
		public boolean contains(char c) {
			return true;
		}
	};

	/**
	 * 空の文字集合です。
	 */
	public static final CharacterSet NOTHING = new CharacterSet() {
		public boolean contains(char c) {
			return false;
		}
	};

	/**
	 * cがこの文字集合に含まれる場合trueを返します。
	 * 
	 * @param c
	 * @return
	 */
	public boolean contains(char c);
}
