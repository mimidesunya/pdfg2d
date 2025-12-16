package net.zamasoft.pdfg2d.font;

import java.io.IOException;

/**
 * A store for PDF fonts.
 * <p>
 * This interface is not thread-safe.
 * </p>
 * 
 * @author MIYABE Tatsuhiko
 * @since 1.0
 */
public interface FontStore {
	/**
	 * Retrieves or creates a font from the given source.
	 * 
	 * @param metaFont the font source
	 * @return the font
	 * @throws IOException if an error occurs while creating the font
	 */
	public Font useFont(FontSource metaFont) throws IOException;
}
