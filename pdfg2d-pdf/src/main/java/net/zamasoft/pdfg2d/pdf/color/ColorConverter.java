package net.zamasoft.pdfg2d.pdf.color;

import java.awt.Transparency;
import java.awt.color.ColorSpace;
import java.awt.color.ICC_ColorSpace;
import java.awt.color.ICC_Profile;
import java.awt.image.BufferedImage;
import java.awt.image.ColorConvertOp;
import java.awt.image.ComponentColorModel;
import java.awt.image.DataBuffer;
import java.awt.image.Raster;
import java.util.HashMap;
import java.util.Map;
import java.util.Objects;

import net.zamasoft.pdfg2d.pdf.params.CMYKConversion;
import net.zamasoft.pdfg2d.util.ColorUtils;

/**
 * Converts sRGB colors to the output intent's CMYK ICC profile.
 * <p>
 * The JDK public API cannot select a rendering intent;
 * {@link ICC_ColorSpace#fromRGB(float[])} always uses perceptual.
 * Each {@code PDFWriterImpl} owns one instance, so the cache is unsynchronized
 * and the instance itself is not thread-safe.
 * </p>
 *
 * @author MIYABE Tatsuhiko
 * @since 1.3
 */
public final class ColorConverter {
	private static final ColorSpace SRGB = ColorSpace.getInstance(ColorSpace.CS_sRGB);

	private record RGBKey(int red, int green, int blue) {
		static RGBKey of(final float red, final float green, final float blue) {
			return new RGBKey(Float.floatToIntBits(red), Float.floatToIntBits(green),
					Float.floatToIntBits(blue));
		}
	}

	private final ICC_ColorSpace cmykColorSpace;
	private final Map<RGBKey, float[]> cache = new HashMap<>();
	/** Achromatic tolerance as a component difference (0-1). */
	private final float neutralTolerance;
	/** Total ink limit as a sum of components (1-4), or 0 for none. */
	private final float inkLimit;

	/**
	 * Creates a converter using a CMYK ICC profile and the default rules (exact grays only, no ink limit).
	 * Treats input values as components of the JDK-defined {@link ColorSpace#CS_sRGB}.
	 *
	 * @param cmykProfile bytes of the CMYK ICC profile
	 * @throws NullPointerException     if the profile is {@code null}
	 * @throws IllegalArgumentException if not an ICC profile or not CMYK
	 */
	public ColorConverter(final byte[] cmykProfile) {
		this(cmykProfile, CMYKConversion.DEFAULT);
	}

	/**
	 * Creates a converter using a CMYK ICC profile and conversion rules.
	 * Treats input values as components of the JDK-defined {@link ColorSpace#CS_sRGB}.
	 *
	 * @param cmykProfile bytes of the CMYK ICC profile
	 * @param rules       achromatic tolerance and total ink limit
	 * @throws NullPointerException     if the profile or the rules are {@code null}
	 * @throws IllegalArgumentException if not an ICC profile or not CMYK
	 * @since 1.3
	 */
	public ColorConverter(final byte[] cmykProfile, final CMYKConversion rules) {
		Objects.requireNonNull(cmykProfile, "cmykProfile");
		Objects.requireNonNull(rules, "rules");
		final var profile = ICC_Profile.getInstance(cmykProfile);
		if (profile.getColorSpaceType() != ColorSpace.TYPE_CMYK || profile.getNumComponents() != 4) {
			throw new IllegalArgumentException("A four-component CMYK ICC profile is required.");
		}
		this.cmykColorSpace = new ICC_ColorSpace(profile);
		this.neutralTolerance = rules.neutralTolerance() / 255f;
		this.inkLimit = rules.inkLimit() / 100f;
	}

	/**
	 * Converts sRGB to CMYK. For achromatic colors ({@link #isNeutral}), returns K-only output so black text and
	 * similar content do not become rich black.
	 *
	 * @return a new four-component array owned exclusively by the caller
	 */
	public float[] toCMYK(final float red, final float green, final float blue) {
		if (this.isNeutral(red, green, blue)) {
			return new float[] { 0, 0, 0, this.neutralK(red, green, blue) };
		}
		return this.toCMYKNoNeutralRule(red, green, blue);
	}

	/**
	 * Returns whether a color counts as achromatic: exactly gray, or with components no further apart than the
	 * tolerance of the rules.
	 *
	 * @since 1.3
	 */
	public boolean isNeutral(final float red, final float green, final float blue) {
		if (red == green && green == blue) {
			return true;
		}
		return this.neutralTolerance > 0 && Math.max(red, Math.max(green, blue))
				- Math.min(red, Math.min(green, blue)) <= this.neutralTolerance + 1e-6f;
	}

