package net.zamasoft.pdfg2d.gc.font;

import java.io.Serializable;

/**
 * Represents a range of Unicode characters.
 * 
 * @author MIYABE Tatsuhiko
 * @since 1.0
 */
public record UnicodeRange(int first, int last) implements Serializable {

	/**
	 * Parses a string representation of a unicode range.
	 * 
	 * @param s the string to parse
	 * @return the unicode range
	 * @throws NumberFormatException if the string format is invalid
	 */
	public static UnicodeRange parseRange(final String s) throws NumberFormatException {
		int first, last;
		final var hyph = s.indexOf('-');
		if (hyph != -1) {
			final var u1 = s.substring(2, hyph);
			var u2 = s.substring(hyph + 1);
			if (u2.startsWith("U+")) {
				u2 = u2.substring(2);
			}
			first = Integer.parseInt(u1, 16);
			last = Integer.parseInt(u2, 16);
		} else {
			final var u = s.substring(2);
			if (u.indexOf('?') != -1) {
				first = Integer.parseInt(u.replace('?', '0'), 16);
				last = Integer.parseInt(u.replace('?', 'F'), 16);
			} else {
				first = last = Integer.parseInt(u, 16);
			}
		}
		return new UnicodeRange(first, last);
	}

	/**
	 * Returns whether the range contains the character.
	 * 
	 * @param c the character code
	 * @return true if the range contains the character, false otherwise
	 */
	public boolean contains(final int c) {
		return (c >= this.first && c <= this.last);
	}

	@Override
	public String toString() {
		if (this.first == this.last) {
			return "U+" + Integer.toHexString(this.first);
		}
		return "U+" + Integer.toHexString(this.first) + "-" + Integer.toHexString(this.last);
	}
}
