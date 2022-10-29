package net.zamasoft.pdfg2d.gc.paint;

/**
 * @author MIYABE Tatsuhiko
 * @version $Id: RGBColor.java 1565 2018-07-04 11:51:25Z miyabe $
 */
public class RGBColor implements Color {
	public static final int R = 0, G = 1, B = 2;

	public static final RGBColor BLACK = new RGBColor(0, 0, 0);

	public static final RGBColor WHITE = new RGBColor(1, 1, 1);

	protected final float red, green, blue;

	public static RGBColor create(float red, float green, float blue) {
		if (red <= 0 && green <= 0 && blue <= 0) {
			return RGBColor.BLACK;
		}
		if (red >= 1 && green >= 1 && blue >= 1) {
			return RGBColor.WHITE;
		}
		return new RGBColor(red, green, blue);
	}

	protected RGBColor(float red, float green, float blue) {
		this.red = Math.min(1.0f, Math.max(0f, red));
		this.green = Math.min(1.0f, Math.max(0f, green));
		this.blue = Math.min(1.0f, Math.max(0f, blue));
	}

	public short getPaintType() {
		return COLOR;
	}

	public short getColorType() {
		return RGB;
	}

	public float getComponent(int i) {
		switch (i) {
		case 0:
			return this.red;
		case 1:
			return this.green;
		case 2:
			return this.blue;
		}
		throw new IllegalArgumentException();
	}

	public float getRed() {
		return this.red;
	}

	public float getGreen() {
		return this.green;
	}

	public float getBlue() {
		return this.blue;
	}

	public float getAlpha() {
		return 1f;
	}

	public boolean equals(Object o) {
		if (o instanceof RGBColor) {
			RGBColor color = (RGBColor) o;
			return this.red == color.red && this.green == color.green && this.blue == color.blue;
		}
		return false;
	}

	public String toString() {
		return "rgb(" + this.red + "," + this.green + "," + this.blue + ")";
	}
}