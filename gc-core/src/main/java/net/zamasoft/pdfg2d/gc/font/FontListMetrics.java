package net.zamasoft.pdfg2d.gc.font;

import java.io.Serializable;

/**
 * 複数のフォントのメトリックス情報です。
 * 
 * @author MIYABE Tatsuhiko
 * @since 1.0
 */
public class FontListMetrics implements Serializable {
	private static final long serialVersionUID = -1L;

	protected final FontMetrics[] fontMetricses;

	protected double maxAscent = -1, maxDescent = -1, maxXHeight = -1;

	public FontListMetrics(FontMetrics[] fontMetricses) {
		this.fontMetricses = fontMetricses;
	}

	protected void calculate() {
		double ascent = 0, descent = 0, xHeight = 0;
		for (int i = 0; i < this.fontMetricses.length; ++i) {
			FontMetrics fontMetrics = this.fontMetricses[i];
			ascent = Math.max(ascent, fontMetrics.getAscent());
			descent = Math.max(descent, fontMetrics.getDescent());
			xHeight = Math.max(xHeight, fontMetrics.getXHeight());
		}
		this.maxAscent = ascent;
		this.maxDescent = descent;
		this.maxXHeight = xHeight;
	}

	/**
	 * @return the fontMetricses
	 */
	public int getLength() {
		return this.fontMetricses.length;
	}

	public FontMetrics getFontMetrics(int i) {
		return this.fontMetricses[i];
	}

	public double getMaxAscent() {
		if (this.maxAscent == -1) {
			this.calculate();
		}
		return this.maxAscent;
	}

	public double getMaxDescent() {
		if (this.maxDescent == -1) {
			this.calculate();
		}
		return this.maxDescent;
	}

	public double getMaxXHeight() {
		if (this.maxXHeight == -1) {
			this.calculate();
		}
		return this.maxXHeight;
	}

	public String toString() {
		StringBuffer buff = new StringBuffer(super.toString());
		buff.append(":[");
		for (int i = 0; i < this.fontMetricses.length; ++i) {
			buff.append(this.fontMetricses[i]).append(',');
		}
		buff.append("]");
		return buff.toString();
	}
}