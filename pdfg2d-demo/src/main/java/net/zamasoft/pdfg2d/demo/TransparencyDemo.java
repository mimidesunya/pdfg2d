package net.zamasoft.pdfg2d.demo;

import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.Shape;
import java.awt.geom.AffineTransform;
import java.awt.geom.Rectangle2D;
import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.OutputStream;

import javax.swing.JFrame;

import net.zamasoft.pdfg2d.io.impl.OutputFragmentedStream;
import net.zamasoft.pdfg2d.g2d.gc.G2DGC;
import net.zamasoft.pdfg2d.gc.GC;
import net.zamasoft.pdfg2d.gc.paint.RGBAColor;
import net.zamasoft.pdfg2d.gc.paint.RGBColor;
import net.zamasoft.pdfg2d.pdf.PDFGraphicsOutput;
import net.zamasoft.pdfg2d.pdf.PDFWriter;
import net.zamasoft.pdfg2d.pdf.gc.PDFGC;
import net.zamasoft.pdfg2d.pdf.impl.PDFWriterImpl;
import net.zamasoft.pdfg2d.pdf.params.PDFParams;

/**
 * Demonstrates alpha transparency and line styles in PDF.
 * <p>
 * This demo draws shapes with different alpha values and stroke patterns
 * to illustrate how transparency and line dashes work in the PDF generation.
 * </p>
 * 
 * @author MIYABE Tatsuhiko
 * @since 1.0
 */
public class TransparencyDemo {
	public static void main(String[] args) throws Exception {
		PDFParams params = new PDFParams();
		params.setCompression(PDFParams.Compression.NONE);

		final double width = 300;
		final double height = 300;

		try (OutputStream out = new BufferedOutputStream(
				new FileOutputStream(new File(DemoUtils.getOutputDir(), "alpha.pdf")))) {
			OutputFragmentedStream builder = new OutputFragmentedStream(out);
			final PDFWriter pdf = new PDFWriterImpl(builder, params);

			try (PDFGraphicsOutput page = pdf.nextPage(width, height)) {
				PDFGC gc = new PDFGC(page);
				draw(gc);
			}

			JFrame frame = new JFrame("Graphics") {
				private static final long serialVersionUID = 1L;

				@Override
				public void paint(Graphics g) {
					super.paint(g);
					Graphics2D g2d = (Graphics2D) g;
					G2DGC gc = new G2DGC(g2d, pdf.getFontManager());
					TransparencyDemo.draw(gc);
				}

			};
			frame.setSize((int) width, (int) height);
			frame.setVisible(true);

			pdf.close();
			builder.close();
		}
	}

	private static void draw(GC gc) {
		gc.transform(AffineTransform.getTranslateInstance(100, 0));
		gc.transform(AffineTransform.getRotateInstance(.1f));
		gc.setLineWidth(10);
		gc.setLinePattern(new double[] { 10, 10 });
		{
			gc.setFillPaint(RGBColor.create(0, 1.0f, 0));
			gc.setStrokePaint(RGBColor.create(0, 0, 1.0f));
			Shape shape = new Rectangle2D.Double(50, 50, 100, 100);
			gc.fillDraw(shape);
		}
		gc.transform(AffineTransform.getRotateInstance(.2f));
		{
			gc.begin();
			gc.setFillPaint(RGBAColor.create(0, 1.0f, 0, .5f));
			gc.setStrokePaint(RGBAColor.create(0, 0, 1.0f, .5f));
			Shape shape = new Rectangle2D.Double(50, 50, 100, 100);
			gc.fillDraw(shape);
			gc.end();
		}
		gc.transform(AffineTransform.getRotateInstance(.2f));
		{
			gc.setLineWidth(20);
			gc.setFillPaint(RGBColor.create(0, 1.0f, 0));
			gc.setStrokePaint(RGBColor.create(0, 0, 1.0f));
			Shape shape = new Rectangle2D.Double(50, 50, 100, 100);
			gc.fillDraw(shape);
		}
		gc.transform(AffineTransform.getRotateInstance(.2f));
		{
			gc.setLineWidth(10);
			gc.setFillPaint(RGBAColor.create(0, 1.0f, 0, .5f));
			gc.setStrokePaint(RGBAColor.create(0, 0, 1.0f, .5f));
			Shape shape = new Rectangle2D.Double(50, 50, 100, 100);
			gc.fillDraw(shape);
		}
		gc.transform(AffineTransform.getRotateInstance(.2f));
		{
			gc.setLinePattern(new double[] { 10, 10 });
			gc.setFillPaint(RGBColor.create(0, 1.0f, 0));
			gc.setStrokePaint(RGBColor.create(0, 0, 1.0f));
			Shape shape = new Rectangle2D.Double(50, 50, 100, 100);
			gc.fillDraw(shape);
		}
	}
}
