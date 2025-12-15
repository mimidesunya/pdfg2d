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

import jp.cssj.rsr.impl.StreamRandomBuilder;
import net.zamasoft.pdfg2d.g2d.gc.G2DGC;
import net.zamasoft.pdfg2d.gc.GC;
import net.zamasoft.pdfg2d.gc.image.GroupImageGC;
import net.zamasoft.pdfg2d.gc.image.Image;
import net.zamasoft.pdfg2d.gc.paint.RGBColor;
import net.zamasoft.pdfg2d.pdf.PDFGraphicsOutput;
import net.zamasoft.pdfg2d.pdf.PDFWriter;
import net.zamasoft.pdfg2d.pdf.gc.PDFGC;
import net.zamasoft.pdfg2d.pdf.impl.PDFWriterImpl;
import net.zamasoft.pdfg2d.pdf.params.PDFParams;

/**
 * Demonstrates the use of Transparency Groups.
 * <p>
 * This demo creates a group image context, draws shapes into it with
 * transparency,
 * and then renders the group into the PDF page.
 * </p>
 * 
 * @author MIYABE Tatsuhiko
 * @since 1.0
 */
public class TransparencyGroupDemo {
	public static void main(String[] args) throws Exception {
		PDFParams params = new PDFParams();
		params.setCompression(PDFParams.Compression.NONE);

		final double width = 300;
		final double height = 300;

		try (OutputStream out = new BufferedOutputStream(
				new FileOutputStream(new File(DemoUtils.getOutputDir(), "group-image.pdf")))) {
			StreamRandomBuilder builder = new StreamRandomBuilder(out);
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
					TransparencyGroupDemo.draw(gc);
				}

			};
			frame.setSize((int) width, (int) height);
			frame.setVisible(true);

			pdf.close();
			builder.close();
		}
	}

	private static void draw(GC gc) {
		gc.transform(AffineTransform.getRotateInstance(.1f));
		gc.setLineWidth(10);
		{
			gc.setFillPaint(RGBColor.create(0, 1.0f, 0));
			gc.setStrokePaint(RGBColor.create(0, 1.0f, 1.0f));
			Shape shape = new Rectangle2D.Double(50, 50, 100, 100);
			gc.fillDraw(shape);
		}

		Image gi1;
		{
			GroupImageGC gc2 = gc.createGroupImage(300f, 300f);
			{
				gc2.setFillPaint(RGBColor.create(0, 0, 1.0f));
				gc2.setStrokePaint(RGBColor.create(1.0f, 0, 0));
				Shape shape = new Rectangle2D.Double(100, 100, 100, 100);
				gc2.fillDraw(shape);
			}
			{
				gc2.setFillPaint(RGBColor.create(1.0f, 0, 0));
				gc2.setStrokePaint(RGBColor.create(0, 0, 1.0f));
				Shape shape = new Rectangle2D.Double(130, 130, 100, 100);
				gc2.fillDraw(shape);
			}

			{
				GroupImageGC gc3 = gc2.createGroupImage(300f, 300f);
				{
					gc3.setFillPaint(RGBColor.create(1.0f, 0, 1.0f));
					gc3.setStrokePaint(RGBColor.create(1.0f, 1.0f, 0));
					Shape shape = new Rectangle2D.Double(70, 70, 100, 100);
					gc3.fillDraw(shape);
				}
				gi1 = gc3.finish();
				gc2.setFillAlpha(.5f);
				gc2.drawImage(gi1);
			}
			Image gi = gc2.finish();
			gc.setFillAlpha(.5f);
			gc.drawImage(gi);

			gc.transform(AffineTransform.getTranslateInstance(100, 0));
			gc.setFillAlpha(1);
			gc.drawImage(gi1);
		}
	}
}
