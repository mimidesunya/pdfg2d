package net.zamasoft.pdfg2d.gc.text.hyphenation.impl;

import java.util.BitSet;

public class BitSetCharacterSet implements CharacterSet {
	private final BitSet set = new BitSet();

	public BitSetCharacterSet(String str) {
		for (int i = 0; i < str.length(); ++i) {
			char c = str.charAt(i);
			this.set.set(c);
		}
	}

	public boolean contains(char c) {
		return this.set.get(c);
	}

}
