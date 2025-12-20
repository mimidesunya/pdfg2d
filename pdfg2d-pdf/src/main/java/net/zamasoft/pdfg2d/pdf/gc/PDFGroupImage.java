package net.zamasoft.pdfg2d.pdf.gc;

import java.io.IOException;
import java.io.OutputStream;

import net.zamasoft.pdfg2d.gc.GC;
import net.zamasoft.pdfg2d.gc.GraphicsException;
import net.zamasoft.pdfg2d.gc.image.Image;
import net.zamasoft.pdfg2d.pdf.ObjectRef;
import net.zamasoft.pdfg2d.pdf.PDFNamedGraphicsOutput;
import net.zamasoft.pdfg2d.pdf.PDFWriter;

/**
 * Represents an offscreen group image in PDF.
 * This class corresponds to a PDF Form XObject.
 * 
 * @author MIYABE Tatsuhiko
 * @since 1.0
 */
public abstract class PDFGroupImage extends PDFNamedGraphicsOutput implements Image {
	private final String name;

	private final ObjectRef objectRef;

	public static final int VIEW_OFF = 1;
	public static final int PRINT_OFF = 2;

	protected int ocgFlags = 0;

	protected PDFGroupImage(final PDFWriter pdfWriter, final OutputStream out, final double width, final double height,
			final String name, final ObjectRef objectRef) throws IOException {
		super(pdfWriter, out, width, height);
		this.name = name;
		this.objectRef = objectRef;
	}

	/**
	 * Sets the Optional Content Group (OCG) flags.
	 * 
	 * @param ocgFlags The flags to set.
	 */
	public void setOCG(final int ocgFlags) {
		this.ocgFlags = ocgFlags;
	}

	@Override
	public void drawTo(final GC gc) throws GraphicsException {
		if (gc instanceof PDFGC pdfgc) {
			pdfgc.drawPDFImage(this.name, this.width, this.height);
		}
	}

	@Override
	public String getName() {
		return this.name;
	}

	/**
	 * Returns the object reference for this group image.
	 * 
	 * @return The object reference.
	 */
	public ObjectRef getObjectRef() {
		return this.objectRef;
	}

	@Override
	public String getAltString() {
		return null;
	}

	@Override
	public String toString() {
		return this.name;
	}

	@Override
	public boolean equals(final Object o) {
		if (o instanceof PDFGroupImage other) {
			return other.name.equals(this.name);
		}
		return false;
	}

	@Override
	public int hashCode() {
		return this.name.hashCode();
	}
}
