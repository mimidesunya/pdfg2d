package net.zamasoft.pdfg2d.pdf.font.cid;

import net.zamasoft.pdfg2d.gc.font.Panose;
import net.zamasoft.pdfg2d.pdf.font.PDFFontSource;

/**
 * @author MIYABE Tatsuhiko
 * @since 1.0
 */
public interface CIDFontSource extends PDFFontSource {
	/**
	 * PANOSE-1コードを返します。
	 * 
	 * @return PANOSE-1コード。
	 */
	public Panose getPanose();
}
