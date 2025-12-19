package net.zamasoft.pdfg2d.pdf.impl;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import net.zamasoft.pdfg2d.font.Font;
import net.zamasoft.pdfg2d.font.FontSource;
import net.zamasoft.pdfg2d.pdf.ObjectRef;
import net.zamasoft.pdfg2d.pdf.font.PDFFont;
import net.zamasoft.pdfg2d.pdf.font.PDFFontSource;

/**
 * Manages font resources.
 * 
 * @author MIYABE Tatsuhiko
 * @since 1.0
 */
class FontFlow {
	private final XRefImpl xref;

	private final Map<String, ObjectRef> nameToResourceRef;

	private final PDFFragmentOutputImpl objectsFlow;

	/** Mapping from FontSource to PDFFont. */
	private final Map<FontSource, Font> fonts = new HashMap<>();
	private final List<PDFFont> fontList = new ArrayList<>();

	public FontFlow(final Map<String, ObjectRef> nameToResourceRef, final PDFFragmentOutputImpl objectsFlow,
			final XRefImpl xref) throws IOException {
		this.xref = xref;
		this.nameToResourceRef = nameToResourceRef;
		this.objectsFlow = objectsFlow;
	}

	public Font useFont(final FontSource source) throws IOException {
		Font font = this.fonts.get(source);
		if (font != null) {
			return font;
		}

		if (source instanceof PDFFontSource) {
			final String name = "F" + this.fonts.size();
			final ObjectRef fontRef = this.xref.nextObjectRef();
			this.nameToResourceRef.put(name, fontRef);

			font = ((PDFFontSource) source).createFont(name, fontRef);
			this.fontList.add((PDFFont) font);
		} else {
			font = source.createFont();
		}
		this.fonts.put(source, font);

		return font;
	}

	public void close() throws IOException {
		for (final PDFFont font : this.fontList) {
			font.writeTo(this.objectsFlow, this.xref);
		}
	}
}
