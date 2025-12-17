package net.zamasoft.pdfg2d.demo;

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
import net.zamasoft.pdfg2d.g2d.gc.G2DGC;
import net.zamasoft.pdfg2d.gc.GC;
import net.zamasoft.pdfg2d.gc.font.FontFace;
import net.zamasoft.pdfg2d.gc.font.FontFamilyList;
import net.zamasoft.pdfg2d.gc.font.FontManager;
import net.zamasoft.pdfg2d.gc.font.FontStyle;
import net.zamasoft.pdfg2d.gc.text.TextLayoutHandler;
import net.zamasoft.pdfg2d.gc.text.breaking.TextBreakingRulesBundle;
import net.zamasoft.pdfg2d.gc.text.layout.SimpleLayoutGlyphHandler;
import net.zamasoft.pdfg2d.pdf.PDFGraphicsOutput;
import net.zamasoft.pdfg2d.pdf.PDFWriter;
import net.zamasoft.pdfg2d.pdf.font.FontManagerImpl;
import net.zamasoft.pdfg2d.pdf.font.PDFFontSourceManager;
import net.zamasoft.pdfg2d.pdf.gc.PDFGC;
import net.zamasoft.pdfg2d.pdf.impl.PDFWriterImpl;
import net.zamasoft.pdfg2d.pdf.params.PDFParams;

/**
 * Demonstrates complex text layout capabilities.
 * <p>
 * This demo shows support for various languages (Japanese, Thai, Korean, etc.),
 * vertical text layout, and font substitution/fallback mechanisms.
 * </p>
 * 
 * @author MIYABE Tatsuhiko
 * @since 1.0
 */
public class ComplexTextDemo {
	public static void main(String[] args) throws Exception {
		PDFParams params = new PDFParams();
		params.setCompression(PDFParams.Compression.NONE);

		try (PDFFontSourceManager fsm = new PDFFontSourceManager()) {
			{
				FontFace face = new FontFace();
				face.src = new FileSource(DemoUtils.getResourceFile("ipaexm.ttf"));
				face.fontFamily = FontFamilyList.create("IPAex明朝");
				fsm.addFontFace(face);
			}
			{
				FontFace face = new FontFace();
				face.src = new FileSource(DemoUtils.getResourceFile("KentenGeneric.otf"));
				face.fontFamily = FontFamilyList.create("Kenten Generic");
				fsm.addFontFace(face);
			}
			{
				FontFace face = new FontFace();
				face.src = new FileSource(DemoUtils.getResourceFile("UnDotum.ttf"));
				face.fontFamily = FontFamilyList.create("Hangul");
				fsm.addFontFace(face);
			}
			{
				FontFace face = new FontFace();
				face.src = new FileSource(DemoUtils.getResourceFile("FT Meuang BL-Regular.ttf"));
				face.fontFamily = FontFamilyList.create("FT Meuang");
				fsm.addFontFace(face);
			}

			params.setFontSourceManager(fsm);

			final double width = 300;
			final double height = 300;

			try (OutputStream out = new BufferedOutputStream(
					new FileOutputStream(new File(DemoUtils.getOutputDir(), "text.pdf")))) {
				StreamRandomBuilder builder = new StreamRandomBuilder(out);
				final PDFWriter pdf = new PDFWriterImpl(builder, params);

				try (PDFGraphicsOutput page = pdf.nextPage(width, height)) {
					PDFGC gc = new PDFGC(page);
					draw(gc);
				}

				pdf.close();
				builder.close();
			}

			final FontManager fm = new FontManagerImpl(fsm);
			try {
				JFrame frame = new JFrame("Graphics") {
					private static final long serialVersionUID = 1L;

					@Override
					public void paint(Graphics g) {
						super.paint(g);
						Graphics2D g2d = (Graphics2D) g;
						g2d.translate(0, 24);
						g2d.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
						G2DGC gc = new G2DGC(g2d, fm);
						ComplexTextDemo.draw(gc);
					}
				};
				frame.setSize((int) width, (int) height);
				frame.setVisible(true);
			} finally {
				// fm does not need close? Check implementation.
				// The original code tried to close it.
				// If FontManagerImpl is closeable but FontManager is not, cast and close?
				if (fm instanceof AutoCloseable) {
					((AutoCloseable) fm).close();
				}
			}
		}
	}

	private static void draw(GC gc) {
		{
			gc.begin();
			gc.transform(AffineTransform.getTranslateInstance(200, 0));
			SimpleLayoutGlyphHandler lgh = new SimpleLayoutGlyphHandler();
			lgh.setGC(gc);
			try (TextLayoutHandler tlf = new TextLayoutHandler(gc, TextBreakingRulesBundle.getRules("ja"), lgh)) {
				// Set text direction to top-to-bottom (vertical)
				tlf.setDirection(FontStyle.Direction.TB);
				tlf.setFontFamilies(FontFamilyList.create("IPAex明朝"));
				tlf.setFontSize(16);
				tlf.characters("盗人を捕らえてみれば我が子なり\n斬りたくもあり斬りたくもなし\n");

				// Change font style to oblique
				tlf.setFontStyle(FontStyle.Style.OBLIQUE);
				tlf.characters("盗人を捕らえてみれば我が子なり\n斬りたくもあり斬りたくもなし\n");
				// Reset font style to normal
				tlf.setFontStyle(FontStyle.Style.NORMAL);
				tlf.characters("The thief caught turn out to be one's own son.\n");
				tlf.setFontStyle(FontStyle.Style.OBLIQUE);
				tlf.characters("The thief caught turn out to be one's own son.\n");
				tlf.flush();
			}
			gc.end();
		}

		{
			gc.begin();
			gc.transform(AffineTransform.getTranslateInstance(100, 0));
			SimpleLayoutGlyphHandler lgh = new SimpleLayoutGlyphHandler();
			lgh.setGC(gc);
			try (TextLayoutHandler tlf = new TextLayoutHandler(gc, TextBreakingRulesBundle.getRules("ja"), lgh)) {
				tlf.setDirection(FontStyle.Direction.TB);
				tlf.setFontFamilies(FontFamilyList.create("Kenten Generic"));
				tlf.setFontSize(16);
				tlf.characters("﹅﹆");
				tlf.flush();
			}
			gc.end();
		}

		// {
		// gc.begin();
		// gc.transform(AffineTransform.getTranslateInstance(10, 20));
		// SimpleLayoutGlyphHandler lgh = new SimpleLayoutGlyphHandler();
		// lgh.setGC(gc);
		// TextLayoutHandler tlf = new TextLayoutHandler(gc,
		// TextBreakingRulesBundle.getRules("ja"), lgh);
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
		// TextBreakingRulesBundle.getRules("ja"), lgh);
		// tlf.setDirection(FontStyle.DIRECTION_LTR);
		// tlf.setFontFamilies(FontFamilyList.create("FT Meuang"));
		// tlf.setFontSize(16);
		// tlf.characters("ศิลปะการต่อสู้ป้องกันตัว");
		// tlf.flush();
		// gc.end();
		// }
	}
}
