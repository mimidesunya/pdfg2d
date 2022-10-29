package net.zamasoft.pdfg2d.gc.text.layout;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import net.zamasoft.pdfg2d.gc.GC;
import net.zamasoft.pdfg2d.gc.GraphicsException;
import net.zamasoft.pdfg2d.gc.font.FontMetrics;
import net.zamasoft.pdfg2d.gc.font.FontStyle;
import net.zamasoft.pdfg2d.gc.text.Element;
import net.zamasoft.pdfg2d.gc.text.GlyphHandler;
import net.zamasoft.pdfg2d.gc.text.Quad;
import net.zamasoft.pdfg2d.gc.text.Text;
import net.zamasoft.pdfg2d.gc.text.TextImpl;
import net.zamasoft.pdfg2d.gc.text.layout.control.Control;
import net.zamasoft.pdfg2d.gc.text.layout.control.Tab;

public class PageLayoutGlyphHandler implements GlyphHandler {
	/**
	 * タブの幅です。
	 */
	private static final double TAB_WIDTH = 24.0;

	/** 描画先です。 */
	private GC gc;

	/** 文字の方向です。 */
	private byte direction = FontStyle.DIRECTION_LTR;

	/** 行幅です。 */
	private double lineHeight = 1.2;

	/** 最大行幅です。 */
	private double lineAdvance = Double.MAX_VALUE;

	/** 最大ページ方向幅です。 */
	private double pageAdvance = Double.MAX_VALUE;

	/** 実際の描画領域の最大行幅です。 */
	private double maxLineAdvance = 0;

	/** 実際の描画領域の最後の行幅です。 */
	private double lastLineAdvance = 0;

	/** 実際の描画領域の最大ページ方向幅です。 */
	private double maxPageAdvance = 0;

	public static byte FLOAT_NONE = 0;
	public static byte FLOAT_TOP_RIGHT = 1;

	private byte floatPosition = FLOAT_NONE;

	public static byte ALIGN_START = 0;
	public static byte ALIGN_END = 1;
	public static byte ALIGN_CENTER = 2;
	public static byte ALIGN_JUSTIFY = 3;

	private byte align = ALIGN_START;

	private double floatWidth, floatHeight;

	private int columnCount = 1;

	private double columnGap = 0;

	private double pageOffset = 0, lineOffset = 0;

	private int column = 0;

	private List<Object> buffer = new ArrayList<Object>();

	private Element[] elements;

	private double lineFactor = 0;

	private TextImpl text = null;

	private List<Element> textBuffer = new ArrayList<Element>();

	private double letterSpacing = 0;

	private double advance = 0;

	private int textUnitElementCount = 0;

	private int textUnitGlyphCount = 0;

	private boolean justifyPage = false;

	private double fontSize = 0;

	public GC getGC() {
		return this.gc;
	}

	public void setGC(GC gc) {
		this.gc = gc;
	}

	public byte getDirection() {
		return this.direction;
	}

	public void setDirection(byte direction) {
		this.direction = direction;
	}

	public byte getAlign() {
		return this.align;
	}

	public void setAlign(byte align) {
		this.align = align;
	}

	public double getLineHeight() {
		return this.lineHeight;
	}

	public void setLineHeight(double lineHeight) {
		this.lineHeight = lineHeight;
	}

	public double getLetterSpacing() {
		return this.letterSpacing;
	}

	public void setLetterSpacing(double letterSpacing) {
		this.letterSpacing = letterSpacing;
	}

	public double getPageAdvance() {
		return this.pageAdvance;
	}

	public void setPageAdvance(double pageAdvance) {
		this.pageAdvance = pageAdvance;
	}

	public int getColumnCount() {
		return this.columnCount;
	}

	public void setColumnCount(int columnCount) {
		this.columnCount = columnCount;
	}

	public double getColumnGap() {
		return this.columnGap;
	}

	public void setColumnGap(double columnGap) {
		this.columnGap = columnGap;
	}

	public void setLineAdvance(double lineAdvance) {
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
		if (this.floatPosition == FLOAT_TOP_RIGHT) {
			if (this.pageOffset < this.floatHeight) {
				maxAdvance -= this.floatWidth;
			}
		}
		return maxAdvance;
	}

	public boolean isJustifyPage() {
		return this.justifyPage;
	}

	public void setJustifyPage(boolean justifyPage) {
		this.justifyPage = justifyPage;
	}

	public double getFontSize() {
		return this.fontSize;
	}

	public void setFontSize(double fontSize) {
		this.fontSize = fontSize;
	}

	public void setFloat(byte position, double width, double height) {
		this.floatPosition = position;
		this.floatWidth = width;
		this.floatHeight = height;
	}

