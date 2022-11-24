package net.zamasoft.pdfg2d.gc.text;

public interface Element {
	public static enum Type {
		TEXT, QUAD
	}

	public Type getElementType();

	public abstract double getAdvance();
}
