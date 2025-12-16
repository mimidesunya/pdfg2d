package net.zamasoft.pdfg2d.gc.paint;

public class RGBAColor extends RGBColor {
	public static final int A = 3;

	protected final float alpha;

	/**
	 * Creates a new RGB(A) color.
	 * 
	 * @param red   the red component
	 * @param green the green component
	 * @param blue  the blue component
	 * @param alpha the alpha component
	 * @return the RGB or RGBA color
	 */
	public static RGBColor create(final float red, final float green, final float blue, final float alpha) {
		if (alpha == 1) {
			return RGBColor.create(red, green, blue);
		}
		return new RGBAColor(red, green, blue, alpha);
	}

	protected RGBAColor(final float red, final float green, final float blue, final float alpha) {
		super(red, green, blue);
		this.alpha = alpha;
	}

	@Override
	public Color.Type getColorType() {
		return Color.Type.RGBA;
	}

	@Override
	public float getComponent(final int i) {
		return switch (i) {
			case 0 -> this.red;
			case 1 -> this.green;
			case 2 -> this.blue;
			case 3 -> this.alpha;
			default -> throw new IllegalArgumentException();
		};
	}

	@Override
	public float getAlpha() {
		return this.alpha;
	}

	@Override
	public boolean equals(final Object o) {
		return o instanceof RGBAColor color
				&& this.red == color.red && this.green == color.green && this.blue == color.blue
				&& this.alpha == color.alpha;
	}

	@Override
	public String toString() {
		return "rgba(" + this.red + "," + this.green + "," + this.blue + "," + this.alpha + ")";
	}
}
