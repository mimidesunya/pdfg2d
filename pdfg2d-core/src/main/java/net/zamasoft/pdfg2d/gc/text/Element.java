package net.zamasoft.pdfg2d.gc.text;

public interface Element {
	public static final short TEXT = 1;
	public static final short QUAD = 2;

	public short getElementType();

	public abstract double getAdvance();
}
