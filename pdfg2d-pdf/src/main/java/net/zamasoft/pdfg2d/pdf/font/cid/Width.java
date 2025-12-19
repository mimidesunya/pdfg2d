package net.zamasoft.pdfg2d.pdf.font.cid;

import java.io.Serializable;
import java.util.Arrays;

/**
 * Represents a width entry for a range of characters.
 *
 * @param firstCode The first character code.
 * @param lastCode  The last character code.
 * @param widths    The list of character widths.
 */
public record Width(int firstCode, int lastCode, short[] widths) implements Serializable {
	private static final long serialVersionUID = 0L;

	/**
	 * Creates a width entry for a single code.
	 *
	 * @param code   The character code.
	 * @param widths The list of character widths.
	 */
	public Width(final int code, final short[] widths) {
		this(code, code, widths);
	}

	/**
	 * Creates a width entry for code 0.
	 *
	 * @param widths The list of character widths.
	 */
	public Width(final short[] widths) {
		this(0, 0, widths);
	}

	/**
	 * Returns the width for the given character code.
	 *
	 * @param code The character code.
	 * @return The width.
	 */
	public short getWidth(final int code) {
		assert (code >= this.firstCode && code <= this.lastCode);
		final int index = code - this.firstCode;
		if (index >= this.widths.length) {
			return this.widths[this.widths.length - 1];
		}
		return this.widths[index];
	}

	@Override
	public String toString() {
		final var buff = new StringBuilder();
		buff.append(this.firstCode).append(' ');
		buff.append(this.lastCode);
		for (final short width : this.widths) {
			buff.append(' ');
			buff.append(width);
		}
		return buff.toString();
	}

	@Override
	public boolean equals(final Object o) {
		if (this == o) {
			return true;
		}
		if (!(o instanceof Width other)) {
			return false;
		}
		return this.firstCode == other.firstCode
				&& this.lastCode == other.lastCode
				&& Arrays.equals(this.widths, other.widths);
	}

	@Override
	public int hashCode() {
		int result = Integer.hashCode(this.firstCode);
		result = 31 * result + Integer.hashCode(this.lastCode);
		result = 31 * result + Arrays.hashCode(this.widths);
		return result;
	}
}
