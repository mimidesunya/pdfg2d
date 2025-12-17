package net.zamasoft.pdfg2d.gc.text;

import java.io.Closeable;

import net.zamasoft.pdfg2d.gc.font.FontMetrics;
import net.zamasoft.pdfg2d.gc.font.FontStyle;

/**
 * Handles text that has been decomposed into glyphs.
 * 
 * @author MIYABE Tatsuhiko
 * @since 1.0
 */
public interface GlyphHandler extends Closeable {
	/**
	 * Starts a text run.
	 * 
	 * @param charOffset  the character offset
	 * @param fontStyle   the font style
	 * @param fontMetrics the font metrics
	 */
	public void startTextRun(int charOffset, FontStyle fontStyle, FontMetrics fontMetrics);

	/**
	 * Ends the current text run.
	 */
	public void endTextRun();

	/**
	 * Sends a glyph.
	 * 
	 * @param charOffset the character offset
	 * @param ch         the characters
	 * @param coff       the character offset in the array
	 * @param clen       the character length
	 * @param gid        the glyph ID
	 */
	public void glyph(int charOffset, char[] ch, int coff, byte clen, int gid);

	/**
	 * Sends a control (spacing/break).
	 * 
	 * @param control the control
	 */
	public void control(TextControl control);

	/**
	 * Flushes the text processed so far. Calling this method explicitly may split
	 * the text
	 * in a way that ignores prohibited line breaks.
	 */
	public void flush();

	/**
	 * Finishes writing the text.
	 */
	@Override
	public void close();
}
