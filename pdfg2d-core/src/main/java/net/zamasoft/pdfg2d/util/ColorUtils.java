package net.zamasoft.pdfg2d.util;

import net.zamasoft.pdfg2d.gc.paint.CMYKColor;
import net.zamasoft.pdfg2d.gc.paint.Color;
import net.zamasoft.pdfg2d.gc.paint.GrayColor;
import net.zamasoft.pdfg2d.gc.paint.RGBAColor;
import net.zamasoft.pdfg2d.gc.paint.RGBColor;

public final class ColorUtils {
	private ColorUtils() {
		// unused
	}

	public static Color toGray(Color color) {
		float gray;
		switch (color.getColorType()) {
		case RGB:
			gray = ColorUtils.toGray(color.getComponent(RGBColor.R), color.getComponent(RGBColor.G),
					color.getComponent(RGBColor.B));
			break;
		case CMYK:
			gray = ColorUtils.toGray(color.getComponent(CMYKColor.C), color.getComponent(CMYKColor.M),
					color.getComponent(CMYKColor.Y), color.getComponent(CMYKColor.K));
			break;
		case GRAY:
			return color;
		case RGBA:
			gray = ColorUtils.toGray(color.getComponent(RGBAColor.R), color.getComponent(RGBAColor.G),
					color.getComponent(RGBAColor.B));
			return RGBAColor.create(gray, gray, gray, color.getComponent(RGBAColor.A));

		default:
			throw new IllegalStateException();
		}
		return GrayColor.create(gray);
	}

	public static float toGray(float r, float g, float b) {
		// PDF 6.2.1 による
		return (float) (0.3 * r + 0.59 * g + 0.11 * b);
	}

	public static float toGray(float c, float m, float y, float k) {
		// PDF 6.2.2 による
		return (float) (1.0 - Math.min(1.0, 0.3 * c + 0.59 * m + 0.11 * y + k));
	}

	public static CMYKColor toCMYK(Color color) {
		float c, m, y, k;
		switch (color.getColorType()) {
		case RGB:
		case RGBA:
			// PDF 6.2.3 による
			c = 1.0f - color.getComponent(RGBColor.R);
			m = 1.0f - color.getComponent(RGBColor.G);
			y = 1.0f - color.getComponent(RGBColor.B);
			k = Math.min(Math.min(c, m), y);
			c = Math.max(0, c - k);
			m = Math.max(0, m - k);
			y = Math.max(0, y - k);
			break;
		case CMYK:
			return (CMYKColor) color;
		case GRAY:
			c = m = y = 0;
			k = 1.0f - color.getComponent(0);
			break;

		default:
			throw new IllegalStateException();
		}
		return CMYKColor.create(c, m, y, k);
	}
}
