package net.zamasoft.pdfg2d.pdf.gc;

import java.io.IOException;
import java.io.OutputStream;

import net.zamasoft.pdfg2d.gc.GC;
import net.zamasoft.pdfg2d.gc.GraphicsException;
import net.zamasoft.pdfg2d.gc.image.Image;
import net.zamasoft.pdfg2d.pdf.ObjectRef;
import net.zamasoft.pdfg2d.pdf.PdfNamedGraphicsOutput;
import net.zamasoft.pdfg2d.pdf.PdfWriter;

/**
 * オフスクリーン画像です。
 * 
 * @author MIYABE Tatsuhiko
 * @version $Id: PdfGroupImage.java 1565 2018-07-04 11:51:25Z miyabe $
 */
public abstract class PdfGroupImage extends PdfNamedGraphicsOutput implements Image {
	private final String name;

	private final ObjectRef objectRef;

	public static final int VIEW_OFF = 1;
	public static final int PRINT_OFF = 2;

	protected int ocgFlags = 0;

	protected PdfGroupImage(PdfWriter pdfWriter, OutputStream out, double width, double height, String name,
			ObjectRef objectRef) throws IOException {
		super(pdfWriter, out, width, height);
		this.name = name;
		this.objectRef = objectRef;
	}

	public void setOCG(int ocgFlags) {
		this.ocgFlags = ocgFlags;
	}

	public void drawTo(GC _gc) throws GraphicsException {
		PdfGC gc = (PdfGC) _gc;
		gc.drawPDFImage(this.name, this.width, this.height);
	}

	public String getName() {
		return this.name;
	}

	public ObjectRef getObjectRef() {
		return this.objectRef;
	}

	public String getAltString() {
		return null;
	}

	public String toString() {
		return this.name;
	}

	public boolean equals(Object o) {
		if (o instanceof PdfGroupImage) {
			return ((PdfGroupImage) o).name.equals(this.name);
		}
		return false;
	}

	public int hashCode() {
		return this.name.hashCode();
	}
}
