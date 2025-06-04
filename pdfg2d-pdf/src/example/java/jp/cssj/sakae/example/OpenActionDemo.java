package jp.cssj.sakae.example;

import java.awt.geom.Rectangle2D;
import java.io.BufferedOutputStream;
import java.io.FileOutputStream;
import java.io.OutputStream;

import jp.cssj.rsr.impl.StreamRandomBuilder;
import jp.cssj.sakae.pdf.PdfPageOutput;
import jp.cssj.sakae.pdf.PdfWriter;
import jp.cssj.sakae.pdf.action.JavaScriptAction;
import jp.cssj.sakae.pdf.gc.PdfGC;
import jp.cssj.sakae.pdf.impl.PdfWriterImpl;
import jp.cssj.sakae.pdf.params.PdfParams;

/**
 * @author MIYABE Tatsuhiko
 * @since 1.0
 */
public class OpenActionDemo {
	public static void main(String[] args) throws Exception {
		PdfParams params = new PdfParams();
		params.setCompression(PdfParams.COMPRESSION_NONE);

		JavaScriptAction js = new JavaScriptAction("this.print();");
		params.setOpenAction(js);

		final double width = 300;
		final double height = 300;

		try (OutputStream out = new BufferedOutputStream(new FileOutputStream("loocal/test.pdf"))) {
			StreamRandomBuilder builder = new StreamRandomBuilder(out);
			final PdfWriter pdf = new PdfWriterImpl(builder, params);

			try (PdfPageOutput page = pdf.nextPage(width, height)) {
				PdfGC gc = new PdfGC(page);
				Rectangle2D rect = new Rectangle2D.Double(10, 10, 280, 10);
				gc.draw(rect);
			}

			pdf.finish();
			builder.finish();
		}
	}
}
