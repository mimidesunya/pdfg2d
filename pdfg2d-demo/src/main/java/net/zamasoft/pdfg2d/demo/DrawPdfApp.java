package net.zamasoft.pdfg2d.demo;

import java.awt.BasicStroke;
import java.awt.Color;
import java.io.File;

import net.zamasoft.pdfg2d.PdfGraphics2D;

public class DrawPdfApp {

	public static void main(String[] args) throws Exception {
		try(PdfGraphics2D g2d = new PdfGraphics2D(new File("out/drawpdf.pdf"))) {
			g2d.setColor(Color.RED);
			g2d.setStroke(new BasicStroke(5));
			g2d.drawOval(100, 100, 200, 200);
		}
	}
}
