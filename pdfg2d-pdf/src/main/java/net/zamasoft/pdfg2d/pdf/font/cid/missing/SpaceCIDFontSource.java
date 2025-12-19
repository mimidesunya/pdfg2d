package net.zamasoft.pdfg2d.pdf.font.cid.missing;

import net.zamasoft.pdfg2d.gc.font.FontStyle.Direction;
import net.zamasoft.pdfg2d.pdf.ObjectRef;
import net.zamasoft.pdfg2d.pdf.font.PDFFont;

/**
 * 
 * @author MIYABE Tatsuhiko
 * @version $Id: GenericType0FontFace.java,v 1.2 2005/06/06 04:42:24 harumanx
 *          Exp $
 */
public class SpaceCIDFontSource extends MissingCIDFontSource {
	private static final long serialVersionUID = 1L;

	public static final SpaceCIDFontSource INSTANCES_LTR = new SpaceCIDFontSource(Direction.LTR);
	public static final SpaceCIDFontSource INSTANCES_TB = new SpaceCIDFontSource(Direction.TB);

	SpaceCIDFontSource(Direction direction) {
		super(direction);
	}

	public String getFontName() {
		return "SPACE";
	}

	public boolean canDisplay(int c) {
		switch (c) {
			// Control codes
			case 0x0000:
			case 0x000B:
			case 0x001C:
			case 0x001D:
			case 0x001E:
			case 0x001F:
				// Zero-width spaces
			case 0x200B:
			case 0x200C:
			case 0x200D:
			case 0x200E:
			case 0x200F:
			case 0x202A:
			case 0x202B:
			case 0x202C:
			case 0x202D:
			case 0x202E:
			case 0x2060:
			case 0xFEFF:
				// Space characters
			case 0x007F:
			case 0x0020:
			case 0x00A0:
			case 0x2028:
			case 0x2029:
			case 0x202F:
				return true;
		}
		return false;
	}

	public PDFFont createFont(String name, ObjectRef fontRef) {
		return new SpaceCIDFont(this, name, fontRef);
	}
}
