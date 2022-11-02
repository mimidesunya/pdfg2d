package net.zamasoft.pdfg2d.pdf.font;

import java.awt.Shape;

import net.zamasoft.pdfg2d.font.BBox;

public interface PDFEmbeddedFont extends PDFFont {
	public String getPSName();

	public BBox getBBox();

	public String getRegistry();

	public String getOrdering();

	public int getSupplement();

	public Shape getShape(int i);

	public byte[] getCharString(int i);

	public int getGlyphCount();

	public int getCharCount();
}
