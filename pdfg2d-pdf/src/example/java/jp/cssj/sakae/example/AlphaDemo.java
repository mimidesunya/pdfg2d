package jp.cssj.sakae.example;

import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.Shape;
import java.awt.geom.AffineTransform;
import java.awt.geom.Rectangle2D;
import java.io.BufferedOutputStream;
import java.io.FileOutputStream;
import java.io.OutputStream;

import javax.swing.JFrame;

import jp.cssj.rsr.impl.StreamRandomBuilder;
import jp.cssj.sakae.g2d.gc.G2dGC;
import jp.cssj.sakae.gc.GC;
import jp.cssj.sakae.gc.paint.RGBAColor;
import jp.cssj.sakae.gc.paint.RGBColor;
import jp.cssj.sakae.pdf.PdfGraphicsOutput;
import jp.cssj.sakae.pdf.PdfWriter;
import jp.cssj.sakae.pdf.gc.PdfGC;
import jp.cssj.sakae.pdf.impl.PdfWriterImpl;
import jp.cssj.sakae.pdf.params.PdfParams;

/**
 * @author MIYABE Tatsuhiko
 * @since 1.0
 */
public class AlphaDemo {
	public static void main(String[] args) throws Exception {
		PdfParams params = new PdfParams();
		params.setCompression(PdfParams.COMPRESSION_NONE);

		final double width = 300;
		final double height = 300;

		try (OutputStream out = new BufferedOutputStream(new FileOutputStream("local/test.pdf"))) {
			StreamRandomBuilder builder = new StreamRandomBuilder(out);
			final PdfWriter pdf = new PdfWriterImpl(builder, params);

			try (PdfGraphicsOutput page = pdf.nextPage(width, height)) {
				PdfGC gc = new PdfGC(page);
				draw(gc);
			}

			JFrame frame = new JFrame("Graphics") {
				private static final long serialVersionUID = 1L;

				@Override
				public void paint(Graphics g) {
					super.paint(g);
					Graphics2D g2d = (Graphics2D) g;
					G2dGC gc = new G2dGC(g2d, pdf.getFontManager());
					AlphaDemo.draw(gc);
				}

			};
			frame.setSize((int) width, (int) height);
			frame.setVisible(true);

			pdf.finish();
			builder.finish();
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
