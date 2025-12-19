package net.zamasoft.pdfg2d.pdf.font.cid;

import net.zamasoft.pdfg2d.gc.font.Panose;
import net.zamasoft.pdfg2d.pdf.font.PDFFontSource;

/**
 * @author MIYABE Tatsuhiko
 * @since 1.0
 */
public interface CIDFontSource extends PDFFontSource {
	/**
	 * Returns the PANOSE-1 classification code.
	 * 
	 * @return the PANOSE-1 code
	 */
	public Panose getPanose();
}