	private void endLine(boolean last) {
		double advance;
		if (last) {
			int elementCount = this.textBuffer.size();
			if (this.text != null) {
				++elementCount;
			}
			this.elements = new Element[elementCount];
			for (int i = 0; i < this.textBuffer.size(); ++i) {
				this.elements[i] = (Element) this.textBuffer.get(i);
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
				if (this.text.getGLen() <= this.textUnitGlyphCount) {
					if (this.textUnitElementCount > 0) {
						++elementCount;
						++count;
					}
				} else {
					++elementCount;
				}
			}
			this.elements = new Element[elementCount];
			Iterator<Element> i = this.textBuffer.iterator();
			for (int j = 0; j < count; ++j) {
				Element e = (Element) i.next();
				this.elements[j] = e;
				advance += e.getAdvance();
				i.remove();
			}
			if (this.text != null && this.text.getGLen() > this.textUnitGlyphCount) {
				int pos = this.text.getGLen() - this.textUnitGlyphCount;
				Element e = this.text.split(pos);
				this.elements[elementCount - 1] = e;
				advance += e.getAdvance();
			}

			// 両あわせ
			if (this.align == ALIGN_JUSTIFY) {
				// TODO ハイフネーション
				int glyphCount = 0;
				for (int j = 0; j < this.elements.length; ++j) {
					Element e = this.elements[j];
					if (e.getElementType() == Element.TEXT) {
						glyphCount += ((Text) e).getGLen();
					}
				}
				if (glyphCount >= 2) {
					double letterSpacing = (this.getMaxAdvance() - advance) / (double) (glyphCount - 1);
					for (int j = 0; j < this.elements.length; ++j) {
						Element e = this.elements[j];
						if (e.getElementType() == Element.TEXT) {
							TextImpl t = (TextImpl) e;
							t.setLetterSpacing(t.getLetterSpacing() + letterSpacing);
						}
					}
				}
			}
		}
		this.advance -= advance;
		// assert (advance <= this.getMaxAdvance()) : this.textUnitElementCount
		// + "/" + this.textUnitGlyphCount;

		// アセント・ディセントを算出
		double maxAscent = 0, maxDescent = 0;
		for (int i = 0; i < this.elements.length; ++i) {
			Element e = this.elements[i];
			if (e.getElementType() == Element.TEXT) {
				Text text = (Text) e;
				maxAscent = Math.max(maxAscent, text.getAscent());
				maxDescent = Math.max(maxDescent, text.getDescent());
			} else {
				Control control = (Control) e;
				maxAscent = Math.max(maxAscent, control.getAscent());
				maxDescent = Math.max(maxDescent, control.getDescent());
			}
		}
		if (this.fontSize != 0) {
			maxDescent = this.fontSize - maxAscent;
		}

		// ページ方向の進行幅を求める
		double lineMargin = (maxAscent + maxDescent) * (this.lineHeight - 1) / 2.0;
		double pageAdvance1 = maxAscent + lineMargin;
		double pageAdvance2 = maxDescent + lineMargin + this.lineFactor;

		if (compare(this.pageOffset + pageAdvance1 + pageAdvance2, this.pageAdvance) > 0) {
			// 描画
			this.endColumn();

			// カラム移動
			++this.column;
			if (this.column >= this.columnCount) {
				this.overflow();
			} else {
				this.pageOffset = 0;
				this.lineOffset += (this.getMaxAdvance() + this.columnGap);
			}
			this.buffer = new ArrayList<Object>();
			pageAdvance1 = maxAscent;
		}

		// 記録
		this.buffer.add(this.elements);
		this.buffer.add(Boolean.valueOf(last));

		// 行送り
		this.pageOffset += pageAdvance1 + pageAdvance2;
	}

	public static int compare(double a, double b) {
		double diff = a - b;
		if (diff < .1 && diff > -.1) {
			return 0;
		}
		return a < b ? -1 : 1;
	}

