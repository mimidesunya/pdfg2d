package net.zamasoft.pdfg2d.gc.text.layout;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import net.zamasoft.pdfg2d.gc.GC;
import net.zamasoft.pdfg2d.gc.GraphicsException;
import net.zamasoft.pdfg2d.gc.font.FontMetrics;
import net.zamasoft.pdfg2d.gc.font.FontStyle;
import net.zamasoft.pdfg2d.gc.font.FontStyle.Direction;
import net.zamasoft.pdfg2d.gc.text.Element;

import net.zamasoft.pdfg2d.gc.text.GlyphHandler;
import net.zamasoft.pdfg2d.gc.text.TextControl;
import net.zamasoft.pdfg2d.gc.text.Text;
import net.zamasoft.pdfg2d.gc.text.TextImpl;
import net.zamasoft.pdfg2d.gc.text.layout.control.Control;
import net.zamasoft.pdfg2d.gc.text.layout.control.Tab;

public class PageLayoutGlyphHandler implements GlyphHandler {
	/**
	 * Tab width.
	 */
	private static final double TAB_WIDTH = 24.0;

	/** The graphics context to draw to. */
	private GC gc;

	/** Text direction. */
	private Direction direction = Direction.LTR;

	/** Line height. */
	private double lineHeight = 1.2;

	/** Max line advance (width). */
	private double lineAdvance = Double.MAX_VALUE;

	/** Max page advance (height). */
	private double pageAdvance = Double.MAX_VALUE;

	/** Actual max line advance of the drawing area. */
	private double maxLineAdvance = 0;

	/** Actual last line advance of the drawing area. */
	private double lastLineAdvance = 0;

	/** Actual max page advance of the drawing area. */
	private double maxPageAdvance = 0;

	public enum FloatPosition {
		NONE, TOP_RIGHT
	}

	private FloatPosition floatPosition = FloatPosition.NONE;

	public enum Alignment {
		START, END, CENTER, JUSTIFY;
	}

	private Alignment align = Alignment.START;

	private double floatWidth, floatHeight;

	private int columnCount = 1;

	private double columnGap = 0;

	private double pageOffset = 0, lineOffset = 0;

	private int column = 0;

	private record LineBufferItem(Element[] elements, boolean last) {
	}

	private List<LineBufferItem> buffer = new ArrayList<>();

	private Element[] elements;

	private double lineFactor = 0;

	private TextImpl text = null;

	private final List<Element> textBuffer = new ArrayList<>();

	private double letterSpacing = 0;

	private double advance = 0;

	private int textUnitElementCount = 0;

	private int textUnitGlyphCount = 0;

	private boolean justifyPage = false;

	private double fontSize = 0;

	public PageLayoutGlyphHandler(final GC gc) {
		this.gc = gc;
	}

	public GC getGC() {
		return this.gc;
	}

	public void setGC(final GC gc) {
		this.gc = gc;
	}

	public Direction getDirection() {
		return this.direction;
	}

	public void setDirection(final Direction direction) {
		this.direction = direction;
	}

	public Alignment getAlign() {
		return this.align;
	}

	public void setAlign(final Alignment align) {
		this.align = align;
	}

	public double getLineHeight() {
		return this.lineHeight;
	}

	public void setLineHeight(final double lineHeight) {
		this.lineHeight = lineHeight;
	}

	public double getLetterSpacing() {
		return this.letterSpacing;
	}

	public void setLetterSpacing(final double letterSpacing) {
		this.letterSpacing = letterSpacing;
	}

	public double getPageAdvance() {
		return this.pageAdvance;
	}

	public void setPageAdvance(final double pageAdvance) {
		this.pageAdvance = pageAdvance;
	}

	public int getColumnCount() {
		return this.columnCount;
	}

	public void setColumnCount(final int columnCount) {
		this.columnCount = columnCount;
	}

	public double getColumnGap() {
		return this.columnGap;
	}

	public void setColumnGap(final double columnGap) {
		this.columnGap = columnGap;
	}

	public void setLineAdvance(final double lineAdvance) {
		this.lineAdvance = lineAdvance;
	}

	public double getMaxLineAdvance() {
		return this.maxLineAdvance;
	}

	public double getLastLineAdvance() {
		return this.lastLineAdvance;
	}

	public double getMaxPageAdvance() {
		return this.maxPageAdvance;
	}

	private double getMaxAdvance() {
		double maxAdvance = (this.lineAdvance - (this.columnGap * (this.columnCount - 1))) / this.columnCount;
		if (this.floatPosition == FloatPosition.TOP_RIGHT) {
			if (this.pageOffset < this.floatHeight) {
				maxAdvance -= this.floatWidth;
			}
		}
		return maxAdvance;
	}

