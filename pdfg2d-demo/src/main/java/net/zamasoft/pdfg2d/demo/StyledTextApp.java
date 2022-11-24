package net.zamasoft.pdfg2d.demo;

import java.awt.geom.AffineTransform;
import java.io.File;

import jp.cssj.rsr.impl.FileRandomBuilder;
import net.zamasoft.pdfg2d.gc.font.FontFamilyList;
import net.zamasoft.pdfg2d.gc.font.FontStyle.Direction;
import net.zamasoft.pdfg2d.gc.font.FontStyle.Style;
import net.zamasoft.pdfg2d.gc.text.TextLayoutHandler;
import net.zamasoft.pdfg2d.gc.text.hyphenation.HyphenationBundle;
import net.zamasoft.pdfg2d.gc.text.layout.PageLayoutGlyphHandler;
import net.zamasoft.pdfg2d.gc.text.layout.PageLayoutGlyphHandler.Alignment;
import net.zamasoft.pdfg2d.pdf.PDFWriter;
import net.zamasoft.pdfg2d.pdf.gc.PDFGC;
import net.zamasoft.pdfg2d.pdf.impl.PDFWriterImpl;
import net.zamasoft.pdfg2d.pdf.util.PDFUtils;

public class StyledTextApp {
	public static void main(String[] args) throws Exception {
		try (PDFWriter pdf = new PDFWriterImpl(new FileRandomBuilder(new File("out/styled-text.pdf")));
				PDFGC gc = new PDFGC(pdf.nextPage(PDFUtils.mmToPt(PDFUtils.PAPER_A4_WIDTH_MM),
						PDFUtils.mmToPt(PDFUtils.PAPER_A4_HEIGHT_MM)));) {
			gc.transform(AffineTransform.getTranslateInstance(PDFUtils.mmToPt(10), PDFUtils.mmToPt(10)));
			try (PageLayoutGlyphHandler lgh = new PageLayoutGlyphHandler(gc)) {
				lgh.setLineAdvance(PDFUtils.mmToPt(PDFUtils.PAPER_A4_WIDTH_MM - 20));
				lgh.setAlign(Alignment.JUSTIFY);
				lgh.setLineHeight(1.616);
				try (TextLayoutHandler tlf = new TextLayoutHandler(gc, HyphenationBundle.getHyphenation("ja"), lgh)) {
					tlf.setDirection(Direction.LTR);
					tlf.setFontFamilies(FontFamilyList.SERIF);
					tlf.setFontSize(24);
					tlf.characters("兵は詭道なり。");
					tlf.setFontSize(16);
					tlf.characters(
							"故に能なるも之に不能を示し、用なるも之に不用を示し、近くとも之に遠きを示し、遠くとも之に近きを示し、利にして之を誘い、乱にして之を取り、実にして之に備え、強にして之を避け、怒にして之を撓し、卑にして之を驕らせ、佚にして之を労し、親にして之を離す。\n");
				}
				try (TextLayoutHandler tlf = new TextLayoutHandler(gc, HyphenationBundle.getHyphenation("en"), lgh)) {
					tlf.setFontFamilies(FontFamilyList.SANS_SERIF);
					tlf.setFontStyle(Style.ITALIC);
					tlf.setFontSize(12);
					tlf.characters(
							"All warfare is based on deception. Hence, when able to attack, we must seem unable; when using our forces, we must seem inactive; when we are near, we must make the enemy believe we are far away; when far away, we must make him believe we are near.");
				}
			}
		}
	}
}
