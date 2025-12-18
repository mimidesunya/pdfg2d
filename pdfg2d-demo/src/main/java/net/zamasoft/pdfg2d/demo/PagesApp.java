package net.zamasoft.pdfg2d.demo;

import java.awt.Font;
import java.io.File;

import net.zamasoft.pdfg2d.io.impl.FileStream;
import net.zamasoft.pdfg2d.PDFGraphics2D;
import net.zamasoft.pdfg2d.pdf.PDFWriter;
import net.zamasoft.pdfg2d.pdf.impl.PDFWriterImpl;
import net.zamasoft.pdfg2d.pdf.util.PDFUtils;

/**
 * Demonstrates multi-page PDF generation.
 * <p>
 * This app creates a PDF with multiple pages and draws content on each page.
 * </p>
 * 
 * @author MIYABE Tatsuhiko
 */
public class PagesApp {
	public static void main(String[] args) throws Exception {
		try (PDFWriter pdf = new PDFWriterImpl(
				new FileStream(new File(DemoUtils.getOutputDir(), "pages.pdf")))) {
			try (PDFGraphics2D g2d = new PDFGraphics2D(pdf.nextPage(PDFUtils.mmToPt(PDFUtils.PAPER_A4_WIDTH_MM),
					PDFUtils.mmToPt(PDFUtils.PAPER_A4_HEIGHT_MM)))) {
				g2d.setFont(new Font(Font.SERIF, Font.PLAIN, 38));
				g2d.drawString("Page 1", 10, 100);
			}
			try (PDFGraphics2D g2d = new PDFGraphics2D(pdf.nextPage(PDFUtils.mmToPt(PDFUtils.PAPER_A4_WIDTH_MM),
					PDFUtils.mmToPt(PDFUtils.PAPER_A4_HEIGHT_MM)))) {
				g2d.setFont(new Font(Font.SERIF, Font.PLAIN, 38));
				g2d.drawString("Page 2", 10, 100);
			}
		}
	}
}
