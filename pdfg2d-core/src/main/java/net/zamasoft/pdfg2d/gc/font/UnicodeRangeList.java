package net.zamasoft.pdfg2d.gc.font;

/**
 * Represents a list of unicode ranges.
 * 
 * @author MIYABE Tatsuhiko
 * @since 1.0
 */
public class UnicodeRangeList {
	private final UnicodeRange[] includes;

	/**
	 * Creates a new UnicodeRangeList.
	 * 
	 * @param includes the array of unicode ranges
	 * @throws NullPointerException if the array is null
	 */
	public UnicodeRangeList(final UnicodeRange[] includes) {
		if (includes == null) {
			throw new NullPointerException();
		}
		this.includes = includes;
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
		final var buff = new StringBuilder();
		for (int i = 0; i < this.includes.length; ++i) {
			if (i > 0) {
				buff.append(", ");
			}
			buff.append(this.includes[i]);
		}
		return buff.toString();
	}

	/**
	 * Returns whether the list is empty.
	 * 
	 * @return true if empty, false otherwise
	 */
	public boolean isEmpty() {
		return this.includes.length == 0;
	}
}
