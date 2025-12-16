package net.zamasoft.pdfg2d.gc.font;

import java.io.Serializable;

import net.zamasoft.pdfg2d.font.FontSource;

/**
 * Represents the metrics information of an allocated font.
 * 
 * @author MIYABE Tatsuhiko
 * @since 1.0
 */
public interface FontMetrics extends Serializable {
	/**
	 * Returns the font size.
	 * 
	 * @return the font size
	 */
	public double getFontSize();

	/**
	 * Returns the height of the font.
	 * 
	 * @return the font height
	 */
	public double getXHeight();

	/**
	 * Returns the ascent of the font (height above baseline).
	 * 
	 * @return the ascent
	 */
	public double getAscent();

	/**
	 * Returns the descent of the font (height below baseline).
	 * 
	 * @return the descent
	 */
	public double getDescent();

	/**
	 * Returns the advance width of the specified glyph.
	 * 
	 * @param gid the glyph ID
	 * @return the advance width
	 */
	public double getAdvance(int gid);

	/**
	 * Returns the width of the specified glyph.
	 * 
	 * @param gid the glyph ID
	 * @return the width
	 */
	public double getWidth(int gid);

	/**
	 * Returns the advance width of a space character.
	 * 
	 * @return the space advance width
	 */
	public double getSpaceAdvance();

	/**
	 * Returns the kerning between two glyphs.
	 * 
	 * @param gid  the first glyph ID
	 * @param sgid the second glyph ID
	 * @return the kerning value
	 */
	public double getKerning(int gid, int sgid);

	/**
	 * Returns the FontSource.
	 * 
	 * @return the FontSource
	 */
	public FontSource getFontSource();
}