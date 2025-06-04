package net.zamasoft.pdfg2d.gc.text;

/**
 * 描画可能なテキストです。
 * 
 * @author MIYABE Tatsuhiko
 * @since 1.0
 */
public abstract class AbstractText implements Text {
	public Type getElementType() {
		return Type.TEXT;
	}

	public void toGlyphs(GlyphHandler gh) {
		int glen = this.getGLen();
		int charOffset = this.getCharOffset();
		gh.startTextRun(charOffset, this.getFontStyle(), this.getFontMetrics());
		byte[] clens = this.getCLens();
		char[] ch = this.getChars();
		int[] gids = this.getGIDs();
		for (int i = 0, coff = 0; i < glen; ++i) {
			byte clen = clens[i];
			gh.glyph(charOffset, ch, coff, clen, gids[i]);
			coff += clen;
			charOffset += clen;
		}
		gh.endTextRun();
	}

	/**
	 * テキストを文字列として返します。
	 */
	public String toString() {
		StringBuffer buff = new StringBuffer();
		buff.append("[Text]");
		buff.append(this.getChars(), 0, this.getCLen());
		buff.append("[");
		int glen = this.getGLen();
		int[] gids = this.getGIDs();
		for (int i = 0; i < glen; ++i) {
			int gid = gids[i];
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
