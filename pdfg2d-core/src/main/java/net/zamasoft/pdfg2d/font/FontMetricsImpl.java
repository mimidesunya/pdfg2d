package net.zamasoft.pdfg2d.font;

import java.io.IOException;

import net.zamasoft.pdfg2d.gc.font.FontMetrics;
import net.zamasoft.pdfg2d.gc.font.FontStyle;
import net.zamasoft.pdfg2d.gc.font.FontStyle.Direction;

public class FontMetricsImpl implements FontMetrics {
	private static final long serialVersionUID = 1L;

	protected final FontStore fontStore;

	protected final FontSource source;

	protected final double size, xheight;

	protected final double ascent, descent;

	protected Font font = null;

	public FontMetricsImpl(final FontStore fontStore, final FontSource fontSource, final FontStyle fontStyle) {
		this.fontStore = fontStore;
		this.source = fontSource;
		this.size = fontStyle.getSize();
		this.xheight = this.size * this.source.getXHeight() / FontSource.DEFAULT_UNITS_PER_EM;

		double ascent, descent;
		Direction direction = fontStyle.getDirection();
		switch (direction) {
		case LTR:
		case RTL:
			// 横書き
			ascent = this.size * this.source.getAscent() / FontSource.DEFAULT_UNITS_PER_EM;
			descent = this.size * this.source.getDescent() / FontSource.DEFAULT_UNITS_PER_EM;
			break;

		case TB:
			// 縦書き
			ascent = descent = this.size / 2.0;
			break;

		default:
			throw new IllegalStateException();
		}
		double remainder = (this.size - ascent - descent);
		if (remainder != 0) {
			double afrac = ascent / (ascent + descent);
			double dfrac = descent / (ascent + descent);
			ascent = this.size * afrac;
			descent = this.size * dfrac;
		}
		this.ascent = ascent;
		this.descent = descent;
	}

	public Font getFont() {
		try {
			this.font = this.fontStore.useFont(this.source);
		} catch (IOException e) {
			throw new RuntimeException(e);
		}
		return this.font;
	}

	public FontSource getFontSource() {
		return this.source;
	}

	public double getAscent() {
		return this.ascent;
	}

	public double getDescent() {
		return this.descent;
	}

	public double getFontSize() {
		return this.size;
	}

	public double getXHeight() {
		return this.xheight;
	}

	public double getSpaceAdvance() {
		return this.size * this.source.getSpaceAdvance() / FontSource.DEFAULT_UNITS_PER_EM;
	}

	public double getAdvance(int gid) {
		return this.size * this.getFont().getAdvance(gid) / FontSource.DEFAULT_UNITS_PER_EM;
	}

	public double getWidth(int gid) {
		return this.size * this.getFont().getWidth(gid) / FontSource.DEFAULT_UNITS_PER_EM;
	}

	public double getKerning(int gid, int sgid) {
		return this.size * this.getFont().getKerning(gid, sgid) / FontSource.DEFAULT_UNITS_PER_EM;
	}

	public int getLigature(int gid, int cid) {
		return this.getFont().getLigature(gid, cid);
	}

	public boolean canDisplay(int c) {
		return this.getFontSource().canDisplay(c);
	}

	public String toString() {
		return super.toString() + ":[fontSource=" + this.source + "]";
	}
}
