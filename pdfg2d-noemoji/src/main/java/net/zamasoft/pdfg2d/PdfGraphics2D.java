package net.zamasoft.pdfg2d;

import java.io.Closeable;
import java.io.File;
import java.io.IOException;

import jp.cssj.rsr.impl.FileRandomBuilder;
import net.zamasoft.pdfg2d.g2d.gc.BridgeGraphics2D;
import net.zamasoft.pdfg2d.pdf.PdfPageOutput;
import net.zamasoft.pdfg2d.pdf.gc.PdfGC;
import net.zamasoft.pdfg2d.pdf.impl.PdfWriterImpl;
import net.zamasoft.pdfg2d.pdf.params.PdfParams;
import net.zamasoft.pdfg2d.pdf.util.PdfUtils;

public class PdfGraphics2D extends BridgeGraphics2D implements Closeable {
	private final boolean fileOut;

	public PdfGraphics2D(File file, double width, double height, PdfParams params) throws IOException {
		super(new PdfGC(new PdfWriterImpl(new FileRandomBuilder(file), params).nextPage(width, height)));
		this.fileOut = true;
	}

	public PdfGraphics2D(File file, double width, double height) throws IOException {
		this(file, width, height, new PdfParams());
	}

	public PdfGraphics2D(File file) throws IOException {
		this(file, PdfUtils.mmToPt(PdfUtils.PAPER_A4_WIDTH_MM), PdfUtils.mmToPt(PdfUtils.PAPER_A4_HEIGHT_MM));
	}

	public PdfGraphics2D(PdfPageOutput pageOut) throws IOException {
		super(new PdfGC(pageOut));
		this.fileOut = false;
	}

	public void close() throws IOException {
		PdfGC gc = (PdfGC) this.gc;
		gc.getPDFGraphicsOutput().close();
		if (!this.fileOut) {
			return;
		}
		PdfWriterImpl pdfWriter = (PdfWriterImpl) gc.getPdfWriter();
		pdfWriter.finish();
		pdfWriter.getBuilder().finish();
	}
}
