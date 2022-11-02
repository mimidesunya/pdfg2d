package net.zamasoft.pdfg2d.pdf.impl;

import java.io.IOException;

import net.zamasoft.pdfg2d.pdf.ObjectRef;

/**
 * @author MIYABE Tatsuhiko
 * @since 1.0
 */
class NameDictionaryFlow {
	private final XRefImpl xref;

	private final PDFFragmentOutputImpl out, catalogFlow;

	private boolean hasEntry = false;

	public NameDictionaryFlow(PDFWriterImpl pdfWriter) throws IOException {
		this.xref = pdfWriter.xref;

		PDFFragmentOutputImpl mainFlow = pdfWriter.mainFlow;
		this.out = mainFlow.forkFragment();
		this.catalogFlow = pdfWriter.catalogFlow;
	}

	public void addEntry(String key, ObjectRef ref) throws IOException {
		if (!this.hasEntry) {
			ObjectRef nameTreeRef = this.xref.nextObjectRef();
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
