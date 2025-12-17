package net.zamasoft.pdfg2d.gc.text;

/**
 * Represents a text that can be drawn.
 * 
 * @author MIYABE Tatsuhiko
 * @since 1.0
 */
public abstract class AbstractText implements Text {

	@Override
	public void toGlyphs(final GlyphHandler gh) {
		final int glyphCount = this.getGlyphCount();
		int charOffset = this.getCharOffset();
		gh.startTextRun(charOffset, this.getFontStyle(), this.getFontMetrics());
		final byte[] clusterLengths = this.getClusterLengths();
		final char[] ch = this.getChars();
		final int[] glyphIds = this.getGlyphIds();
		for (int i = 0, coff = 0; i < glyphCount; ++i) {
			final byte clen = clusterLengths[i];
			gh.glyph(charOffset, ch, coff, clen, glyphIds[i]);
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
		buff.append(this.getChars(), 0, this.getCharCount());
		buff.append("[");
		final int glyphCount = this.getGlyphCount();
		final int[] glyphIds = this.getGlyphIds();
		for (int i = 0; i < glyphCount; ++i) {
			final int gid = glyphIds[i];
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
