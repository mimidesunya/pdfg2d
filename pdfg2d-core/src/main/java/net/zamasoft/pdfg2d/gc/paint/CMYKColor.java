package net.zamasoft.pdfg2d.gc.paint;

import java.io.Serializable;

/**
 * Represents a CMYK color.
 * 
 * @author MIYABE Tatsuhiko
 * @since 1.0
 */
public class CMYKColor implements Color, Serializable {
	private static final long serialVersionUID = 1L;
	public static final int C = 0, M = 1, Y = 2, K = 3;

	/**
	 * No overprint.
	 */
	public static final byte OVERPRINT_NONE = 0;

	/**
	 * Standard overprint mode.
	 */
	public static final byte OVERPRINT_STANDARD = 1;

	/**
	 * Adobe Illustrator overprint mode.
	 */
	public static final byte OVERPRINT_ILLUSTRATOR = 2;

	/**
	 * Overprint applying mode.
	 */
	public final byte overprint;

	/**
	 * Creates a CMYK color.
	 * 
	 * @param cyan    the cyan component
	 * @param magenta the magenta component
	 * @param yellow  the yellow component
	 * @param black   the black component
	 * @return the CMYK color
	 */
	public static CMYKColor create(final float cyan, final float magenta, final float yellow, final float black) {
		return new CMYKColor(cyan, magenta, yellow, black);
	}

	/**
	 * Creates a CMYK color with overprint.
	 * 
	 * @param cyan      the cyan component
	 * @param magenta   the magenta component
	 * @param yellow    the yellow component
	 * @param black     the black component
	 * @param overprint the overprint mode
	 * @return the CMYK color
	 */
	public static CMYKColor create(final float cyan, final float magenta, final float yellow, final float black,
			final byte overprint) {
		return new CMYKColor(cyan, magenta, yellow, black, overprint);
	}

	private final float cyan, magenta, yellow, black;

	protected CMYKColor(final float cyan, final float magenta, final float yellow, final float black) {
		this(cyan, magenta, yellow, black, OVERPRINT_NONE);
	}

	protected CMYKColor(final float cyan, final float magenta, final float yellow, final float black,
			final byte overprint) {
		this.cyan = Math.min(1.0f, Math.max(0f, cyan));
		this.magenta = Math.min(1.0f, Math.max(0f, magenta));
		this.yellow = Math.min(1.0f, Math.max(0f, yellow));
		this.black = Math.min(1.0f, Math.max(0f, black));
		this.overprint = overprint;
	}

	@Override
	public Paint.Type getPaintType() {
		return Paint.Type.COLOR;
	}

	@Override
	public Color.Type getColorType() {
		return Color.Type.CMYK;
	}

	@Override
	public float getComponent(final int i) {
		return switch (i) {
			case C -> this.cyan;
			case M -> this.magenta;
			case Y -> this.yellow;
			case K -> this.black;
			default -> throw new IllegalArgumentException();
		};
	}

	@Override
	public float getRed() {
		return Math.max(0, 1.0f - (this.cyan + this.black));
	}

	@Override
	public float getGreen() {
		return Math.max(0, 1.0f - (this.magenta + this.black));
	}

	@Override
	public float getBlue() {
		return Math.max(0, 1.0f - (this.yellow + this.black));
	}

	@Override
	public float getAlpha() {
		return 1f;
	}

	public byte getOverprint() {
		return this.overprint;
	}

	@Override
	public boolean equals(final Object o) {
		return o instanceof CMYKColor color
				&& this.cyan == color.cyan && this.magenta == color.magenta && this.yellow == color.yellow
				&& this.black == color.black && this.overprint == color.overprint;
	}

	@Override
	public String toString() {
		return "-cssj-cmyk(" + this.cyan + "," + this.magenta + "," + this.yellow + "," + this.black + ","
				+ this.overprint + ")";
	}
}