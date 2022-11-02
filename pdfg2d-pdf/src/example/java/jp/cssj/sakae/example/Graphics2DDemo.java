package jp.cssj.sakae.example;

import java.awt.AlphaComposite;
import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.Font;
import java.awt.FontMetrics;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.font.TextAttribute;
import java.awt.geom.GeneralPath;
import java.io.BufferedOutputStream;
import java.io.FileOutputStream;
import java.io.OutputStream;
import java.text.AttributedString;

import javax.swing.JFrame;

import jp.cssj.rsr.impl.StreamRandomBuilder;
import jp.cssj.sakae.g2d.gc.BridgeGraphics2D;
import jp.cssj.sakae.gc.text.util.TextUtils;
import jp.cssj.sakae.pdf.PdfGraphicsOutput;
import jp.cssj.sakae.pdf.PdfWriter;
import jp.cssj.sakae.pdf.gc.PdfGC;
import jp.cssj.sakae.pdf.impl.PdfWriterImpl;

/**
 * @author MIYABE Tatsuhiko
 * @since 1.0
 */
public class Graphics2DDemo {
	public static void main(String[] args) throws Exception {
		final double width = 400;
		final double height = 400;
		final int x = 100, y = 100;

		try (OutputStream out = new BufferedOutputStream(new FileOutputStream("target/test.pdf"))) {
			StreamRandomBuilder builder = new StreamRandomBuilder(out);
			final PdfWriter pdf = new PdfWriterImpl(builder);

			{
				// 1ページ目
				try (PdfGraphicsOutput page = pdf.nextPage(width, height)) {
					Graphics2D g = new BridgeGraphics2D(new PdfGC(page));
					draw1(g);
				}

				JFrame frame = new JFrame("Graphics") {
					private static final long serialVersionUID = 1L;

					@Override
					public void paint(Graphics g) {
						super.paint(g);
						draw1((Graphics2D) g);
					}

				};
				frame.setSize((int) width, (int) height);
				frame.setLocation(x, y);
				frame.setVisible(true);
			}

			{
				// 2ページ目
				try (PdfGraphicsOutput page = pdf.nextPage(width, height)) {
					Graphics2D g = new BridgeGraphics2D(new PdfGC(page));
					draw2(g);
				}

				JFrame frame = new JFrame("Graphics") {
					private static final long serialVersionUID = 1L;

					@Override
					public void paint(Graphics g) {
						super.paint(g);
						draw2((Graphics2D) g);
					}

				};
				frame.setSize((int) width, (int) height);
				frame.setLocation(x, y);
				frame.setVisible(true);
			}

			pdf.finish();
			builder.finish();
		}
	}

	private static void draw1(Graphics2D g) {
		// クリップ
		GeneralPath path = new GeneralPath();
		path.moveTo(150, 0);
		path.lineTo(20, 225);
		path.lineTo(0, 225);
		path.lineTo(0, 0);
		path.closePath();
		path.moveTo(150, 0);
		path.lineTo(280, 225);
		path.lineTo(300, 225);
		path.lineTo(300, 0);
		path.closePath();
		path.moveTo(0, 225);
		path.lineTo(300, 225);
		path.lineTo(300, 300);
		path.lineTo(0, 300);
		path.closePath();
		g.clip(path);

		// 図形描画
		g.setColor(Color.BLUE);
		g.setStroke(new BasicStroke(10, BasicStroke.CAP_BUTT, BasicStroke.JOIN_BEVEL, 1, new float[] { 10, 10 }, 0));
		g.drawOval(10, 10, 280, 280);
		g.setColor(Color.RED);
		AlphaComposite comp = AlphaComposite.getInstance(AlphaComposite.SRC_OVER, 0.5f);
		g.setComposite(comp);
		g.drawRect(20, 20, 260, 260);
	}

	private static void draw2(Graphics2D g) {
		g.setFont(new Font("SansSerif", Font.PLAIN, 12));

		FontMetrics fm = g.getFontMetrics();
		System.out.println(fm.stringWidth("abc"));

		// 横書き
		AttributedString text = new AttributedString("盗人を捕らえてみれば我が子なり\n斬りたくもあり斬りたくもなし");
		text.addAttribute(TextAttribute.WEIGHT, TextAttribute.WEIGHT_BOLD, 0, 2);
		text.addAttribute(TextAttribute.POSTURE, TextAttribute.POSTURE_OBLIQUE, 3, 7);
		text.addAttribute(TextAttribute.WEIGHT, TextAttribute.WEIGHT_BOLD, 16, 23);
		g.drawString(text.getIterator(), 0, 12);

		// 縦書き
		text.addAttribute(TextUtils.WRITING_MODE, TextUtils.WRITING_MODE_VERTICAL, 0, 30);
		g.drawString(text.getIterator(), 300 - 6, 0);
	}
}
