package net.zamasoft.pdfg2d.gc.paint;

public class RGBAColor extends RGBColor {
	public static final int A = 3;

	protected final float alpha;

	public static RGBColor create(float red, float green, float blue, float alpha) {
		if (alpha == 1) {
			return RGBColor.create(red, green, blue);
		}
		return new RGBAColor(red, green, blue, alpha);
	}

	protected RGBAColor(float red, float green, float blue, float alpha) {
		super(red, green, blue);
		this.alpha = alpha;
	}

	public Color.Type getColorType() {
		return Color.Type.RGBA;
	}

	public float getComponent(int i) {
		switch (i) {
		case 0:
			return this.red;
		case 1:
			return this.green;
		case 2:
			return this.blue;
		case 3:
			return this.alpha;
		}
		throw new IllegalArgumentException();
	}

	public float getAlpha() {
		return this.alpha;
	}

	public boolean equals(Object o) {
		if (o instanceof RGBAColor) {
			RGBAColor color = (RGBAColor) o;
			return this.red == color.red && this.green == color.green && this.blue == color.blue
					&& this.alpha == color.alpha;
		}
		return false;
	}

	public String toString() {
		return "rgba(" + this.red + "," + this.green + "," + this.blue + "," + this.alpha + ")";
	}
}
