package net.zamasoft.pdfg2d.gc.font;

import java.util.Objects;
import java.util.Arrays;

/**
 * Represents a list of unicode ranges.
 * 
 * @author MIYABE Tatsuhiko
 * @since 1.0
 */
public record UnicodeRangeList(UnicodeRange[] includes) {

	public UnicodeRangeList {
		Objects.requireNonNull(includes);
	}

	/**
	 * Returns whether the character can be displayed.
	 * 
	 * @param c the character code
	 * @return true if displayable, false otherwise
	 */
	public boolean canDisplay(final int c) {
		if (this.includes.length == 0) {
			return true;
		}
		for (int i = 0; i < this.includes.length; ++i) {
			if (this.includes[i].contains(c)) {
				return true;
			}
		}
		return false;
	}

	@Override
	public String toString() {
		return java.util.Arrays.stream(this.includes)
				.map(UnicodeRange::toString)
				.collect(java.util.stream.Collectors.joining(", "));
	}

	/**
	 * Returns whether the list is empty.
	 * 
	 * @return true if empty, false otherwise
	 */
	public boolean isEmpty() {
		return this.includes.length == 0;
	}

	@Override
	public boolean equals(final Object o) {
		return o instanceof UnicodeRangeList u && Arrays.equals(this.includes, u.includes);
	}

	@Override
	public int hashCode() {
		return Arrays.hashCode(this.includes);
	}
}
