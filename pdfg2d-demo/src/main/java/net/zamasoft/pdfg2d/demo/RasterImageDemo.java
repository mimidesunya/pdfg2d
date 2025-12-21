package net.zamasoft.pdfg2d.demo;

import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileOutputStream;

import net.zamasoft.pdfg2d.resolver.protocol.file.FileSource;
import net.zamasoft.pdfg2d.pdf.PDFWriter;
import net.zamasoft.pdfg2d.io.impl.StreamFragmentedOutput;
import net.zamasoft.pdfg2d.pdf.gc.PDFGC;
import net.zamasoft.pdfg2d.pdf.impl.PDFWriterImpl;
import net.zamasoft.pdfg2d.pdf.params.PDFParams;

/**
 * Demonstrates embedding a raster image (JPEG) into a PDF.
 * <p>
 * This demo loads an image file and draws it onto a PDF page.
 * </p>
 * 
 * @author MIYABE Tatsuhiko
 * @since 1.0
 */
public class RasterImageDemo {
	public static void main(final String[] args) throws Exception {
		final var params = PDFParams.createDefault();

		final var width = 300.0;
		final var height = 300.0;

		// Create PDF output stream
		try (final var out = new BufferedOutputStream(
				new FileOutputStream(new File(DemoUtils.getOutputDir(), "image.pdf")))) {
			final var builder = new StreamFragmentedOutput(out);
			final PDFWriter pdf = new PDFWriterImpl(builder, params);

			// Create a page and draw the image
			try (final var page = pdf.nextPage(width, height);
					final var gc = new PDFGC(page)) {
				final var image = pdf.loadImage(new FileSource(DemoUtils.getResourceFile("xxx.jpg")));
				gc.drawImage(image);
			}

			pdf.close();
			builder.close();
		}
	}
}
