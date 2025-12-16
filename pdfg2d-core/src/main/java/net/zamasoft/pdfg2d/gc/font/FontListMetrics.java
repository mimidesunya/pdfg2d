package net.zamasoft.pdfg2d.gc.font;

import java.io.Serializable;

/**
 * 複数のフォントのメトリックス情報です。
 * 
 * @author MIYABE Tatsuhiko
 * @since 1.0
 */
/**
 * Represents the metrics information for multiple fonts.
 * 
 * @author MIYABE Tatsuhiko
 * @since 1.0
 */
public class FontListMetrics implements Serializable {
	private static final long serialVersionUID = -1L;

	protected final FontMetrics[] fontMetricses;

	protected double maxAscent = -1, maxDescent = -1, maxXHeight = -1;

	/**
	 * Creates a new FontListMetrics.
	 * 
	 * @param fontMetricses the array of font metrics
	 */
	public FontListMetrics(final FontMetrics[] fontMetricses) {
		this.fontMetricses = fontMetricses;
	}

	protected void calculate() {
		double ascent = 0, descent = 0, xHeight = 0;
		for (int i = 0; i < this.fontMetricses.length; ++i) {
			final var fontMetrics = this.fontMetricses[i];
			ascent = Math.max(ascent, fontMetrics.getAscent());
			descent = Math.max(descent, fontMetrics.getDescent());
			xHeight = Math.max(xHeight, fontMetrics.getXHeight());
		}
		this.maxAscent = ascent;
		this.maxDescent = descent;
		this.maxXHeight = xHeight;
	}

	/**
	 * Returns the number of font metrics.
	 * 
	 * @return the number of font metrics
	 */
	public int getLength() {
		return this.fontMetricses.length;
	}

	/**
	 * Returns the font metrics at the specified index.
	 * 
	 * @param i the index
	 * @return the font metrics
	 */
	public FontMetrics getFontMetrics(final int i) {
		return this.fontMetricses[i];
	}

	/**
	 * Returns the maximum ascent among the fonts.
	 * 
	 * @return the maximum ascent
	 */
	public double getMaxAscent() {
		if (this.maxAscent == -1) {
			this.calculate();
		}
		return this.maxAscent;
	}

	/**
	 * Returns the maximum descent among the fonts.
	 * 
	 * @return the maximum descent
	 */
	public double getMaxDescent() {
		if (this.maxDescent == -1) {
			this.calculate();
		}
		return this.maxDescent;
	}

	/**
	 * Returns the maximum x-height among the fonts.
	 * 
	 * @return the maximum x-height
	 */
	public double getMaxXHeight() {
		if (this.maxXHeight == -1) {
			this.calculate();
		}
		return this.maxXHeight;
	}

	@Override
	public String toString() {
		final var buff = new StringBuilder(super.toString());
		buff.append(":[");
		for (int i = 0; i < this.fontMetricses.length; ++i) {
			buff.append(this.fontMetricses[i]).append(',');
		}
		buff.append("]");
		return buff.toString();
	}
}