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
import jp.cssj.sakae.gc.font.FontManager;
import jp.cssj.sakae.gc.font.FontStyle;
import jp.cssj.sakae.gc.text.TextLayoutHandler;
import jp.cssj.sakae.gc.text.hyphenation.HyphenationBundle;
import jp.cssj.sakae.gc.text.layout.SimpleLayoutGlyphHandler;
import jp.cssj.sakae.pdf.PdfGraphicsOutput;
import jp.cssj.sakae.pdf.PdfWriter;
import jp.cssj.sakae.pdf.font.FontManagerImpl;
import jp.cssj.sakae.pdf.font.PdfFontSourceManager;
import jp.cssj.sakae.pdf.gc.PdfGC;
import jp.cssj.sakae.pdf.impl.PdfWriterImpl;
import jp.cssj.sakae.pdf.params.PdfParams;

/**
 * @author MIYABE Tatsuhiko
 * @version $Id: TextDemo.java 1565 2018-07-04 11:51:25Z miyabe $
 */
public class TextDemo {
	public static void main(String[] args) throws Exception {
		PdfParams params = new PdfParams();
		params.setCompression(PdfParams.COMPRESSION_NONE);

		PdfFontSourceManager fsm = new PdfFontSourceManager();

		{
			FontFace face = new FontFace();
			face.src = new FileSource(new File("src/example/ipaexm.ttf"));
			face.fontFamily = FontFamilyList.create("IPAex明朝");
			fsm.addFontFace(face);
		}
		{
			FontFace face = new FontFace();
			face.src = new FileSource(new File("src/example/KentenGeneric.otf"));
			face.fontFamily = FontFamilyList.create("Kenten Generic");
			fsm.addFontFace(face);
		}
		{
			FontFace face = new FontFace();
			face.src = new FileSource(new File("src/example/UnDotum.ttf"));
			face.fontFamily = FontFamilyList.create("Hangul");
			fsm.addFontFace(face);
		}
		{
			FontFace face = new FontFace();
			face.src = new FileSource(new File("src/example/FT Meuang BL-Regular.ttf"));
			face.fontFamily = FontFamilyList.create("FT Meuang");
			fsm.addFontFace(face);
		}

		params.setFontSourceManager(fsm);

		final double width = 300;
		final double height = 300;

		try (OutputStream out = new BufferedOutputStream(new FileOutputStream("test.pdf"))) {
			StreamRandomBuilder builder = new StreamRandomBuilder(out);
			final PdfWriter pdf = new PdfWriterImpl(builder, params);

			try (PdfGraphicsOutput page = pdf.nextPage(width, height)) {
				PdfGC gc = new PdfGC(page);
				draw(gc);
			}

			pdf.finish();
			builder.finish();
		}

		final FontManager fm = new FontManagerImpl(fsm);
		JFrame frame = new JFrame("Graphics") {
			private static final long serialVersionUID = 1L;

			@Override
			public void paint(Graphics g) {
				super.paint(g);
				Graphics2D g2d = (Graphics2D) g;
				g2d.translate(0, 24);
				g2d.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
				G2dGC gc = new G2dGC(g2d, fm);
				TextDemo.draw(gc);
			}
		};
		frame.setSize((int) width, (int) height);
		frame.setVisible(true);
	}

	private static void draw(GC gc) {
		{
			gc.begin();
			gc.transform(AffineTransform.getTranslateInstance(200, 0));
			SimpleLayoutGlyphHandler lgh = new SimpleLayoutGlyphHandler();
			lgh.setGC(gc);
			TextLayoutHandler tlf = new TextLayoutHandler(gc, HyphenationBundle.getHyphenation("ja"), lgh);
			tlf.setDirection(FontStyle.DIRECTION_TB);
			tlf.setFontFamilies(FontFamilyList.create("IPAex明朝"));
			tlf.setFontSize(16);
			tlf.characters("盗人を捕らえてみれば我が子なり\n斬りたくもあり斬りたくもなし\n");
			tlf.setFontStyle(FontStyle.FONT_STYLE_OBLIQUE);
			tlf.characters("盗人を捕らえてみれば我が子なり\n斬りたくもあり斬りたくもなし\n");
			tlf.setFontStyle(FontStyle.FONT_STYLE_NORMAL);
			tlf.characters("The thief caught turn out to be one's own son.\n");
			tlf.setFontStyle(FontStyle.FONT_STYLE_OBLIQUE);
			tlf.characters("The thief caught turn out to be one's own son.\n");
			tlf.flush();
			gc.end();
		}

		{
			gc.begin();
			gc.transform(AffineTransform.getTranslateInstance(100, 0));
			SimpleLayoutGlyphHandler lgh = new SimpleLayoutGlyphHandler();
			lgh.setGC(gc);
			TextLayoutHandler tlf = new TextLayoutHandler(gc, HyphenationBundle.getHyphenation("ja"), lgh);
			tlf.setDirection(FontStyle.DIRECTION_TB);
			tlf.setFontFamilies(FontFamilyList.create("Kenten Generic"));
			tlf.setFontSize(16);
			tlf.characters("﹅﹆");
			tlf.flush();
			gc.end();
		}

		// {
		// gc.begin();
		// gc.transform(AffineTransform.getTranslateInstance(10, 20));
		// SimpleLayoutGlyphHandler lgh = new SimpleLayoutGlyphHandler();
		// lgh.setGC(gc);
		// TextLayoutHandler tlf = new TextLayoutHandler(gc,
		// HyphenationBundle.getHyphenation("ja"), lgh);
		// tlf.setDirection(FontStyle.DIRECTION_LTR);
		// tlf.setFontFamilies(FontFamilyList.create("Hangul"));
		// tlf.setFontSize(16);
		// tlf.characters("은 돋움");
		// tlf.flush();
		// gc.end();
		// }
		//
		// {
		// gc.begin();
		// gc.transform(AffineTransform.getTranslateInstance(10, 30));
		// SimpleLayoutGlyphHandler lgh = new SimpleLayoutGlyphHandler();
		// lgh.setGC(gc);
		// TextLayoutHandler tlf = new TextLayoutHandler(gc,
		// HyphenationBundle.getHyphenation("ja"), lgh);
		// tlf.setDirection(FontStyle.DIRECTION_LTR);
		// tlf.setFontFamilies(FontFamilyList.create("FT Meuang"));
		// tlf.setFontSize(16);
		// tlf.characters("ศิลปะการต่อสู้ป้องกันตัว");
		// tlf.flush();
		// gc.end();
		// }
	}
}
