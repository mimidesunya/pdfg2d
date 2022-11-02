package jp.cssj.sakae.example;

import java.awt.Shape;
import java.awt.geom.AffineTransform;
import java.awt.geom.Rectangle2D;
import java.io.BufferedOutputStream;
import java.io.FileOutputStream;
import java.io.OutputStream;
import java.net.URI;

import jp.cssj.rsr.impl.StreamRandomBuilder;
import jp.cssj.sakae.pdf.PdfPageOutput;
import jp.cssj.sakae.pdf.PdfWriter;
import jp.cssj.sakae.pdf.annot.LinkAnnot;
import jp.cssj.sakae.pdf.gc.PdfGC;
import jp.cssj.sakae.pdf.impl.PdfWriterImpl;
import jp.cssj.sakae.pdf.params.PdfParams;

/**
 * @author MIYABE Tatsuhiko
 * @since 1.0
 */
public class AnnotationDemo {
	public static void main(String[] args) throws Exception {
		PdfParams params = new PdfParams();
		params.setCompression(PdfParams.COMPRESSION_NONE);
		params.setVersion(PdfParams.VERSION_1_7);

		final double width = 300;
		final double height = 300;

		try (OutputStream out = new BufferedOutputStream(new FileOutputStream("local/test.pdf"))) {
			StreamRandomBuilder builder = new StreamRandomBuilder(out);
			final PdfWriter pdf = new PdfWriterImpl(builder, params);

			try (PdfPageOutput page = pdf.nextPage(width, height)) {

				PdfGC gc = new PdfGC(page);

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

			pdf.finish();
			builder.finish();
		}
	}
}
