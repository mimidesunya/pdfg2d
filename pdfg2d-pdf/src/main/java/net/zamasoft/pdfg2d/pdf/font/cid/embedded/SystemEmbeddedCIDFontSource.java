package net.zamasoft.pdfg2d.pdf.font.cid.embedded;

import java.awt.Font;

import net.zamasoft.pdfg2d.pdf.ObjectRef;
import net.zamasoft.pdfg2d.pdf.font.PDFFont;
import net.zamasoft.pdfg2d.pdf.font.cid.SystemCIDFontSource;

/**
 * @author MIYABE Tatsuhiko
 * @version $Id: SystemEmbeddedCIDFontFace.java,v 1.1 2005/06/07 11:45:08
 *          harumanx Exp $
 */
public class SystemEmbeddedCIDFontSource extends SystemCIDFontSource {
	private static final long serialVersionUID = 1L;

	public SystemEmbeddedCIDFontSource(Font font) {
		super(font);
	}

	public PDFFont createFont(String name, ObjectRef fontRef) {
		return new SystemEmbeddedCIDFont(this, name, fontRef);
	}

	public net.zamasoft.pdfg2d.font.Font createFont() {
		return this.createFont(null, null);
	}

	public Type getType() {
		return Type.EMBEDDED;
	}
}
