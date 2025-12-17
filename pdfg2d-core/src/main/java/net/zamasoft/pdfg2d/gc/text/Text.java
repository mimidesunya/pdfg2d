package net.zamasoft.pdfg2d.gc.text;

import net.zamasoft.pdfg2d.gc.font.FontMetrics;
import net.zamasoft.pdfg2d.gc.font.FontStyle;

/**
 * Represents text that can be drawn.
 * 
 * @author MIYABE Tatsuhiko
 * @since 1.0
 */
public non-sealed interface Text extends Element {
	public FontStyle getFontStyle();

	public FontMetrics getFontMetrics();

	public int getCharOffset();

	public double getAscent();

	public double getDescent();

	public char[] getChars();

	public int getCharCount();

	public int[] getGlyphIds();

	public byte[] getClusterLengths();

	public int getGlyphCount();

	public double getLetterSpacing();

	public void toGlyphs(GlyphHandler gh);

	public double[] getXAdvances(boolean make);
}
