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
import net.zamasoft.pdfg2d.gc.font.FontStyle;
import net.zamasoft.pdfg2d.gc.paint.RGBColor;
import net.zamasoft.pdfg2d.gc.text.TextLayoutHandler;
import net.zamasoft.pdfg2d.gc.text.breaking.TextBreakingRulesBundle;
import net.zamasoft.pdfg2d.gc.text.layout.SimpleLayoutGlyphHandler;
import net.zamasoft.pdfg2d.pdf.PDFGraphicsOutput;
import net.zamasoft.pdfg2d.pdf.PDFWriter;
import net.zamasoft.pdfg2d.pdf.font.PDFFontSourceManager;
import net.zamasoft.pdfg2d.pdf.gc.PDFGC;
import net.zamasoft.pdfg2d.pdf.impl.PDFWriterImpl;
import net.zamasoft.pdfg2d.pdf.params.PDFParams;

/**
 * Demonstrates text outlining and stroking.
 * <p>
 * This demo shows how to render text with strokes (outlines) and fills color,
 * creating an effect where the character shapes are outlined.
 * </p>
 * 
 * @author MIYABE Tatsuhiko
 * @since 1.0
 */
public class TextOutlineDemo {
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

			params.setFontSourceManager(fsm);

			final double width = 300;
			final double height = 300;

			try (OutputStream out = new BufferedOutputStream(
					new FileOutputStream(new File(DemoUtils.getOutputDir(), "text-stroke.pdf")))) {
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
						g2d.translate(0, 24);
						g2d.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
						G2DGC gc = new G2DGC(g2d, pdf.getFontManager());
						TextOutlineDemo.draw(gc);
					}
				};
				frame.setSize((int) width, (int) height);
				frame.setVisible(true);

				pdf.close();
				builder.close();
			}
		}
	}

	private static void draw(GC gc) {
		{
			gc.begin();
			gc.transform(AffineTransform.getTranslateInstance(250, 0));
			{
				gc.setStrokePaint(RGBColor.BLACK);
				gc.setFillPaint(RGBColor.WHITE);
				gc.setTextMode(GC.TextMode.FILL_STROKE);
				SimpleLayoutGlyphHandler lgh = new SimpleLayoutGlyphHandler();
				lgh.setGC(gc);
				try (TextLayoutHandler tlf = new TextLayoutHandler(gc, TextBreakingRulesBundle.getRules("ja"), lgh)) {
					tlf.setDirection(FontStyle.Direction.TB);
					tlf.setFontFamilies(FontFamilyList.create("IPAex明朝"));
					tlf.setFontSize(32);
					tlf.characters("白抜き");
					tlf.flush();
				}
			}
			gc.transform(AffineTransform.getTranslateInstance(-50, 0));
			{
				gc.setStrokePaint(RGBColor.BLACK);
				gc.setFillPaint(RGBColor.create(255, 0, 0));
				gc.setTextMode(GC.TextMode.FILL);
				SimpleLayoutGlyphHandler lgh = new SimpleLayoutGlyphHandler();
				lgh.setGC(gc);
				try (TextLayoutHandler tlf = new TextLayoutHandler(gc, TextBreakingRulesBundle.getRules("ja"), lgh)) {
					tlf.setDirection(FontStyle.Direction.TB);
					tlf.setFontFamilies(FontFamilyList.create("IPAex明朝"));
					tlf.setFontWeight(FontStyle.Weight.W_800);
					tlf.setFontSize(32);
					tlf.characters("太字");
					tlf.flush();
				}
			}
			gc.end();
		}
	}
}
