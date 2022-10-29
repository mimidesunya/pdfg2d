package jp.cssj.sakae.example;

import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.OutputStream;

import jp.cssj.resolver.file.FileSource;
import jp.cssj.rsr.impl.StreamRandomBuilder;
import jp.cssj.sakae.gc.image.Image;
import jp.cssj.sakae.pdf.PdfGraphicsOutput;
import jp.cssj.sakae.pdf.PdfWriter;
import jp.cssj.sakae.pdf.gc.PdfGC;
import jp.cssj.sakae.pdf.impl.PdfWriterImpl;
import jp.cssj.sakae.pdf.params.PdfParams;

/**
 * @author MIYABE Tatsuhiko
 * @version $Id: ImageDemo.java 1565 2018-07-04 11:51:25Z miyabe $
 */
public class ImageDemo {
	public static void main(String[] args) throws Exception {
		PdfParams params = new PdfParams();

		final double width = 300;
		final double height = 300;

		try (OutputStream out = new BufferedOutputStream(new FileOutputStream("local/test.pdf"))) {
			StreamRandomBuilder builder = new StreamRandomBuilder(out);
			final PdfWriter pdf = new PdfWriterImpl(builder, params);

			try (PdfGraphicsOutput page = pdf.nextPage(width, height)) {
				Image image = pdf.loadImage(new FileSource(new File("src/example/xxx.jpg")));

				PdfGC gc = new PdfGC(page);
				gc.drawImage(image);
			}

			pdf.finish();
			builder.finish();
		}
	}
}
