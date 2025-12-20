package net.zamasoft.pdfg2d.demo;

import java.awt.Font;
import java.io.File;

import net.zamasoft.pdfg2d.PDFGraphics2D;

/**
 * Demonstrates emoji rendering.
 * <p>
 * This app shows how to render emoji characters using the PDFGraphics2D API.
 * </p>
 * 
 * @author MIYABE Tatsuhiko
 */
public class EmojiApp {
	public static void main(final String[] args) throws Exception {
		try (final var g2d = new PDFGraphics2D(new File(DemoUtils.getOutputDir(), "emoji.pdf"))) {
			g2d.setFont(new Font("emoji", Font.PLAIN, 38));
			g2d.drawString("\u26A1\uD83D\uDE01", 10, 140);
		}
	}
}
