package net.zamasoft.pdfg2d.pdf.gc;

import net.zamasoft.pdfg2d.gc.GC;
import net.zamasoft.pdfg2d.gc.GraphicsException;
import net.zamasoft.pdfg2d.gc.image.Image;

/**
 * @author MIYABE Tatsuhiko
 * @version $Id: PdfImage.java 1565 2018-07-04 11:51:25Z miyabe $
 */

public class PdfImage implements Image {
	private final String name;

	private final double width;

	private final double height;

	public PdfImage(String name, double width, double height) {
		this.name = name;
		this.width = width;
		this.height = height;
	}

	public void drawTo(GC _gc) throws GraphicsException {
		PdfGC gc = (PdfGC) _gc;
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
		if (o instanceof PdfImage) {
			return ((PdfImage) o).name.equals(this.name);
		}
		return false;
	}

	public int hashCode() {
		return this.name.hashCode();
	}
}
