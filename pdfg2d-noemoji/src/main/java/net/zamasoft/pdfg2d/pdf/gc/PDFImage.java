package net.zamasoft.pdfg2d.pdf.gc;

import net.zamasoft.pdfg2d.gc.GC;
import net.zamasoft.pdfg2d.gc.GraphicsException;
import net.zamasoft.pdfg2d.gc.image.Image;

/**
 * @author MIYABE Tatsuhiko
 * @since 1.0
 */

public class PDFImage implements Image {
	private final String name;

	private final double width;

	private final double height;

	public PDFImage(String name, double width, double height) {
		this.name = name;
		this.width = width;
		this.height = height;
	}

	public void drawTo(GC _gc) throws GraphicsException {
		PDFGC gc = (PDFGC) _gc;
		gc.drawPDFImage(this.name, this.width, this.height);
	}

	public double getWidth() {
		return this.width;
	}

	public double getHeight() {
		return this.height;
	}

	public String getAltString() {
		return null;
	}

	public String getName() {
		return this.name;
	}

	public String toString() {
		return this.name;
	}

	public boolean equals(Object o) {
		if (o instanceof PDFImage) {
			return ((PDFImage) o).name.equals(this.name);
		}
		return false;
	}

	public int hashCode() {
		return this.name.hashCode();
	}
}
