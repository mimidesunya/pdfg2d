package net.zamasoft.pdfg2d.pdf.annot;

import java.io.IOException;

import net.zamasoft.pdfg2d.pdf.PdfOutput;
import net.zamasoft.pdfg2d.pdf.PdfPageOutput;

/**
 * Squareアノテーションです。
 * 
 * @author MIYABE Tatsuhiko
 * @version $Id: SquareAnnot.java 1565 2018-07-04 11:51:25Z miyabe $
 */
public class SquareAnnot extends Annot {
	public void writeTo(PdfOutput out, PdfPageOutput pageOut) throws IOException {
		super.writeTo(out, pageOut);

		out.writeName("Subtype");
		out.writeName("Square");
		out.breakBefore();
	}
}
