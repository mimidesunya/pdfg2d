package net.zamasoft.pdfg2d.gc.paint;

/**
 * @author MIYABE Tatsuhiko
 * @since 1.0
 */
/**
 * Represents a grayscale color.
 * 
 * @author MIYABE Tatsuhiko
 * @since 1.0
 */
public class GrayColor implements Color {
	/**
	 * Generally represents white color (1.0).
	 */
	public static final GrayColor WHITE = new GrayColor(1);

	/**
	 * Generally represents black color (0.0).
	 */
	public static final GrayColor BLACK = new GrayColor(0);

	protected final float gray;

	/**
	 * Creates a new GrayColor instance.
	 * 
	 * @param gray the gray value (0.0 - 1.0)
	 * @return the GrayColor instance
	 */
	public static GrayColor create(final float gray) {
		if (gray <= 0) {
			return BLACK;
		}
		if (gray >= 1) {
			return WHITE;
		}

		return new GrayColor(gray);
	}

	protected GrayColor(final float gray) {
		this.gray = Math.min(1.0f, Math.max(0f, gray));
	}

	@Override
	public Paint.Type getPaintType() {
		return Paint.Type.COLOR;
	}

	@Override
	public Color.Type getColorType() {
		return Color.Type.GRAY;
	}

	@Override
	public float getComponent(final int i) {
		if (i == 0) {
			return this.gray;
		}
		throw new IllegalArgumentException();
	}

	@Override
	public float getRed() {
		return this.gray;
	}

	@Override
	public float getGreen() {
		return this.gray;
	}

	@Override
	public float getBlue() {
		return this.gray;
	}

	@Override
	public float getAlpha() {
		return 1f;
	}

	@Override
	public boolean equals(final Object o) {
		return o instanceof GrayColor color && color.gray == this.gray;
	}

	@Override
	public String toString() {
		return "-cssj-gray(" + this.gray + ")";
	}
}