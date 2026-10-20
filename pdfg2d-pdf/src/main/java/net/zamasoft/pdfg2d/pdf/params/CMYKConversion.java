package net.zamasoft.pdfg2d.pdf.params;

/**
 * Rules applied when RGB colors and images are converted to the output intent's CMYK (2026-10-20, a publishing
 * request: dark text set in a slightly tinted black became four-color black, and the total ink was not bounded).
 *
 * @param neutralTolerance how far apart (0-255, in 8-bit sRGB steps) the components of a solid color or of every
 *                         gradient stop may be for the color to count as achromatic and be written with K only;
 *                         {@code 0} keeps only exact grays ({@code R = G = B}). Images are not affected
 * @param inkLimit         upper bound of the total area coverage C+M+Y+K in percent (for example 300), applied to
 *                         converted colors and image pixels; {@code 0} for none. Colors already given in CMYK are
 *                         not changed
 * @author MIYABE Tatsuhiko
 * @since 1.3
 */
public record CMYKConversion(int neutralTolerance, int inkLimit) {
	/** Exact grays only and no ink limit (the behavior before 2026-10-20). */
	public static final CMYKConversion DEFAULT = new CMYKConversion(0, 0);

	/**
	 * Creates the rules.
	 *
	 * @throws IllegalArgumentException if {@code neutralTolerance} is outside 0-255, or {@code inkLimit} is neither
	 *                                  0 nor within 100-400
	 */
	public CMYKConversion {
		if (neutralTolerance < 0 || neutralTolerance > 255) {
			throw new IllegalArgumentException("neutralTolerance must be within 0-255: " + neutralTolerance);
		}
		if (inkLimit != 0 && (inkLimit < 100 || inkLimit > 400)) {
			throw new IllegalArgumentException("inkLimit must be 0 or within 100-400: " + inkLimit);
		}
	}
}
