package net.zamasoft.pdfg2d.pdf.impl;

import java.io.IOException;

import net.zamasoft.pdfg2d.pdf.ObjectRef;

/**
 * @author MIYABE Tatsuhiko
 * @since 1.0
 */
class PagesFlow {
	private final PDFWriterImpl pdfWriter;

	/** ルートページリファレンス。 */
	private final ObjectRef rootPageRef;

	/** 子ページ参照フロー。 */
	private final PDFFragmentOutputImpl pagesKidsFlow;

	/** ページ数フロー。 */
	private final PDFFragmentOutputImpl pageCountFlow;

	/** ページ数カウンタ */
	private int pageCount = 0;

	public PagesFlow(PDFWriterImpl pdfWriter, ObjectRef rootPageRef) throws IOException {
		this.pdfWriter = pdfWriter;
		this.rootPageRef = rootPageRef;

		PDFFragmentOutputImpl mainFlow = pdfWriter.mainFlow;
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

	public PDFPageOutputImpl createPage(double width, double height) throws IOException {
		// ページオブジェクト
		++this.pageCount;

		return new PDFPageOutputImpl(this.pdfWriter, this.rootPageRef, this.pagesKidsFlow, width, height);
	}

	public void close() throws IOException {
		this.pageCountFlow.writeInt(this.pageCount);
		this.pageCountFlow.close();
		this.pagesKidsFlow.close();
	}
}