	public boolean isJustifyPage() {
		return this.justifyPage;
	}

	public void setJustifyPage(final boolean justifyPage) {
		this.justifyPage = justifyPage;
	}

	public double getFontSize() {
		return this.fontSize;
	}

	public void setFontSize(final double fontSize) {
		this.fontSize = fontSize;
	}

	public void setFloat(final FloatPosition position, final double width, final double height) {
		this.floatPosition = position;
		this.floatWidth = width;
		this.floatHeight = height;
	}

	private void endLine(final boolean last) {
		double advance;
		if (last) {
			int elementCount = this.textBuffer.size();
			if (this.text != null) {
				++elementCount;
			}
			this.elements = new Element[elementCount];
			for (int i = 0; i < this.textBuffer.size(); ++i) {
				this.elements[i] = this.textBuffer.get(i);
			}
			if (this.text != null) {
				this.text.pack();
				this.elements[elementCount - 1] = this.text;
				this.text = null;
			}
			advance = this.advance;
			this.textBuffer.clear();
		} else {
			advance = 0;
			int count = this.textBuffer.size() - this.textUnitElementCount;
			int elementCount = count;
			if (this.text != null) {
				if (this.text.getGlyphCount() <= this.textUnitGlyphCount) {
					if (this.textUnitElementCount > 0) {
						++elementCount;
						++count;
					}
				} else {
					++elementCount;
				}
			}
			this.elements = new Element[elementCount];
			final Iterator<Element> i = this.textBuffer.iterator();
			for (int j = 0; j < count; ++j) {
				final Element e = i.next();
				this.elements[j] = e;
				advance += e.getAdvance();
				i.remove();
			}
			if (this.text != null && this.text.getGlyphCount() > this.textUnitGlyphCount) {
				final int pos = this.text.getGlyphCount() - this.textUnitGlyphCount;
				final Element e = this.text.split(pos);
				this.elements[elementCount - 1] = e;
				advance += e.getAdvance();
			}

			// Justify
			if (this.align == Alignment.JUSTIFY) {
				// TODO Hyphenation
				int glyphCount = 0;
				for (final Element e : this.elements) {
					if (e instanceof Text text) {
						glyphCount += text.getGlyphCount();
					}
				}
				if (glyphCount >= 2) {
					final double letterSpacing = (this.getMaxAdvance() - advance) / (double) (glyphCount - 1);
					for (final Element e : this.elements) {
						if (e instanceof TextImpl t) {
							t.setLetterSpacing(t.getLetterSpacing() + letterSpacing);
						}
					}
				}
			}
		}
		this.advance -= advance;
		// assert (advance <= this.getMaxAdvance()) : this.textUnitElementCount
		// + "/" + this.textUnitGlyphCount;

		// Calculate ascent/descent
		double maxAscent = 0;
		double maxDescent = 0;
		for (final Element e : this.elements) {
			if (e instanceof Text text) {
				maxAscent = Math.max(maxAscent, text.getAscent());
				maxDescent = Math.max(maxDescent, text.getDescent());
			} else if (e instanceof Control control) {
				maxAscent = Math.max(maxAscent, control.getAscent());
				maxDescent = Math.max(maxDescent, control.getDescent());
			}
		}
		if (this.fontSize != 0) {
			maxDescent = this.fontSize - maxAscent;
		}

		// Calculate page direction advance
		final double lineMargin = (maxAscent + maxDescent) * (this.lineHeight - 1) / 2.0;
		double pageAdvance1 = maxAscent + lineMargin;
		final double pageAdvance2 = maxDescent + lineMargin + this.lineFactor;

		if (compare(this.pageOffset + pageAdvance1 + pageAdvance2, this.pageAdvance) > 0) {
			// Draw
			this.endColumn();

			// Move column
			++this.column;
			if (this.column >= this.columnCount) {
				this.overflow();
			} else {
				this.pageOffset = 0;
				this.lineOffset += (this.getMaxAdvance() + this.columnGap);
			}
			this.buffer = new ArrayList<>();
			pageAdvance1 = maxAscent;
		}

		// Record
		this.buffer.add(new LineBufferItem(this.elements, last));

		// Line feed
		this.pageOffset += pageAdvance1 + pageAdvance2;
	}

	public static int compare(final double a, final double b) {
		final double diff = a - b;
		if (diff < .1 && diff > -.1) {
			return 0;
		}
		return a < b ? -1 : 1;
	}

