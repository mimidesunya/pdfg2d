package net.zamasoft.pdfg2d.pdf;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.awt.geom.AffineTransform;
import java.awt.geom.Rectangle2D;
import java.io.ByteArrayOutputStream;
import java.nio.charset.StandardCharsets;

import org.junit.jupiter.api.Test;

import net.zamasoft.pdfg2d.gc.paint.CMYKColor;
import net.zamasoft.pdfg2d.gc.paint.Color;
import net.zamasoft.pdfg2d.gc.paint.LinearGradient;
import net.zamasoft.pdfg2d.gc.paint.RGBColor;
import net.zamasoft.pdfg2d.pdf.gc.PDFGC;
import net.zamasoft.pdfg2d.pdf.impl.PDFWriterImpl;
import net.zamasoft.pdfg2d.pdf.params.CMYKConversion;
import net.zamasoft.pdfg2d.pdf.params.PDFParams;
import net.zamasoft.zstream.io.impl.StreamFragmentedOutput;

/**
 * The achromatic tolerance of {@link CMYKConversion} in gradients (2026-10-20): near-gray RGB stops become K-only,
 * while stops given in CMYK keep their plates (codex review: they used to be taken for near-gray through their
 * approximate RGB and lost C, M and Y).
 */
public class CMYKConversionGradientTest {
	private static String pdf(final Color from, final Color to) throws Exception {
		final var out = new ByteArrayOutputStream();
		final var builder = new StreamFragmentedOutput(out);
		final var pdf = new PDFWriterImpl(builder, PDFParams.createDefault()
				.withCompression(PDFParams.Compression.NONE).withColorMode(PDFParams.ColorMode.CMYK)
				.withCMYKConversion(new CMYKConversion(8, 0)));
		try (final var gc = new PDFGC(pdf.nextPage(200, 200))) {
			gc.setFillPaint(new LinearGradient(0, 0, 200, 0, new double[] { 0, 1 }, new Color[] { from, to },
					new AffineTransform()));
			gc.fill(new Rectangle2D.Double(0, 0, 200, 200));
		}
		pdf.close();
		builder.close();
		return out.toString(StandardCharsets.ISO_8859_1);
	}

	@Test
	public void testNearGrayRgbStopsBecomeBlackOnly() throws Exception {
		final var raw = pdf(RGBColor.create(0x1d / 255f, 0x1a / 255f, 0x22 / 255f),
				RGBColor.create(0x3a / 255f, 0x38 / 255f, 0x3c / 255f));
		assertTrue(raw.contains("/C0 [ 0 0 0 "), "near-gray RGB stops are K-only:\n" + raw);
	}

	@Test
	public void testNearGrayCmykStopsKeepTheirPlates() throws Exception {
		final var raw = pdf(CMYKColor.create(.8f, .79f, .78f, 0), CMYKColor.create(.6f, .59f, .58f, 0));
		assertTrue(raw.contains("/C0 [ 0.8 0.79 0.78 0 ]"),
				"CMYK stops pass through:\n" + raw);
		assertFalse(raw.contains("/C0 [ 0 0 0 "), "CMYK stops must not become K-only");
	}
}
