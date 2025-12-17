package net.zamasoft.pdfg2d.gc.text;

import java.io.Closeable;

import net.zamasoft.pdfg2d.gc.font.FontStyle;

/**
 * Handles characters to convert string to glyphs.
 * 
 * @author MIYABE Tatsuhiko
 * @since 1.0
 */
public interface CharacterHandler extends Closeable {
	/**
	 * Sets the current font style.
	 * 
	 * @param fontStyle the new font style
	 */
	public void fontStyle(FontStyle fontStyle);

	/**
	 * Sends the characters to be processed.
	 * 
	 * @param charOffset the offset of the characters
	 * @param ch         the array of characters
	 * @param off        the start offset in the array
	 * @param len        the number of characters to process
	 */
	public void characters(int charOffset, char[] ch, int off, int len);

	/**
	 * Inserts a control (spacing/break).
	 * 
	 * @param control the control to insert
	 */
	public void control(TextControl control);

	/**
	 * Flushes the current text segment.
	 * 
	 * <b>This method calls wordBreak().</b>
	 */
	public void flush();

	/**
	 * Ends the paragraph.
	 * 
	 * <b>This method calls wordBreak().</b>
	 */
	@Override
	public void close();
}
