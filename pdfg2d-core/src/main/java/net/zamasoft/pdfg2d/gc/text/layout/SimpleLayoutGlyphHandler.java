package net.zamasoft.pdfg2d.gc.text.layout;

import net.zamasoft.pdfg2d.gc.GC;
import net.zamasoft.pdfg2d.gc.font.FontMetrics;
import net.zamasoft.pdfg2d.gc.font.FontStyle;
import net.zamasoft.pdfg2d.gc.text.GlyphHandler;
import net.zamasoft.pdfg2d.gc.text.Quad;
import net.zamasoft.pdfg2d.gc.text.TextImpl;
import net.zamasoft.pdfg2d.gc.text.layout.control.Control;
import net.zamasoft.pdfg2d.gc.text.layout.control.Tab;

public class SimpleLayoutGlyphHandler implements GlyphHandler {
	/**
	 * Tab width.
	 */
	private static final double TAB_WIDTH = 24.0;

	/** The graphics context to draw to. */
	private GC gc;

	private TextImpl text = null;

	private double letterSpacing = 0;

	private double advance = 0, line = 0, maxLineHeight = 0;

	public GC getGC() {
		return this.gc;
	}

	public void setGC(final GC gc) {
		this.gc = gc;
	}

	public double getAdvance() {
		return this.advance;
	}

	public double getLetterSpacing() {
		return this.letterSpacing;
	}

	public void setLetterSpacing(final double letterSpacing) {
		this.letterSpacing = letterSpacing;
	}

	@Override
	public void startTextRun(final int charOffset, final FontStyle fontStyle, final FontMetrics fontMetrics) {
		this.text = new TextImpl(charOffset, fontStyle, fontMetrics);
		this.text.setLetterSpacing(this.letterSpacing);
	}

	@Override
	public void glyph(final int charOffset, final char[] ch, final int coff, final byte clen, final int gid) {
		this.text.appendGlyph(ch, coff, clen, gid);
	}

	@Override
	public void endTextRun() {
		assert this.text.getGLen() > 0;
		if (this.gc != null) {
			switch (this.text.getFontStyle().getDirection()) {
				case LTR:
				case RTL:
					// Horizontal writing
					this.gc.drawText(this.text, this.advance, this.line);
					break;
				case TB:
					// Vertical writing
					this.gc.drawText(this.text, -this.line, this.advance);
					break;
				default:
					throw new IllegalArgumentException();
			}
		}
		this.advance += this.text.getAdvance();
		this.maxLineHeight = Math.max(this.maxLineHeight, this.text.getFontStyle().getSize());
	}

	@Override
	public void quad(final Quad quad) {
		final Control control = (Control) quad;
		this.maxLineHeight = Math.max(this.maxLineHeight, control.getAscent() + control.getDescent());
		switch (control.getControlChar()) {
			case '\n':
				this.line += this.maxLineHeight;
				this.maxLineHeight = 0;
				this.advance = 0;
				return;

			case '\t':
				// Tab character
				final Tab tab = (Tab) control;
				tab.advance = (TAB_WIDTH - (this.advance % TAB_WIDTH));
				break;
		}
		this.advance += quad.getAdvance();
	}

	@Override
	public void flush() {
		// ignore
	}

	@Override
	public void close() {
		// ignore
	}
}
