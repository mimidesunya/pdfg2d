package net.zamasoft.pdfg2d.pdf.impl;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import net.zamasoft.pdfg2d.font.Font;
import net.zamasoft.pdfg2d.font.FontSource;
import net.zamasoft.pdfg2d.pdf.ObjectRef;
import net.zamasoft.pdfg2d.pdf.font.PdfFont;
import net.zamasoft.pdfg2d.pdf.font.PdfFontSource;

/**
 * @author MIYABE Tatsuhiko
 * @version $Id: FontFlow.java 1565 2018-07-04 11:51:25Z miyabe $
 */
class FontFlow {
	private final XRefImpl xref;

	private final Map<String, ObjectRef> nameToResourceRef;

	private final PdfFragmentOutputImpl objectsFlow;

	/** FontKeyからPDFFontへのマッピング。 */
	private final Map<FontSource, Font> fonts = new HashMap<FontSource, Font>();
	private final List<PdfFont> fontList = new ArrayList<PdfFont>();

	public FontFlow(Map<String, ObjectRef> nameToResourceRef, PdfFragmentOutputImpl objectsFlow, XRefImpl xref)
			throws IOException {
		this.xref = xref;
		this.nameToResourceRef = nameToResourceRef;
		this.objectsFlow = objectsFlow;
	}

	public Font useFont(FontSource source) throws IOException {
		Font font = this.fonts.get(source);
		if (font != null) {
			return font;
		}

		if (source instanceof PdfFontSource) {
			String name = "F" + this.fonts.size();
			ObjectRef fontRef = this.xref.nextObjectRef();
			this.nameToResourceRef.put(name, fontRef);

			font = ((PdfFontSource)source).createFont(name, fontRef);
			this.fontList.add((PdfFont)font);
		}
		else {
			font = source.createFont();
		}
		this.fonts.put(source, font);

		return font;
	}

	public void close() throws IOException {
		for (int i = 0; i < this.fontList.size(); ++i) {
			PdfFont font = (PdfFont) this.fontList.get(i);
			font.writeTo(this.objectsFlow, this.xref);
		}
	}
}
