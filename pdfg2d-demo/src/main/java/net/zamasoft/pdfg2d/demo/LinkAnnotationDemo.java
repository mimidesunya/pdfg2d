package net.zamasoft.pdfg2d.demo;

import java.awt.Shape;
import java.awt.geom.AffineTransform;
import java.awt.geom.Rectangle2D;
import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.OutputStream;
import java.net.URI;

import jp.cssj.rsr.impl.StreamRandomBuilder;
import net.zamasoft.pdfg2d.pdf.PDFPageOutput;
import net.zamasoft.pdfg2d.pdf.PDFWriter;
import net.zamasoft.pdfg2d.pdf.annot.LinkAnnot;
import net.zamasoft.pdfg2d.pdf.gc.PDFGC;
import net.zamasoft.pdfg2d.pdf.impl.PDFWriterImpl;
import net.zamasoft.pdfg2d.pdf.params.PDFParams;

/**
 * Demonstrates how to create link annotations in a PDF.
 * <p>
 * This demo adds a clickable URI link to the PDF page.
 * </p>
 * 
 * @author MIYABE Tatsuhiko
 * @since 1.0
 */
public class LinkAnnotationDemo {
	public static void main(String[] args) throws Exception {
		PDFParams params = new PDFParams();
		params.setCompression(PDFParams.Compression.NONE);
		params.setVersion(PDFParams.Version.V_1_7);

		final double width = 300;
		final double height = 300;

		try (OutputStream out = new BufferedOutputStream(
				new FileOutputStream(new File(DemoUtils.getOutputDir(), "annotation.pdf")))) {
			StreamRandomBuilder builder = new StreamRandomBuilder(out);
			final PDFWriter pdf = new PDFWriterImpl(builder, params);

			try (PDFPageOutput page = pdf.nextPage(width, height)) {

				PDFGC gc = new PDFGC(page);

				{
					Shape s = new Rectangle2D.Double(10, 10, 280, 30);
					s = AffineTransform.getRotateInstance(.2).createTransformedShape(s);

					gc.draw(s);

					LinkAnnot link = new LinkAnnot();
					link.setShape(s);
					link.setURI(URI.create("http://www.yahoo.co.jp/"));
					page.addAnnotation(link);
				}
			}

			pdf.close();
			builder.close();
		}
	}
}
