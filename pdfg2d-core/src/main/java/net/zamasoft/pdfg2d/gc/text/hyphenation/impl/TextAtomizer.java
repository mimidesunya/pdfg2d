package net.zamasoft.pdfg2d.gc.text.hyphenation.impl;

import net.zamasoft.pdfg2d.gc.font.FontMetrics;
import net.zamasoft.pdfg2d.gc.font.FontStyle;
import net.zamasoft.pdfg2d.gc.text.FilterGlyphHandler;
import net.zamasoft.pdfg2d.gc.text.GlyphHandler;
import net.zamasoft.pdfg2d.gc.text.TextControl;
import net.zamasoft.pdfg2d.gc.text.hyphenation.TextBreakingRules;

/**
 * Decomposes text into unbreakable units for line breaking.
 *
 * @author MIYABE Tatsuhiko
 * @since 1.0
 */
public class TextAtomizer implements FilterGlyphHandler {
	protected TextBreakingRules rules;

	private GlyphHandler glyphHandler;

	/**
	 * Previous character.
	 */
	private char prevChar = 0;

	private TextControl beforeControl = null;

	public TextAtomizer(final TextBreakingRules rules) {
		this.rules = rules;
	}

	public TextBreakingRules getTextBreakingRules() {
		return this.rules;
	}

	public void setTextBreakingRules(final TextBreakingRules rules) {
		this.rules = rules;
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
		if (this.beforeControl != null) {
			this.glyphHandler.control(this.beforeControl);
			this.beforeControl = null;
		}
		this.glyphHandler.glyph(charOffset, ch, coff, clen, gid);
	}

	@Override
	public void control(final TextControl control) {
		final String str = control.getString();
		// System.err.println("TU CONTROL: " + control + "/" +
		// Integer.toHexString(this.prevChar));
		if (str == TextControl.BREAK) {
			// Separates strings except for CONTINUE_BEFORE, CONTINUE_AFTER
			if (this.prevChar != 0 && this.prevChar != '\u2060') {
				this.internalFlush();
			}
			this.prevChar = '\u200B';
		} else if (str == TextControl.CONTINUE_BEFORE) {
			// Treat as previous character (attach to following string <span>...)
			if (this.prevChar == '\u200B') {
				this.internalFlush();
				this.prevChar = '\u2060';
			} else {
				if (this.beforeControl != null) {
					this.glyphHandler.control(this.beforeControl);
				}
				this.beforeControl = control;
				return;
			}
		} else if (str == TextControl.CONTINUE_AFTER) {
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
		if (this.beforeControl != null) {
			this.glyphHandler.control(this.beforeControl);
			this.beforeControl = null;
		}
		this.glyphHandler.control(control);
	}

	private void internalFlush() {
		this.glyphHandler.flush();
	}

	@Override
	public void flush() {
		if (this.beforeControl != null) {
			this.glyphHandler.control(this.beforeControl);
			this.beforeControl = null;
		}
		this.internalFlush();
	}

	@Override
	public void close() {
		if (this.beforeControl != null) {
			this.glyphHandler.control(this.beforeControl);
			this.beforeControl = null;
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
				&& (this.prevChar == '\u200B' || !this.rules.atomic(this.prevChar, c1))) {
			this.internalFlush();
		}
		this.prevChar = c2;
	}
}
