package net.zamasoft.pdfg2d.pdf.impl;

import java.io.IOException;

import net.zamasoft.pdfg2d.pdf.ObjectRef;

/**
 * Manages the "Names" dictionary in the PDF Catalog.
 * This class handles mapping of top-level name tree categories (e.g., Dests,
 * EmbeddedFiles)
 * to their respective root nodes.
 * 
 * @author MIYABE Tatsuhiko
 * @since 1.0
 */
class NameDictionaryFlow {
	private final XRefImpl xref;

	private final PDFFragmentOutputImpl out, catalogFlow;

	private boolean hasEntry = false;

	public NameDictionaryFlow(final PDFWriterImpl pdfWriter) throws IOException {
		this.xref = pdfWriter.xref;
		final var mainFlow = pdfWriter.mainFlow;
		this.out = mainFlow.forkFragment();
		this.catalogFlow = pdfWriter.catalogFlow;
	}

	public void addEntry(final String key, final ObjectRef ref) throws IOException {
		if (!this.hasEntry) {
			final var nameTreeRef = this.xref.nextObjectRef();
			this.catalogFlow.writeName("Names");
			this.catalogFlow.writeObjectRef(nameTreeRef);
			this.catalogFlow.lineBreak();

			this.out.startObject(nameTreeRef);
			this.out.startHash();
			this.hasEntry = true;
		}

		this.out.writeName(key);
		this.out.writeObjectRef(ref);
		this.out.lineBreak();
	}

	public void close() throws IOException {
		if (this.hasEntry) {
			this.out.endHash();
			this.out.endObject();
		}
		this.out.close();
	}
}
