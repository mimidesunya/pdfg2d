package net.zamasoft.pdfg2d.gc.text;

import net.zamasoft.pdfg2d.gc.font.FontMetrics;
import net.zamasoft.pdfg2d.gc.font.FontStyle;

/**
 * 描画可能なテキストです。
 * 
 * @author MIYABE Tatsuhiko
 * @version $Id: Text.java 1565 2018-07-04 11:51:25Z miyabe $
 */
public interface Text extends Element {
	public FontStyle getFontStyle();

	public FontMetrics getFontMetrics();

	public int getCharOffset();

	public double getAscent();

	public double getDescent();

	public char[] getChars();

	public int getCLen();

	public int[] getGIDs();

	public byte[] getCLens();

	public int getGLen();

	public double getLetterSpacing();

	public void toGlyphs(GlyphHandler gh);

	public double[] getXAdvances(boolean make);
}
