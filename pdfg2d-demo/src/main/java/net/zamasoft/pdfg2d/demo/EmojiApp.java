package net.zamasoft.pdfg2d.demo;

import java.awt.Font;
import java.io.File;

import net.zamasoft.pdfg2d.PDFGraphics2D;

public class EmojiApp {
	public static void main(String[] args) throws Exception {
		try (PDFGraphics2D g2d = new PDFGraphics2D(new File("out/emoji.pdf"))) {
			g2d.setFont(new Font(Font.SERIF, Font.PLAIN, 38));
			g2d.drawString("Emoji \u26A1", 10, 100);
			g2d.setFont(new Font("emoji", Font.PLAIN, 38));
			g2d.drawString("Emoji \u26A1", 10, 140);
		}
	}
}
