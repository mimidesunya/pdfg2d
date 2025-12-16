package net.zamasoft.pdfg2d.font;

import net.zamasoft.pdfg2d.gc.font.FontStyle;

/**
 * Manages font sources and lookups.
 * 
 * @author MIYABE Tatsuhiko
 * @version $Id: PDFFontSourceManager.java,v 1.1 2007-05-06 15:37:19 miyabe Exp
 *          $
 */
public interface FontSourceManager {
	/**
	 * Returns font sources matching the given font style.
	 * 
	 * @param fontStyle the font style to match, or null to return all fonts
	 * @return an array of matching font sources
	 */
	public FontSource[] lookup(FontStyle fontStyle);
}