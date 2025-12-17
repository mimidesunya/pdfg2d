package net.zamasoft.pdfg2d.gc.font;

import java.io.Serializable;

/**
 * Represents the Panose system for font classification.
 * 
 * @author MIYABE Tatsuhiko
 * @since 1.0
 */
public record Panose(byte familyClassId, byte familySubclass, byte familyType, byte serifStyle, byte weight,
		byte proportion, byte contrast, byte strokeVariation, byte armStyle, byte letterForm, byte midline,
		byte xHeight) implements Serializable {

	/**
	 * Creates a new Panose object.
	 * 
	 * @param sFamilyClass the family class
	 * @param panose       the panose bytes
	 */
	public Panose(final int sFamilyClass, final byte[] panose) {
		this((byte) ((sFamilyClass >> 8) & 0xFF), (byte) (sFamilyClass & 0xFF), panose[0], panose[1], panose[2],
				panose[3], panose[4], panose[5], panose[6], panose[7], panose[8], panose[9]);
	}

	@Override
	public String toString() {
		final var sb = new StringBuilder();
		sb.append(String.valueOf(this.familyClassId())).append(" ").append(String.valueOf(this.familySubclass()))
				.append(" ").append(String.valueOf(this.familyType())).append(" ")
				.append(String.valueOf(this.serifStyle()))
				.append(" ").append(String.valueOf(this.weight())).append(" ").append(String.valueOf(this.proportion()))
				.append(" ").append(String.valueOf(this.contrast())).append(" ")
				.append(String.valueOf(this.strokeVariation())).append(" ").append(String.valueOf(this.armStyle()))
				.append(" ").append(String.valueOf(this.letterForm())).append(" ")
				.append(String.valueOf(this.midline()))
				.append(" ").append(String.valueOf(this.xHeight()));
		return sb.toString();
	}
}
