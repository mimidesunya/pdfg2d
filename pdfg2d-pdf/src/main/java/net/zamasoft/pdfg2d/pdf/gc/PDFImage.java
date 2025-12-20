package net.zamasoft.pdfg2d.pdf.gc;

import net.zamasoft.pdfg2d.gc.GC;
import net.zamasoft.pdfg2d.gc.GraphicsException;
import net.zamasoft.pdfg2d.gc.image.Image;

/**
 * Represents a PDF image resource.
 * 
 * @param name   The PDF resource name.
 * @param width  The image width.
 * @param height The image height.
 * 
 * @author MIYABE Tatsuhiko
 * @since 1.0
 */
public record PDFImage(String name, double width, double height) implements Image {
	@Override
	public double getWidth() {
		return this.width;
	}

	@Override
	public double getHeight() {
		return this.height;
	}

	@Override
	public void drawTo(final GC gc) throws GraphicsException {
		if (gc instanceof PDFGC pdfgc) {
			pdfgc.drawPDFImage(this.name, this.width, this.height);
		}
	}

	@Override
	public String getAltString() {
		return null;
	}

	@Override
	public String toString() {
		return this.name;
	}

	/**
	 * Returns the image resource name.
	 * 
	 * @return The name.
	 */
	public String getName() {
		return this.name();
	}
}
