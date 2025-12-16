package net.zamasoft.pdfg2d.gc.text.hyphenation.impl;

import java.util.BitSet;

/**
 * A character set backed by a {@link BitSet}.
 * 
 * @author MIYABE Tatsuhiko
 * @since 1.0
 */
public class BitSetCharacterSet implements CharacterSet {
	private final BitSet set = new BitSet();

	/**
	 * Creates a new BitSetCharacterSet containing the characters in the given
	 * string.
	 * 
	 * @param str the string containing characters to include in the set
	 */
	public BitSetCharacterSet(String str) {
		for (int i = 0; i < str.length(); ++i) {
			char c = str.charAt(i);
			this.set.set(c);
		}
	}

	@Override
	public boolean contains(char c) {
		return this.set.get(c);
	}

}