	private void drawLine(Element[] elements, boolean last) {
		// アセント・ディセントを算出
		double maxAscent = 0, maxDescent = 0;
		for (int i = 0; i < elements.length; ++i) {
			Element e = elements[i];
			if (e.getElementType() == Element.TEXT) {
				Text text = (Text) e;
				maxAscent = Math.max(maxAscent, text.getAscent());
				maxDescent = Math.max(maxDescent, text.getDescent());
			} else {
				Control control = (Control) e;
				maxAscent = Math.max(maxAscent, control.getAscent());
				maxDescent = Math.max(maxDescent, control.getDescent());
			}
		}
		if (this.fontSize != 0) {
			maxDescent = this.fontSize - maxAscent;
		}

		// ページ方向の進行幅を求める
		double lineMargin = (maxAscent + maxDescent) * (this.lineHeight - 1) / 2.0;
		double pageAdvance1 = maxAscent + lineMargin;
		double pageAdvance2 = maxDescent + lineMargin + this.lineFactor;
		this.pageOffset += pageAdvance1;

		// 現在位置を算出
		double lineAxis, pageAxis;
		switch (this.direction) {
		case FontStyle.DIRECTION_LTR:
		case FontStyle.DIRECTION_RTL:// TODO RTL
			// 横書き
			lineAxis = this.lineOffset;
			pageAxis = this.pageOffset;
			break;
		case FontStyle.DIRECTION_TB:
			// 縦書き
			lineAxis = this.lineOffset;
			pageAxis = -this.pageOffset;
			break;
		default:
			throw new IllegalStateException();
		}
		if (this.align == ALIGN_END || this.align == ALIGN_CENTER) {
			double advance = 0;
			for (int i = 0; i < elements.length; ++i) {
				Element e = elements[i];
				advance += e.getAdvance();
			}
			if (this.align == ALIGN_END) {
				lineAxis += this.getMaxAdvance() - advance;
			} else {
				lineAxis += (this.getMaxAdvance() - advance) / 2.0;
			}
		}

		// 描画
		for (int i = 0; i < elements.length; ++i) {
			Element e = elements[i];
			if (this.gc != null && e.getElementType() == Element.TEXT) {
				Text text = (Text) e;
				switch (this.direction) {
				case FontStyle.DIRECTION_LTR:
				case FontStyle.DIRECTION_RTL:
					// 横書き
					this.gc.drawText(text, lineAxis, pageAxis);
					break;
				case FontStyle.DIRECTION_TB:
					// 縦書き
					this.gc.drawText(text, pageAxis, lineAxis);
					break;
				default:
					throw new IllegalArgumentException();
				}
			}
			lineAxis += e.getAdvance();
		}
		// 行送り
		this.pageOffset += pageAdvance2;
		this.maxLineAdvance = Math.max(this.maxLineAdvance, this.lastLineAdvance = lineAxis);
		this.maxPageAdvance = Math.max(this.maxPageAdvance, this.pageOffset);
	}

	protected void overflow() throws GraphicsException {
		this.pageOffset = this.lineOffset = 0;
		this.column = 0;
	}

	public void startTextRun(int charOffset, FontStyle fontStyle, FontMetrics fontMetrics) {
		this.checkText();
		this.text = new TextImpl(charOffset, fontStyle, fontMetrics);
		this.text.setLetterSpacing(this.letterSpacing);
	}

	public void glyph(int charOffset, char[] ch, int coff, byte clen, int gid) {
		this.advance += this.text.appendGlyph(ch, coff, clen, gid);
		this.advance += this.letterSpacing;
		++this.textUnitGlyphCount;
	}

	public void endTextRun() {
		assert this.text.getGLen() > 0;
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

	public void quad(Quad quad) {
		Control control = (Control) quad;
		switch (control.getControlChar()) {
		case '\n':
			this.endLine(true);
			this.textUnitElementCount = 0;
			this.textUnitGlyphCount = 0;
			break;

		case '\t':
			// タブ文字
			Tab tab = (Tab) control;
			tab.advance = (TAB_WIDTH - (this.advance % TAB_WIDTH));
			if (this.advance + tab.advance > this.getMaxAdvance()) {
				this.endLine(false);
				tab.advance = TAB_WIDTH;
			}
			break;
		}
		this.checkText();
		this.textBuffer.add(quad);
		++this.textUnitElementCount;
		this.advance += quad.getAdvance();
	}

	public void flush() {
		if (this.advance > this.getMaxAdvance()) {
			this.endLine(false);
		}
		this.textUnitElementCount = 0;
		this.textUnitGlyphCount = 0;
	}

	private void endColumn() {
		if (this.justifyPage && this.columnCount > 1) {
			// ページ方向両あわせ
			this.lineFactor = (this.pageAdvance - this.pageOffset) / (this.buffer.size() / 2 - 1);
		}

		List<Object> list = this.buffer;
		this.buffer = null;
		this.pageOffset = 0;
		for (int i = 0; i < list.size(); ++i) {
			Element[] elements = (Element[]) list.get(i);
			Boolean last = (Boolean) list.get(++i);
			this.drawLine(elements, last.booleanValue());
		}
		this.lineFactor = 0;
	}

	public void finish() {
		this.endLine(true);
		this.endColumn();
	}
}
