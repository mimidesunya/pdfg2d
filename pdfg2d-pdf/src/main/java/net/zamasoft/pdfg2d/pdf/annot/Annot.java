package net.zamasoft.pdfg2d.pdf.annot;

import java.awt.Shape;
import java.awt.geom.Rectangle2D;
import java.io.IOException;

import net.zamasoft.pdfg2d.pdf.PDFOutput;
import net.zamasoft.pdfg2d.pdf.PDFPageOutput;

/**
 * Annotation.
 * 
 * @author MIYABE Tatsuhiko
 * @since 1.0
 */
public abstract class Annot {
	protected Shape shape;
	protected String contents;

	/**
	 * Sets the annotation area. Coordinate transformation by cm operator (or
	 * PDF_GC.transform method) is not applied. Therefore, the application must
	 * explicitly apply coordinate transformation to the shape.
	 * 
	 * @param shape Active area. For PDF 1.5 or earlier, the bounding box becomes
	 *              the specified range.
	 */
	public void setShape(final Shape shape) {
		this.shape = shape;
	}

	public Shape getShape() {
		return this.shape;
	}

	public String getContents() {
		return this.contents;
	}

	public void setContents(final String contents) {
		this.contents = contents;
	}

	public void writeTo(final PDFOutput out, final PDFPageOutput pageOut) throws IOException {
		out.writeName("Type");
		out.writeName("Annot");
		out.lineBreak();

		// Area
		final double pageHeight = pageOut.getHeight();
		out.writeName("Rect");
		out.startArray();
		final Rectangle2D rect = this.getShape().getBounds2D();
		final double x = rect.getX();
		final double y = rect.getY();
		final double width = rect.getWidth();
		final double height = rect.getHeight();
		out.writeReal(x);
		out.writeReal(pageHeight - (y + height));
		out.writeReal(x + width);
		out.writeReal(pageHeight - y);
		out.endArray();
		out.lineBreak();

		// Contents
		final String contents = this.getContents();
		if (contents != null) {
			out.writeName("Contents");
			out.writeText(contents);
			out.lineBreak();
		}
	}
}
