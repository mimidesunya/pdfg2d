package net.zamasoft.pdfg2d.pdf.annot;

import java.io.IOException;

import net.zamasoft.pdfg2d.pdf.PDFOutput;
import net.zamasoft.pdfg2d.pdf.PDFPageOutput;

/**
 * Squareアノテーションです。
 * 
 * @author MIYABE Tatsuhiko
 * @since 1.0
 */
public class SquareAnnot extends Annot {
	public void writeTo(PDFOutput out, PDFPageOutput pageOut) throws IOException {
		super.writeTo(out, pageOut);

		out.writeName("Subtype");
		out.writeName("Square");
		out.breakBefore();
	}
}
