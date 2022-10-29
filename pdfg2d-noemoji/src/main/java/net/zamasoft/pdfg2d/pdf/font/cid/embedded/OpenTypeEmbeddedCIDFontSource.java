package net.zamasoft.pdfg2d.pdf.font.cid.embedded;

import java.io.File;
import java.io.IOException;

import net.zamasoft.pdfg2d.font.Font;
import net.zamasoft.pdfg2d.font.otf.OpenTypeFontSource;
import net.zamasoft.pdfg2d.pdf.ObjectRef;
import net.zamasoft.pdfg2d.pdf.font.PdfFont;
import net.zamasoft.pdfg2d.pdf.font.cid.CIDFontSource;

/**
 * @author MIYABE Tatsuhiko
 * @version $Id: SystemEmbeddedCIDFontFace.java,v 1.1 2005/06/07 11:45:08
 *          harumanx Exp $
 */
public class OpenTypeEmbeddedCIDFontSource extends OpenTypeFontSource implements CIDFontSource {
	private static final long serialVersionUID = 1L;

	public OpenTypeEmbeddedCIDFontSource(File otfFont, int index, byte direction) throws IOException {
		super(otfFont, index, direction);
	}

	public PdfFont createFont(String name, ObjectRef fontRef) {
		return new OpenTypeEmbeddedCIDFont(this, name, fontRef);
	}

	public Font createFont() {
		return this.createFont(null, null);
	}

	public byte getType() {
		return TYPE_EMBEDDED;
	}
}