	private void drawLine(final Element[] elements, final boolean last) {
		// Calculate ascent/descent
		double maxAscent = 0;
		double maxDescent = 0;
		for (final Element e : elements) {
			if (e instanceof Text text) {
				maxAscent = Math.max(maxAscent, text.getAscent());
				maxDescent = Math.max(maxDescent, text.getDescent());
			} else if (e instanceof Control control) {
				maxAscent = Math.max(maxAscent, control.getAscent());
				maxDescent = Math.max(maxDescent, control.getDescent());
			}
		}
		if (this.fontSize != 0) {
			maxDescent = this.fontSize - maxAscent;
		}

		// Calculate page direction advance
		final double lineMargin = (maxAscent + maxDescent) * (this.lineHeight - 1) / 2.0;
		final double pageAdvance1 = maxAscent + lineMargin;
		final double pageAdvance2 = maxDescent + lineMargin + this.lineFactor;
		this.pageOffset += pageAdvance1;

		// Calculate current position
		// Calculate current position
		double lineAxis = this.lineOffset;
		double pageAxis = switch (this.direction) {
			case LTR, RTL -> this.pageOffset; // TODO RTL
			case TB -> -this.pageOffset;
		};
		if (this.align == Alignment.END || this.align == Alignment.CENTER) {
			double advance = 0;
			for (final Element e : elements) {
				advance += e.getAdvance();
			}
			if (this.align == Alignment.END) {
				lineAxis += this.getMaxAdvance() - advance;
			} else {
				lineAxis += (this.getMaxAdvance() - advance) / 2.0;
			}
		}

		// Draw
		for (final Element e : elements) {
			if (this.gc != null && e instanceof Text text) {
				switch (this.direction) {
					case LTR, RTL -> this.gc.drawText(text, lineAxis, pageAxis);
					case TB -> this.gc.drawText(text, pageAxis, lineAxis);
				}
			}
			lineAxis += e.getAdvance();
		}
		// Line feed
		this.pageOffset += pageAdvance2;
		this.maxLineAdvance = Math.max(this.maxLineAdvance, this.lastLineAdvance = lineAxis);
		this.maxPageAdvance = Math.max(this.maxPageAdvance, this.pageOffset);
	}

	protected void overflow() throws GraphicsException {
		this.pageOffset = this.lineOffset = 0;
		this.column = 0;
	}

	@Override
	public void startTextRun(final int charOffset, final FontStyle fontStyle, final FontMetrics fontMetrics) {
		this.checkText();
		this.text = new TextImpl(charOffset, fontStyle, fontMetrics);
		this.text.setLetterSpacing(this.letterSpacing);
	}

	@Override
	public void glyph(final int charOffset, final char[] ch, final int coff, final byte clen, final int gid) {
		this.advance += this.text.appendGlyph(ch, coff, clen, gid);
		this.advance += this.letterSpacing;
		++this.textUnitGlyphCount;
	}

	@Override
	public void endTextRun() {
		assert this.text.getGlyphCount() > 0;
	}

	private void checkText() {
		if (this.text != null) {
			this.text.pack();
			this.textBuffer.add(this.text);
			++this.textUnitElementCount;
			this.textUnitGlyphCount = 0;
			this.text = null;
		}
	}

	@Override
	public void control(final TextControl control) {
		final Control c = (Control) control;
		switch (c.getControlChar()) {
			case '\n' -> {
				this.endLine(true);
				this.textUnitElementCount = 0;
				this.textUnitGlyphCount = 0;
			}

			case '\t' -> {
				// Tab character
				final Tab tab = (Tab) c;
				tab.advance = (TAB_WIDTH - (this.advance % TAB_WIDTH));
				if (this.advance + tab.advance > this.getMaxAdvance()) {
					this.endLine(false);
					tab.advance = TAB_WIDTH;
				}
			}
		}
		this.checkText();
		this.textBuffer.add(control);
		++this.textUnitElementCount;
		this.advance += control.getAdvance();
	}

	@Override
	public void flush() {
		if (this.advance > this.getMaxAdvance()) {
			this.endLine(false);
		}
		this.textUnitElementCount = 0;
		this.textUnitGlyphCount = 0;
	}

	private void endColumn() {
		if (this.justifyPage && this.columnCount > 1) {
			// Vertical justification
			this.lineFactor = (this.pageAdvance - this.pageOffset) / (this.buffer.size() / 2 - 1);
		}

		final List<LineBufferItem> list = this.buffer;
		this.buffer = null;
		this.pageOffset = 0;
		for (final LineBufferItem item : list) {
			this.drawLine(item.elements(), item.last());
		}
		this.lineFactor = 0;
	}

	@Override
	public void close() {
		this.endLine(true);
		this.endColumn();
	}
}
