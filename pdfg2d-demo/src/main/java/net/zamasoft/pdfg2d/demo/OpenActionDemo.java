package net.zamasoft.pdfg2d.demo;

import java.awt.geom.Rectangle2D;
import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.OutputStream;

import jp.cssj.rsr.impl.StreamRandomBuilder;
import net.zamasoft.pdfg2d.pdf.PDFPageOutput;
import net.zamasoft.pdfg2d.pdf.PDFWriter;
import net.zamasoft.pdfg2d.pdf.action.JavaScriptAction;
import net.zamasoft.pdfg2d.pdf.gc.PDFGC;
import net.zamasoft.pdfg2d.pdf.impl.PDFWriterImpl;
import net.zamasoft.pdfg2d.pdf.params.PDFParams;

/**
 * Demonstrates how to create an Open Action in a PDF.
 * <p>
 * This demo configures the PDF to execute a specific action (like showing a
 * dialog)
 * when the document is opened by the viewer.
 * </p>
 * 
 * @author MIYABE Tatsuhiko
 * @since 1.0
 */
public class OpenActionDemo {
	public static void main(String[] args) throws Exception {
		PDFParams params = new PDFParams();
		params.setCompression(PDFParams.Compression.NONE);
		params.setVersion(PDFParams.Version.V_1_7);
		JavaScriptAction js = new JavaScriptAction("this.print();");
		params.setOpenAction(js);

		final double width = 300;
		final double height = 300;

		try (OutputStream out = new BufferedOutputStream(
				new FileOutputStream(new File(DemoUtils.getOutputDir(), "open-action.pdf")))) {
			StreamRandomBuilder builder = new StreamRandomBuilder(out);
			final PDFWriter pdf = new PDFWriterImpl(builder, params);

			try (PDFPageOutput page = pdf.nextPage(width, height)) {

				PDFGC gc = new PDFGC(page);
				Rectangle2D rect = new Rectangle2D.Double(10, 10, 280, 10);
				gc.draw(rect);
			}

			pdf.close();
			builder.close();
		}
	}
}
