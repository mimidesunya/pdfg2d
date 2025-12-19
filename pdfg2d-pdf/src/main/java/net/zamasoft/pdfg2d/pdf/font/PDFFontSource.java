package net.zamasoft.pdfg2d.pdf.font;

import net.zamasoft.pdfg2d.font.FontSource;
import net.zamasoft.pdfg2d.pdf.ObjectRef;

public interface PDFFontSource extends FontSource {
	public static enum Type {
		/**
		 * Unknown/missing font.
		 */
		MISSING,

		/**
		 * Core PDF font.
		 */
		CORE,

		/**
		 * Embedded font.
		 */
		EMBEDDED,

		/**
		 * CID-Identity external font.
		 */
		CID_IDENTITY,

		/**
		 * CID-Keyed font.
		 */
		CID_KEYED;
	}

	/**
	 * Returns the font type.
	 * 
	 * @return the font type
	 */
	public Type getType();

	public PDFFont createFont(String name, ObjectRef fontRef);
}
