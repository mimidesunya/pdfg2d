package net.zamasoft.pdfg2d.pdf;

import static org.junit.jupiter.api.Assertions.assertArrayEquals;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotSame;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.awt.image.BufferedImage;

import java.io.IOException;

import org.junit.jupiter.api.Test;

import net.zamasoft.pdfg2d.pdf.color.ColorConverter;
import net.zamasoft.pdfg2d.pdf.impl.PDFWriterImpl;
import net.zamasoft.pdfg2d.pdf.params.CMYKConversion;

/** Property tests for sRGB-to-CMYK conversion using the output intent ICC profile. */
public class ColorConverterTest {
	private static ColorConverter converter() throws IOException {
		return converter(CMYKConversion.DEFAULT);
	}

	private static ColorConverter converter(final CMYKConversion rules) throws IOException {
		try (final var in = PDFWriterImpl.class.getResourceAsStream("ISOcoated_v2_300_eci.icc")) {
			return new ColorConverter(in.readAllBytes(), rules);
		}
	}

	private static float sum(final float[] cmyk) {
		return cmyk[0] + cmyk[1] + cmyk[2] + cmyk[3];
	}

	@Test
	public void testProcessColorsHaveExpectedInkCharacteristics() throws Exception {
		final var converter = converter();
		final var red = converter.toCMYK(1, 0, 0);
		assertTrue(red[0] < .05f, "red C");
		assertTrue(red[1] > .85f, "red M");
		assertTrue(red[2] > .85f, "red Y");
		assertTrue(red[3] < .05f, "red K");

		final var blue = converter.toCMYK(0, 0, 1);
		assertTrue(blue[0] > .85f, "blue C");
		assertTrue(blue[1] > .70f, "blue M");
		assertTrue(blue[2] < .05f, "blue Y");
	}

	@Test
	public void testNeutralColorsUseBlackOnly() throws Exception {
		final var converter = converter();
		assertArrayEquals(new float[] { 0, 0, 0, 1 }, converter.toCMYK(0, 0, 0));
		assertArrayEquals(new float[] { 0, 0, 0, .5f }, converter.toCMYK(.5f, .5f, .5f));
		assertArrayEquals(new float[] { 0, 0, 0, 0 }, converter.toCMYK(1, 1, 1));
	}

	/**
	 * A tolerance makes a slightly tinted dark text color K-only (2026-10-20: #1d1a22, components 8 steps apart,
	 * became four-color black); exact grays keep their K, and a color outside the tolerance is converted by ICC.
	 */
	@Test
	public void testNeutralToleranceMakesNearGraysBlackOnly() throws Exception {
		final float r = 0x1d / 255f, g = 0x1a / 255f, b = 0x22 / 255f;
		final var strict = converter();
		assertFalse(strict.isNeutral(r, g, b));
		assertTrue(strict.toCMYK(r, g, b)[0] > .5f, "without a tolerance the tinted black is rich black");

		final var tolerant = converter(new CMYKConversion(8, 0));
		assertTrue(tolerant.isNeutral(r, g, b));
		final var cmyk = tolerant.toCMYK(r, g, b);
		assertEquals(0, cmyk[0]);
		assertEquals(0, cmyk[1]);
		assertEquals(0, cmyk[2]);
		assertEquals(1 - (.3f * r + .59f * g + .11f * b), cmyk[3], 1e-6f);
		assertArrayEquals(new float[] { 0, 0, 0, .5f }, tolerant.toCMYK(.5f, .5f, .5f), "exact gray unchanged");
		assertFalse(converter(new CMYKConversion(7, 0)).isNeutral(r, g, b), "8 steps apart exceeds 7");
		assertTrue(tolerant.toCMYK(.5f, .4f, .4f)[1] > 0, "a tinted mid tone stays an ICC color, not K only");
	}

	/** An ink limit bounds the total coverage of converted colors and image pixels; K is kept. */
	@Test
	public void testInkLimitBoundsTotalCoverage() throws Exception {
		final var plain = converter().toCMYKNoNeutralRule(.1f, .05f, .15f);
		assertTrue(sum(plain) > 2.6f, "the dark color exceeds 260% without a limit: " + sum(plain));
		final var limited = converter(new CMYKConversion(0, 260));
		final var cmyk = limited.toCMYKNoNeutralRule(.1f, .05f, .15f);
		assertEquals(2.6f, sum(cmyk), 1e-4f);
		assertTrue(cmyk[3] >= plain[3], "under-color removal moves ink into K, never out of it");
		assertArrayEquals(converter().toCMYK(.9f, .8f, .7f), limited.toCMYK(.9f, .8f, .7f),
				"a light color under the limit is unchanged");

		final var image = new BufferedImage(2, 1, BufferedImage.TYPE_INT_RGB);
		image.setRGB(0, 0, 0x1A0D26);
		image.setRGB(1, 0, 0xE6CCB3);
		final var pixels = limited.toCMYKImage(image).getRaster();
		final var dark = pixels.getPixel(0, 0, (int[]) null);
		assertTrue(dark[0] + dark[1] + dark[2] + dark[3] <= Math.round(2.6f * 255) + 2,
				"image pixel over the limit: " + java.util.Arrays.toString(dark));
		assertArrayEquals(converter().toCMYKImage(image).getRaster().getPixel(1, 0, (int[]) null),
				pixels.getPixel(1, 0, (int[]) null), "a light pixel is unchanged");
	}

	@Test
	public void testRulesRejectOutOfRangeValues() {
		assertThrows(IllegalArgumentException.class, () -> new CMYKConversion(256, 0));
		assertThrows(IllegalArgumentException.class, () -> new CMYKConversion(-1, 0));
		assertThrows(IllegalArgumentException.class, () -> new CMYKConversion(0, 99));
		assertThrows(IllegalArgumentException.class, () -> new CMYKConversion(0, 401));
	}

	@Test
	public void testCachedResultIsEqualButNotShared() throws Exception {
		final var converter = converter();
		final var first = converter.toCMYK(.2f, .4f, .8f);
		final var second = converter.toCMYK(.2f, .4f, .8f);
		assertArrayEquals(first, second);
		assertNotSame(first, second);
	}

	@Test
	public void testNeutralRuleCanBeDisabledForGradientInterpolation() throws Exception {
		final var gray = converter().toCMYKNoNeutralRule(.5f, .5f, .5f);
		for (final var component : gray) {
			assertTrue(component > 0, "ICC gray must contain all four process components");
		}
	}
}
