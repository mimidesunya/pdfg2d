package net.zamasoft.pdfg2d.gc.text.hyphenation.impl;

import net.zamasoft.pdfg2d.gc.font.FontMetrics;
import net.zamasoft.pdfg2d.gc.font.FontStyle;
import net.zamasoft.pdfg2d.gc.text.FilterGlyphHandler;
import net.zamasoft.pdfg2d.gc.text.GlyphHandler;
import net.zamasoft.pdfg2d.gc.text.Quad;
import net.zamasoft.pdfg2d.gc.text.hyphenation.Hyphenation;

/**
 * Decomposes text into unbreakable units for line breaking.
 *
 * @author MIYABE Tatsuhiko
 * @since 1.0
 */
public class TextUnitizer implements FilterGlyphHandler {
	protected Hyphenation hyph;

	private GlyphHandler glyphHandler;

	/**
	 * Previous character.
	 */
	private char prevChar = 0;

	private Quad beforeQuad = null;

	public TextUnitizer(final Hyphenation hyph) {
		this.hyph = hyph;
	}

	public Hyphenation getHyphenation() {
		return this.hyph;
	}

	public void setHyphenation(final Hyphenation hyph) {
		this.hyph = hyph;
	}

	@Override
	public void setGlyphHandler(final GlyphHandler glyphHandler) {
		this.glyphHandler = glyphHandler;
	}

	@Override
	public void startTextRun(final int charOffset, final FontStyle fontStyle, final FontMetrics fontMetrics) {
		this.glyphHandler.startTextRun(charOffset, fontStyle, fontMetrics);
	}

	@Override
	public void endTextRun() {
		this.glyphHandler.endTextRun();
	}

	@Override
	public void glyph(final int charOffset, final char[] ch, final int coff, final byte clen, final int gid) {
		final char c1 = ch[coff];
		final char c2 = ch[coff + clen - 1];
		this.nextGlyph(c1, c2, clen);
		if (this.beforeQuad != null) {
			this.glyphHandler.quad(this.beforeQuad);
			this.beforeQuad = null;
		}
		this.glyphHandler.glyph(charOffset, ch, coff, clen, gid);
	}

	@Override
	public void quad(final Quad quad) {
		final String str = quad.getString();
		// System.err.println("TU QUAD: " + quad + "/" +
		// Integer.toHexString(this.prevChar));
		if (str == Quad.BREAK) {
			// Separates strings except for CONTINUE_BEFORE, CONTINUE_AFTER
			if (this.prevChar != 0 && this.prevChar != '\u2060') {
				this.internalFlush();
			}
			this.prevChar = '\u200B';
		} else if (str == Quad.CONTINUE_BEFORE) {
			// Treat as previous character (attach to following string <span>...)
			if (this.prevChar == '\u200B') {
				this.internalFlush();
				this.prevChar = '\u2060';
			} else {
				if (this.beforeQuad != null) {
					this.glyphHandler.quad(this.beforeQuad);
				}
				this.beforeQuad = quad;
				return;
			}
		} else if (str == Quad.CONTINUE_AFTER) {
			// Treat as previous character (attach to previous string ...</span>)
			if (this.prevChar == '\u200B') {
				this.internalFlush();
				this.prevChar = '\u2060';
			}
		} else if (str.length() == 0) {
			// Treat as previous character (attach to previous/following)
			if (this.prevChar == '\u200B') {
				this.internalFlush();
			}
			this.prevChar = '\u2060';
		} else {
			// Use corresponding string
			final int strlen = str.length();
			final char c1 = str.charAt(0);
			final char c2 = str.charAt(strlen - 1);
			this.nextGlyph(c1, c2, strlen);
		}
		if (this.beforeQuad != null) {
			this.glyphHandler.quad(this.beforeQuad);
			this.beforeQuad = null;
		}
		this.glyphHandler.quad(quad);
	}

	private void internalFlush() {
		this.glyphHandler.flush();
	}

	@Override
	public void flush() {
		if (this.beforeQuad != null) {
			this.glyphHandler.quad(this.beforeQuad);
			this.beforeQuad = null;
		}
		this.internalFlush();
	}

	@Override
	public void close() {
		if (this.beforeQuad != null) {
			this.glyphHandler.quad(this.beforeQuad);
			this.beforeQuad = null;
		}
		this.glyphHandler.close();
	}

	/**
	 * 
	 * @param c1        the first character corresponding to the glyph
	 * @param c2        the last character corresponding to the glyph
	 * @param charCount the number of characters
	 */
	private void nextGlyph(final char c1, final char c2, final int charCount) {
		if (this.prevChar != 0 && this.prevChar != '\u2060'
				&& (this.prevChar == '\u200B' || !this.hyph.atomic(this.prevChar, c1))) {
			this.internalFlush();
		}
		this.prevChar = c2;
	}
}
