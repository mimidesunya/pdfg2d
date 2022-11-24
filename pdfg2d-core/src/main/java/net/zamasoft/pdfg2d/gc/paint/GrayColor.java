package net.zamasoft.pdfg2d.gc.paint;

/**
 * @author MIYABE Tatsuhiko
 * @since 1.0
 */
public class GrayColor implements Color {
	public static final GrayColor WHITE = new GrayColor(1);

	public static final GrayColor BLACK = new GrayColor(0);

	protected final float gray;

	public static GrayColor create(float gray) {
		if (gray <= 0) {
			return BLACK;
		}
		if (gray >= 1) {
			return WHITE;
		}

		return new GrayColor(gray);
	}

	protected GrayColor(float gray) {
		this.gray = Math.min(1.0f, Math.max(0f, gray));
	}

	public Paint.Type getPaintType() {
		return Paint.Type.COLOR;
	}

	public Color.Type getColorType() {
		return Color.Type.GRAY;
	}

	public float getComponent(int i) {
		switch (i) {
		case 0:
			return this.gray;
		}
		throw new IllegalArgumentException();
	}

	public float getRed() {
		return this.gray;
	}

	public float getGreen() {
		return this.gray;
	}

	public float getBlue() {
		return this.gray;
	}

	public float getAlpha() {
		return 1f;
	}

	public boolean equals(Object o) {
		if (o instanceof GrayColor) {
			GrayColor color = (GrayColor) o;
			return color.gray == this.gray;
		}
		return false;
	}

	public String toString() {
		return "-cssj-gray(" + this.gray + ")";
	}
}