	/**
	 * Returns the K of an achromatic color: {@code 1 - red} for an exact gray (unchanged before 2026-10-20),
	 * otherwise one minus the luminance gray mode uses ({@link ColorUtils#toGray(float, float, float)}).
	 *
	 * @since 1.3
	 */
	public float neutralK(final float red, final float green, final float blue) {
		if (red == green && green == blue) {
			return 1 - red;
		}
		return Math.max(0, Math.min(1, 1 - ColorUtils.toGray(red, green, blue)));
	}

	/**
	 * Performs pure ICC conversion from sRGB without mapping achromatic colors to K-only output.
	 * Used for gradient stops and mesh vertices.
	 *
	 * @return a new four-component array owned exclusively by the caller
	 */
	public float[] toCMYKNoNeutralRule(final float red, final float green, final float blue) {
		final var key = RGBKey.of(red, green, blue);
		var converted = this.cache.get(key);
		if (converted == null) {
			final var rgb = new float[SRGB.getNumComponents()];
			rgb[0] = red;
			rgb[1] = green;
			rgb[2] = blue;
			converted = this.cmykColorSpace.fromRGB(rgb);
			if (this.inkLimit > 0) {
				limitInk(converted, this.inkLimit);
			}
			this.cache.put(key, converted);
		}
		return converted.clone();
	}

	/**
	 * Converts all pixels of an RGB image to the output intent's CMYK color space.
	 * If the image color space is {@link ICC_ColorSpace}, uses its profile as input;
	 * otherwise, treats it as sRGB. Preserves alpha in the output image without conversion.
	 *
	 * @param source RGB image
	 * @return 8-bit CMYK image (CMYK+A if alpha is present)
	 */
	public BufferedImage toCMYKImage(final BufferedImage source) {
		Objects.requireNonNull(source, "source");
		final var sourceColorSpace = source.getColorModel().getColorSpace() instanceof ICC_ColorSpace icc
				? icc : SRGB;
		final var hasAlpha = source.getColorModel().hasAlpha();
		final var colorModel = new ComponentColorModel(this.cmykColorSpace, hasAlpha, false,
				hasAlpha ? Transparency.TRANSLUCENT : Transparency.OPAQUE, DataBuffer.TYPE_BYTE);
		final var raster = Raster.createInterleavedRaster(DataBuffer.TYPE_BYTE, source.getWidth(),
				source.getHeight(), hasAlpha ? 5 : 4, null);
		final var converted = new BufferedImage(colorModel, raster, false, null);
		new ColorConvertOp(sourceColorSpace, this.cmykColorSpace, null).filter(source, converted);
		if (this.inkLimit > 0) {
			final var bands = raster.getNumBands();
			final var samples = ((java.awt.image.DataBufferByte) raster.getDataBuffer()).getData();
			final var limit = Math.round(this.inkLimit * 255);
			final var cmyk = new float[4];
			for (var i = 0; i + 3 < samples.length; i += bands) {
				if ((samples[i] & 0xFF) + (samples[i + 1] & 0xFF) + (samples[i + 2] & 0xFF)
						+ (samples[i + 3] & 0xFF) <= limit) {
					continue;
				}
				for (var c = 0; c < 4; ++c) {
					cmyk[c] = (samples[i + c] & 0xFF) / 255f;
				}
				limitInk(cmyk, this.inkLimit);
				for (var c = 0; c < 4; ++c) {
					samples[i + c] = (byte) Math.round(cmyk[c] * 255);
				}
			}
		}
		return converted;
	}

	/**
	 * Lowers the total C+M+Y+K to {@code limit}: first moves the common part of C, M and Y into K (under-color
	 * removal: taking x from each and adding x to K lowers the total by 2x and keeps the darkness), then scales C, M
	 * and Y down for what is left. K itself is never lowered (limits are at least 100%).
	 */
	static void limitInk(final float[] cmyk, final float limit) {
		var excess = cmyk[0] + cmyk[1] + cmyk[2] + cmyk[3] - limit;
		if (excess <= 0) {
			return;
		}
		final var moved = Math.min(Math.min(cmyk[0], Math.min(cmyk[1], cmyk[2])),
				Math.min(1 - cmyk[3], excess / 2));
		if (moved > 0) {
			cmyk[0] -= moved;
			cmyk[1] -= moved;
			cmyk[2] -= moved;
			cmyk[3] += moved;
			excess -= 2 * moved;
		}
		final var cmy = cmyk[0] + cmyk[1] + cmyk[2];
		if (excess > 1e-6f && cmy > 0) {
			final var scale = Math.max(0, cmy - excess) / cmy;
			cmyk[0] *= scale;
			cmyk[1] *= scale;
			cmyk[2] *= scale;
		}
	}
}
