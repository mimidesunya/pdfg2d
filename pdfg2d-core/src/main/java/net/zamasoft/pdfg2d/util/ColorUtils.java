package net.zamasoft.pdfg2d.util;

import net.zamasoft.pdfg2d.gc.paint.CMYKColor;
import net.zamasoft.pdfg2d.gc.paint.Color;
import net.zamasoft.pdfg2d.gc.paint.GrayColor;
import net.zamasoft.pdfg2d.gc.paint.RGBAColor;
import net.zamasoft.pdfg2d.gc.paint.RGBColor;

/**
 * Utility methods for color conversion.
 * 
 * @author MIYABE Tatsuhiko
 * @since 1.0
 */
public final class ColorUtils {
	private ColorUtils() {
		// unused
	}

	/**
	 * Converts the given color to a grayscale color.
	 * 
	 * @param color the color to simple convert
	 * @return the grayscale color
	 */
	public static Color toGray(final Color color) {
		final float gray;
		switch (color.getColorType()) {
			case RGB -> gray = ColorUtils.toGray(color.getComponent(RGBColor.R), color.getComponent(RGBColor.G),
					color.getComponent(RGBColor.B));
			case CMYK -> gray = ColorUtils.toGray(color.getComponent(CMYKColor.C), color.getComponent(CMYKColor.M),
					color.getComponent(CMYKColor.Y), color.getComponent(CMYKColor.K));
			case GRAY -> {
				return color;
			}
			case RGBA -> {
				gray = ColorUtils.toGray(color.getComponent(RGBAColor.R), color.getComponent(RGBAColor.G),
						color.getComponent(RGBAColor.B));
				return RGBAColor.create(gray, gray, gray, color.getComponent(RGBAColor.A));
			}
			default -> throw new IllegalStateException();
		}
		return GrayColor.create(gray);
	}

	/**
	 * Calculates grayscale value from RGB components.
	 * 
	 * @param r red component
	 * @param g green component
	 * @param b blue component
	 * @return gray value
	 */
	public static float toGray(final float r, final float g, final float b) {
		// PDF 6.2.1
		return (float) (0.3 * r + 0.59 * g + 0.11 * b);
	}

	/**
	 * Calculates grayscale value from CMYK components.
	 * 
	 * @param c cyan component
	 * @param m magenta component
	 * @param y yellow component
	 * @param k key (black) component
	 * @return gray value
	 */
	public static float toGray(final float c, final float m, final float y, final float k) {
		// PDF 6.2.2
		return (float) (1.0 - Math.min(1.0, 0.3 * c + 0.59 * m + 0.11 * y + k));
	}

	/**
	 * Converts the given color to CMYK color.
	 * 
	 * @param color the color to convert
	 * @return the CMYK color
	 */
	public static CMYKColor toCMYK(final Color color) {
		float c, m, y, k;
		switch (color.getColorType()) {
			case RGB, RGBA -> {
				// PDF 6.2.3
				c = 1.0f - color.getComponent(RGBColor.R);
				m = 1.0f - color.getComponent(RGBColor.G);
				y = 1.0f - color.getComponent(RGBColor.B);
				k = Math.min(Math.min(c, m), y);
				c = Math.max(0, c - k);
				m = Math.max(0, m - k);
				y = Math.max(0, y - k);
			}
			case CMYK -> {
				return (CMYKColor) color;
			}
			case GRAY -> {
				c = m = y = 0;
				k = 1.0f - color.getComponent(0);
			}
			default -> throw new IllegalStateException();
		}
		return CMYKColor.create(c, m, y, k);
	}
}
