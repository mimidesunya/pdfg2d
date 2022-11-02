package net.zamasoft.pdfg2d;

import java.io.Closeable;
import java.io.File;
import java.io.IOException;

import jp.cssj.rsr.impl.FileRandomBuilder;
import net.zamasoft.pdfg2d.g2d.gc.BridgeGraphics2D;
import net.zamasoft.pdfg2d.pdf.PDFPageOutput;
import net.zamasoft.pdfg2d.pdf.gc.PDFGC;
import net.zamasoft.pdfg2d.pdf.impl.PDFWriterImpl;
import net.zamasoft.pdfg2d.pdf.params.PDFParams;
import net.zamasoft.pdfg2d.pdf.util.PDFUtils;

public class PDFGraphics2D extends BridgeGraphics2D implements Closeable {
	private final boolean fileOut;

	@SuppressWarnings("resource")
	public PDFGraphics2D(File file, double width, double height, PDFParams params) throws IOException {
		super(new PDFGC(new PDFWriterImpl(new FileRandomBuilder(file), params).nextPage(width, height)));
		this.fileOut = true;
	}

	public PDFGraphics2D(File file, double width, double height) throws IOException {
		this(file, width, height, new PDFParams());
	}

	public PDFGraphics2D(File file) throws IOException {
		this(file, PDFUtils.mmToPt(PDFUtils.PAPER_A4_WIDTH_MM), PDFUtils.mmToPt(PDFUtils.PAPER_A4_HEIGHT_MM));
	}

	public PDFGraphics2D(PDFPageOutput pageOut) throws IOException {
		super(new PDFGC(pageOut));
		this.fileOut = false;
	}

	public void close() throws IOException {
		PDFGC gc = (PDFGC) this.gc;
		gc.getPDFGraphicsOutput().close();
		if (!this.fileOut) {
			return;
		}
		gc.getPdfWriter().close();
	}
}
