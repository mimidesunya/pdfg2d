package net.zamasoft.pdfg2d.pdf.impl;

import java.io.IOException;

import net.zamasoft.pdfg2d.pdf.ObjectRef;

/**
 * Manages PDF page structure (Pages tree).
 * 
 * @author MIYABE Tatsuhiko
 * @since 1.0
 */
class PagesFlow {
	private final PDFWriterImpl pdfWriter;

	/** Root page reference. */
	private final ObjectRef rootPageRef;

	/** Child page reference flow. */
	private final PDFFragmentOutputImpl pagesKidsFlow;

	/** Page count flow. */
	private final PDFFragmentOutputImpl pageCountFlow;

	/** Page count counter. */
	private int pageCount = 0;

	public PagesFlow(final PDFWriterImpl pdfWriter, final ObjectRef rootPageRef) throws IOException {
		this.pdfWriter = pdfWriter;
		this.rootPageRef = rootPageRef;

		final PDFFragmentOutputImpl mainFlow = pdfWriter.mainFlow;
		mainFlow.startObject(rootPageRef);

		mainFlow.startHash();

		mainFlow.writeName("Type");
		mainFlow.writeName("Pages");
		mainFlow.lineBreak();

		mainFlow.writeName("Kids");
		mainFlow.startArray();
		this.pagesKidsFlow = mainFlow.forkFragment();
		mainFlow.endArray();
		mainFlow.lineBreak();

		mainFlow.writeName("Count");
		mainFlow.write(' ');
		this.pageCountFlow = mainFlow.forkFragment();
		mainFlow.lineBreak();

		mainFlow.endHash();
		mainFlow.endObject();
	}

	public PDFPageOutputImpl createPage(final double width, final double height) throws IOException {
		// Page Object
		++this.pageCount;

		return new PDFPageOutputImpl(this.pdfWriter, this.rootPageRef, this.pagesKidsFlow, width, height);
	}

	public void close() throws IOException {
		this.pageCountFlow.writeInt(this.pageCount);
		this.pageCountFlow.close();
		this.pagesKidsFlow.close();
	}
}
