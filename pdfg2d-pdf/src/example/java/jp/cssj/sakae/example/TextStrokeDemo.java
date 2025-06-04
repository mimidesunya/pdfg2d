package jp.cssj.sakae.example;

import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.RenderingHints;
import java.awt.geom.AffineTransform;
import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.OutputStream;

import javax.swing.JFrame;

import jp.cssj.resolver.file.FileSource;
import jp.cssj.rsr.impl.StreamRandomBuilder;
import jp.cssj.sakae.g2d.gc.G2dGC;
import jp.cssj.sakae.gc.GC;
import jp.cssj.sakae.gc.font.FontFace;
import jp.cssj.sakae.gc.font.FontFamilyList;
import jp.cssj.sakae.gc.font.FontStyle;
import jp.cssj.sakae.gc.paint.RGBColor;
import jp.cssj.sakae.gc.text.TextLayoutHandler;
import jp.cssj.sakae.gc.text.hyphenation.HyphenationBundle;
import jp.cssj.sakae.gc.text.layout.SimpleLayoutGlyphHandler;
import jp.cssj.sakae.pdf.PdfGraphicsOutput;
import jp.cssj.sakae.pdf.PdfWriter;
import jp.cssj.sakae.pdf.font.PdfFontSourceManager;
import jp.cssj.sakae.pdf.gc.PdfGC;
import jp.cssj.sakae.pdf.impl.PdfWriterImpl;
import jp.cssj.sakae.pdf.params.PdfParams;

/**
 * @author MIYABE Tatsuhiko
 * @since 1.0
 */
public class TextStrokeDemo {
	public static void main(String[] args) throws Exception {
		PdfParams params = new PdfParams();
		params.setCompression(PdfParams.COMPRESSION_NONE);

		PdfFontSourceManager fsm = new PdfFontSourceManager();

		{
			FontFace face = new FontFace();
			face.src = new FileSource(new File("src/example/ipag.otf"));
			face.fontFamily = FontFamilyList.create("IPAゴシック");
			fsm.addFontFace(face);
		}

		params.setFontSourceManager(fsm);

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
					g2d.translate(0, 24);
					g2d.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
					G2dGC gc = new G2dGC(g2d, pdf.getFontManager());
					TextStrokeDemo.draw(gc);
				}
			};
			frame.setSize((int) width, (int) height);
			frame.setVisible(true);

			pdf.finish();
			builder.finish();
		}
	}

	private static void draw(GC gc) {
		{
			gc.begin();
			gc.transform(AffineTransform.getTranslateInstance(250, 0));
			{
				gc.setStrokePaint(RGBColor.BLACK);
				gc.setFillPaint(RGBColor.WHITE);
				gc.setTextMode(GC.TEXT_MODE_FILL_STROKE);
				SimpleLayoutGlyphHandler lgh = new SimpleLayoutGlyphHandler();
				lgh.setGC(gc);
				TextLayoutHandler tlf = new TextLayoutHandler(gc, HyphenationBundle.getHyphenation("ja"), lgh);
				tlf.setDirection(FontStyle.DIRECTION_TB);
				tlf.setFontFamilies(FontFamilyList.create("IPAゴシック"));
				tlf.setFontSize(32);
				tlf.characters("白抜き");
				tlf.flush();
			}
			gc.transform(AffineTransform.getTranslateInstance(-50, 0));
			{
				gc.setStrokePaint(RGBColor.BLACK);
				gc.setFillPaint(RGBColor.create(255, 0, 0));
				gc.setTextMode(GC.TEXT_MODE_FILL);
				SimpleLayoutGlyphHandler lgh = new SimpleLayoutGlyphHandler();
				lgh.setGC(gc);
				TextLayoutHandler tlf = new TextLayoutHandler(gc, HyphenationBundle.getHyphenation("ja"), lgh);
				tlf.setDirection(FontStyle.DIRECTION_TB);
				tlf.setFontFamilies(FontFamilyList.create("IPAゴシック"));
				tlf.setFontWeight((short) 800);
				tlf.setFontSize(32);
				tlf.characters("太字");
				tlf.flush();
			}
			gc.end();
		}
	}
}
