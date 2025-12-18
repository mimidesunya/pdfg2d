package net.zamasoft.pdfg2d.font.table;

import java.io.Serializable;

/**
 * Represents PANOSE classification data.
 * 
 * @param code the 10-byte PANOSE code
 * @since 1.0
 * @author <a href="mailto:david@steadystate.co.uk">David Schweinsberg</a>
 */
public record Panose(byte[] code) implements Serializable {
	private static final long serialVersionUID = 0L;

	/**
	 * Creates a new Panose with the given code.
	 * 
	 * @param code the 10-byte PANOSE code
	 */
	public Panose {
		assert code.length == 10;
	}

	public byte getFamilyType() {
		return this.code[0];
	}

	public byte getSerifStyle() {
		return this.code[1];
	}

	public byte getWeight() {
		return this.code[2];
	}

	public byte getProportion() {
		return this.code[3];
	}

	public byte getContrast() {
		return this.code[4];
	}

	public byte getStrokeVariation() {
		return this.code[5];
	}

	public byte getArmStyle() {
		return this.code[6];
	}

	public byte getLetterForm() {
		return this.code[7];
	}

	public byte getMidline() {
		return this.code[8];
	}

	public byte getXHeight() {
		return this.code[9];
	}

	@Override
	public String toString() {
		final var sb = new StringBuilder();
		sb.append(this.code[0]);
		for (int i = 1; i < this.code.length; ++i) {
			sb.append(' ').append(this.code[i]);
		}
		return sb.toString();
	}
}
