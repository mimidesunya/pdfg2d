package net.zamasoft.pdfg2d.gc.paint;

import java.io.Serializable;

/**
 * @author MIYABE Tatsuhiko
 * @version $Id: CMYKColor.java 1565 2018-07-04 11:51:25Z miyabe $
 */
public class CMYKColor implements Color, Serializable {
	private static final long serialVersionUID = 1L;
	public static final int C = 0, M = 1, Y = 2, K = 3;

	/**
	 * オーバープリントしない。
	 */
	public static byte OVERPRINT_NONE = 0;

	/**
	 * 標準のオーバープリントモード。
	 */
	public static byte OVERPRINT_STANDARD = 1;

	/**
	 * アドビイラストレータのオーバープリントモード。
	 */
	public static byte OVERPRINT_ILLUSTRATOR = 2;

	/**
	 * オーバプリントの適用。
	 */
	public byte overprint = OVERPRINT_NONE;

	public static CMYKColor create(float cyan, float magenta, float yellow, float black) {
		return new CMYKColor(cyan, magenta, yellow, black);
	}

	public static CMYKColor create(float cyan, float magenta, float yellow, float black, byte overprint) {
		return new CMYKColor(cyan, magenta, yellow, black, overprint);
	}

	private final float cyan, magenta, yellow, black;

	protected CMYKColor(float cyan, float magenta, float yellow, float black) {
		this(cyan, magenta, yellow, black, OVERPRINT_NONE);
	}

	protected CMYKColor(float cyan, float magenta, float yellow, float black, byte overprint) {
		this.cyan = Math.min(1.0f, Math.max(0f, cyan));
		this.magenta = Math.min(1.0f, Math.max(0f, magenta));
		this.yellow = Math.min(1.0f, Math.max(0f, yellow));
		this.black = Math.min(1.0f, Math.max(0f, black));
		this.overprint = overprint;
	}

	public short getPaintType() {
		return COLOR;
	}

	public short getColorType() {
		return CMYK;
	}

	public float getComponent(int i) {
		switch (i) {
		case C:
			return this.cyan;
		case M:
			return this.magenta;
		case Y:
			return this.yellow;
		case K:
			return this.black;
		}
		throw new IllegalArgumentException();
	}

	public float getRed() {
		return Math.max(0, 1.0f - (this.cyan + this.black));
	}

	public float getGreen() {
		return Math.max(0, 1.0f - (this.magenta + this.black));
	}

	public float getBlue() {
		return Math.max(0, 1.0f - (this.yellow + this.black));
	}

	public float getAlpha() {
		return 1f;
	}

	public byte getOverprint() {
		return this.overprint;
	}

	public boolean equals(Object o) {
		if (o instanceof CMYKColor) {
			CMYKColor color = (CMYKColor) o;
			return this.cyan == color.cyan && this.magenta == color.magenta && this.yellow == color.yellow
					&& this.black == color.black && this.overprint == color.overprint;
		}
		return false;
	}

	public String toString() {
		return "-cssj-cmyk(" + this.cyan + "," + this.magenta + "," + this.yellow + "," + this.black + ","
				+ this.overprint + ")";
	}
}