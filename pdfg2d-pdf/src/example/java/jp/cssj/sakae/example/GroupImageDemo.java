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
import jp.cssj.sakae.gc.image.GroupImageGC;
import jp.cssj.sakae.gc.image.Image;
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
public class GroupImageDemo {
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
					GroupImageDemo.draw(gc);
				}

			};
			frame.setSize((int) width, (int) height);
			frame.setVisible(true);

			pdf.finish();
			builder.finish();
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
