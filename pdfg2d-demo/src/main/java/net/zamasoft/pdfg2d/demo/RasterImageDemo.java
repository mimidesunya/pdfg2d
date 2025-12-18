package net.zamasoft.pdfg2d.demo;

import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.OutputStream;

import net.zamasoft.pdfg2d.resolver.protocol.file.FileSource;
import net.zamasoft.pdfg2d.io.impl.OutputFragmentedStream;
import net.zamasoft.pdfg2d.gc.image.Image;
import net.zamasoft.pdfg2d.pdf.PDFGraphicsOutput;
import net.zamasoft.pdfg2d.pdf.PDFWriter;
import net.zamasoft.pdfg2d.pdf.gc.PDFGC;
import net.zamasoft.pdfg2d.pdf.impl.PDFWriterImpl;
import net.zamasoft.pdfg2d.pdf.params.PDFParams;

/**
 * Demonstrates how to load and draw a raster image (JPEG) into a PDF.
 * <p>
 * This demo loads a JPEG file from resources and draws it onto the PDF page.
 * </p>
 * 
 * @author MIYABE Tatsuhiko
 * @since 1.0
 */
public class RasterImageDemo {
	public static void main(String[] args) throws Exception {
		PDFParams params = new PDFParams();

		final double width = 300;
		final double height = 300;

		try (OutputStream out = new BufferedOutputStream(
				new FileOutputStream(new File(DemoUtils.getOutputDir(), "image.pdf")))) {
			OutputFragmentedStream builder = new OutputFragmentedStream(out);
			final PDFWriter pdf = new PDFWriterImpl(builder, params);

			try (PDFGraphicsOutput page = pdf.nextPage(width, height)) {
				PDFGC gc = new PDFGC(page);
				Image image = pdf.loadImage(new FileSource(DemoUtils.getResourceFile("xxx.jpg")));
				gc.drawImage(image);
			}

			pdf.close();
			builder.close();
		}
	}
}


