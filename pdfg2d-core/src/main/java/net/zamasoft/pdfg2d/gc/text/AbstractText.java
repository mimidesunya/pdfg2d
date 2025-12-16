package net.zamasoft.pdfg2d.gc.text;

/**
 * Represents a text that can be drawn.
 * 
 * @author MIYABE Tatsuhiko
 * @since 1.0
 */
public abstract class AbstractText implements Text {
	@Override
	public Type getElementType() {
		return Type.TEXT;
	}

	@Override
	public void toGlyphs(final GlyphHandler gh) {
		final int glen = this.getGLen();
		int charOffset = this.getCharOffset();
		gh.startTextRun(charOffset, this.getFontStyle(), this.getFontMetrics());
		final byte[] clens = this.getCLens();
		final char[] ch = this.getChars();
		final int[] gids = this.getGIDs();
		for (int i = 0, coff = 0; i < glen; ++i) {
			final byte clen = clens[i];
			gh.glyph(charOffset, ch, coff, clen, gids[i]);
			coff += clen;
			charOffset += clen;
		}
		gh.endTextRun();
	}

	/**
	 * Returns the text as a string.
	 */
	@Override
	public String toString() {
		final StringBuilder buff = new StringBuilder();
		buff.append("[Text]");
		buff.append(this.getChars(), 0, this.getCLen());
		buff.append("[");
		final int glen = this.getGLen();
		final int[] gids = this.getGIDs();
		for (int i = 0; i < glen; ++i) {
			final int gid = gids[i];
			if (i > 0) {
				buff.append(',');
			}
			buff.append(Integer.toHexString(gid));
		}
		buff.append("]");
		buff.append("[/Text]");
		return buff.toString();
	}
}
