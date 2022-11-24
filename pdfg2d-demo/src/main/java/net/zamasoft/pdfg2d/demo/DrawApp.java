package net.zamasoft.pdfg2d.demo;

import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.geom.Line2D;
import java.awt.geom.Rectangle2D;
import java.io.File;

import net.zamasoft.pdfg2d.PDFGraphics2D;
import net.zamasoft.pdfg2d.pdf.util.PDFUtils;

public class DrawApp {
	public static void main(String[] args) throws Exception {
		try (PDFGraphics2D g2d = new PDFGraphics2D(new File("out/draw.pdf"))) {
			g2d.setColor(Color.WHITE);
			g2d.fill(new Rectangle2D.Double(0, 0, PDFUtils.mmToPt(PDFUtils.PAPER_A4_WIDTH_MM),
					PDFUtils.mmToPt(PDFUtils.PAPER_A4_HEIGHT_MM)));

			g2d.setColor(Color.RED);
			g2d.fill(new Rectangle2D.Double(PDFUtils.mmToPt(51), 0, PDFUtils.mmToPt(PDFUtils.PAPER_A4_WIDTH_MM - 51),
					PDFUtils.mmToPt(154)));

			g2d.setColor(Color.BLUE);
			g2d.fill(new Rectangle2D.Double(0, PDFUtils.mmToPt(154), PDFUtils.mmToPt(51),
					PDFUtils.mmToPt(PDFUtils.PAPER_A4_HEIGHT_MM) - 154));

			g2d.setColor(Color.YELLOW);
			g2d.fill(new Rectangle2D.Double(PDFUtils.mmToPt(187), PDFUtils.mmToPt(182),
					PDFUtils.mmToPt(PDFUtils.PAPER_A4_WIDTH_MM - 187),
					PDFUtils.mmToPt(PDFUtils.PAPER_A4_HEIGHT_MM) - 182));

			g2d.setStroke(new BasicStroke((float) PDFUtils.mmToPt(7)));
			g2d.setColor(Color.BLACK);
			g2d.draw(new Line2D.Double(PDFUtils.mmToPt(51), 0, PDFUtils.mmToPt(51),
					PDFUtils.mmToPt(PDFUtils.PAPER_A4_HEIGHT_MM)));
			g2d.draw(new Line2D.Double(0, PDFUtils.mmToPt(154), PDFUtils.mmToPt(PDFUtils.PAPER_A4_WIDTH_MM),
					PDFUtils.mmToPt(154)));
			g2d.draw(new Line2D.Double(PDFUtils.mmToPt(187), PDFUtils.mmToPt(154), PDFUtils.mmToPt(187),
					PDFUtils.mmToPt(PDFUtils.PAPER_A4_HEIGHT_MM)));

			g2d.setStroke(new BasicStroke((float) PDFUtils.mmToPt(14)));
			g2d.draw(new Line2D.Double(0, PDFUtils.mmToPt(70), PDFUtils.mmToPt(51), PDFUtils.mmToPt(70)));
			g2d.draw(new Line2D.Double(PDFUtils.mmToPt(187), PDFUtils.mmToPt(182),
					PDFUtils.mmToPt(PDFUtils.PAPER_A4_WIDTH_MM), PDFUtils.mmToPt(182)));
		}
	}
}
