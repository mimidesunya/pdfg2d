package net.zamasoft.pdfg2d.gc.text;

/**
 * Processes a string to convert it to glyphs.
 * 
 * @author MIYABE Tatsuhiko
 * @since 1.0
 */
public interface Glypher extends CharacterHandler {
	public void setGlyphHander(GlyphHandler glyphHandler);
}
