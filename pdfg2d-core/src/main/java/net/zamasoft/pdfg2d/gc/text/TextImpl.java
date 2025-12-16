package net.zamasoft.pdfg2d.gc.text;

import java.io.Serializable;

import net.zamasoft.pdfg2d.gc.font.FontMetrics;
import net.zamasoft.pdfg2d.gc.font.FontStyle;

/**
 * 描画可能なテキストです。
 * 
 * @author MIYABE Tatsuhiko
 * @since 1.0
 */
public class TextImpl extends AbstractText implements Serializable {
	private static final long serialVersionUID = 0L;

	private static final int INIT_LEN = 50;

	/** The font style of the text. */
	public final FontStyle fontStyle;

	/** The font metrics of the text. */
	public final FontMetrics fontMetrics;

	private int charOffset;

	/** The character buffer. */
	public char[] chars;

	/** The number of characters. */
	public int clen;

	/** The glyph buffer. */
	public int[] gids;

	/** The number of characters corresponding to each glyph. */
	public byte[] clens;

	/** The number of glyphs. */
	public int glen;

	/**
	 * The total width of the text.
	 */
	public double advance = 0;

	/**
	 * The extra spacing added after each glyph.
	 */
	public double letterSpacing = 0;

	/**
	 * The extra spacing added for each glyph.
	 */
	public double[] xadvances = null;

	public TextImpl(final int charOffset, final FontStyle fontStyle, final FontMetrics fontMetrics) {
		assert fontStyle != null : "FontStyle required.";
		assert fontMetrics != null : "FontMetrics required.";
		this.fontStyle = fontStyle;
		this.fontMetrics = fontMetrics;
		this.charOffset = charOffset;
		final int len = Math.max(this.glen, INIT_LEN);
		this.gids = new int[len];
		this.clens = new byte[len];
		this.chars = new char[len];
	}

	@Override
	public int getCharOffset() {
		return this.charOffset;
	}

	@Override
	public double getAdvance() {
		double advance = this.advance + this.letterSpacing * this.glen;
		if (this.xadvances != null) {
			for (int i = 0; i < this.xadvances.length; ++i) {
				advance += this.xadvances[i];
			}
		}
		return advance;
	}

	@Override
	public double[] getXAdvances(final boolean make) {
		if (make) {
			this.xadvances = new double[this.glen];
		}
		return this.xadvances;
	}

	@Override
	public double getAscent() {
		return this.fontMetrics.getAscent();
	}

	@Override
	public double getDescent() {
		return this.fontMetrics.getDescent();
	}

	@Override
	public FontStyle getFontStyle() {
		return this.fontStyle;
	}

	@Override
	public FontMetrics getFontMetrics() {
		return this.fontMetrics;
	}

	@Override
	public char[] getChars() {
		return this.chars;
	}

	@Override
	public int getCLen() {
		return this.clen;
	}

	@Override
	public int[] getGIDs() {
		return this.gids;
	}

	@Override
	public byte[] getCLens() {
		return this.clens;
	}

	@Override
	public int getGLen() {
		return this.glen;
	}

	@Override
	public double getLetterSpacing() {
		return this.letterSpacing;
	}

	public void setLetterSpacing(final double letterSpacing) {
		this.letterSpacing = letterSpacing;
	}

	public Text split(final int goff) {
		assert goff > 0 : "Cannot split at goff <= 0: goff=" + goff;
		assert goff < this.glen : "Cannot split at goff >= glen (" + this.glen + "): goff=" + goff;

		// The previous text is 'text', and 'this' becomes the subsequent text
		final TextImpl text = new TextImpl(this.charOffset, this.fontStyle, this.fontMetrics);
		text.glen = goff;
		text.gids = new int[text.glen];
		text.clens = new byte[text.glen];
		for (int i = 0; i < text.glen; ++i) {
			final int gid = this.gids[i];
			text.gids[i] = gid;
			text.advance += this.fontMetrics.getAdvance(gid);
			if (i > 0) {
				text.advance -= this.fontMetrics.getKerning(text.gids[i - 1], gid);
			}
			final int clen = (text.clens[i] = this.clens[i]);
			text.clen += clen;
		}
		text.letterSpacing = this.letterSpacing;

		text.chars = new char[text.clen];
		System.arraycopy(this.chars, 0, text.chars, 0, text.clen);

		this.glen -= text.glen;
		System.arraycopy(this.gids, text.glen, this.gids, 0, this.glen);
		System.arraycopy(this.clens, text.glen, this.clens, 0, this.glen);

		this.advance -= text.advance;
		// Restore kerning at the split point
		this.advance += this.fontMetrics.getKerning(text.gids[text.glen - 1], this.gids[0]);

		this.clen -= text.clen;
		this.charOffset += text.clen;
		System.arraycopy(this.chars, text.clen, this.chars, 0, this.clen);

		return text;
	}

	public double glyphAdvance(final int gid) {
		double advance = this.fontMetrics.getAdvance(gid);
		if (this.glen > 0) {
			final double kerning = this.fontMetrics.getKerning(this.gids[this.glen - 1], gid);
			advance -= kerning;
		}
		return advance;
	}

	/**
	 * Append the glyph to tail of Text object.
	 * 
	 * @param ch   the characters
	 * @param coff the character offset
	 * @param clen the character length
	 * @param gid  the glyph ID
	 * @return the advance width
	 */
	public double appendGlyph(final char[] ch, final int coff, final byte clen, final int gid) {
		final double advance = this.glyphAdvance(gid);
		final int glen = this.glen + 1;
		if (glen > this.gids.length) {
			final int newLen = glen * 3 / 2;
			{
				final int[] a = this.gids;
				this.gids = new int[newLen];
				System.arraycopy(a, 0, this.gids, 0, a.length);
			}
			{
				final byte[] a = this.clens;
				this.clens = new byte[newLen];
				System.arraycopy(a, 0, this.clens, 0, a.length);
			}
		}

		final int newClen = this.clen + clen;
		if (newClen > this.chars.length) {
			final char[] a = this.chars;
			this.chars = new char[Math.max(INIT_LEN, newClen * 3 / 2)];
			System.arraycopy(a, 0, this.chars, 0, a.length);
		}
		System.arraycopy(ch, coff, this.chars, this.clen, clen);
		this.clen = newClen;

		this.gids[this.glen] = gid;
		this.clens[this.glen] = clen;
		this.glen = glen;
		this.advance += advance;
		return advance;
	}

	public void pack() {
		assert this.glen > 0 : "Empty text";
		if (this.gids.length != this.glen) {
			final int[] gids = new int[this.glen];
			System.arraycopy(this.gids, 0, gids, 0, this.glen);
			this.gids = gids;

			final byte[] clens = new byte[this.glen];
			System.arraycopy(this.clens, 0, clens, 0, this.glen);
			this.clens = clens;
		}
		if (this.clen != this.chars.length) {
			final char[] chars = new char[this.clen];
			System.arraycopy(this.chars, 0, chars, 0, this.clen);
			this.chars = chars;
		}
	}
}
