package net.zamasoft.pdfg2d.pdf.impl;

import java.io.IOException;

import net.zamasoft.pdfg2d.pdf.ObjectRef;

/**
 * @author MIYABE Tatsuhiko
 * @version $Id: PagesFlow.java 1565 2018-07-04 11:51:25Z miyabe $
 */
class PagesFlow {
	private final PdfWriterImpl pdfWriter;

	/** ルートページリファレンス。 */
	private final ObjectRef rootPageRef;

	/** 子ページ参照フロー。 */
	private final PdfFragmentOutputImpl pagesKidsFlow;

	/** ページ数フロー。 */
	private final PdfFragmentOutputImpl pageCountFlow;

	/** ページ数カウンタ */
	private int pageCount = 0;

	public PagesFlow(PdfWriterImpl pdfWriter, ObjectRef rootPageRef) throws IOException {
		this.pdfWriter = pdfWriter;
		this.rootPageRef = rootPageRef;

		PdfFragmentOutputImpl mainFlow = pdfWriter.mainFlow;
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

	public PdfPageOutputImpl createPage(double width, double height) throws IOException {
		// ページオブジェクト
		++this.pageCount;

		return new PdfPageOutputImpl(this.pdfWriter, this.rootPageRef, this.pagesKidsFlow, width, height);
	}

	public void close() throws IOException {
		this.pageCountFlow.writeInt(this.pageCount);
		this.pageCountFlow.close();
		this.pagesKidsFlow.close();
	}
}
